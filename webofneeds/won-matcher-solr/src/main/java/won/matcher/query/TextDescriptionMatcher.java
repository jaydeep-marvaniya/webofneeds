package won.matcher.query;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similar.MoreLikeThis;
import org.apache.solr.core.SolrCore;
import org.apache.solr.search.SolrIndexSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import won.matcher.protocol.impl.MatcherProtocolNeedServiceClient;
import won.protocol.exception.IllegalMessageForNeedStateException;
import won.protocol.exception.NoSuchNeedException;
import won.protocol.rest.LinkedDataRestClient;
import won.protocol.vocabulary.GEO;
import won.protocol.vocabulary.WON;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: gabriel
 * Date: 21.02.13
 * Time: 15:38
 * To change this template use File | Settings | File Templates.
 */
public class TextDescriptionMatcher
{
  Logger logger;
  SolrCore solrCore;
  MatcherProtocolNeedServiceClient client;
  private Set<String> knownMatches = new HashSet();
  private static final String FIELD_NTRIPLE = "ntriple";
  private static final String FIELD_URL = "url";
  private static final int MAX_MATCHES = 3;
  private static double MAX_DISTANCE_KM = 15;

  public TextDescriptionMatcher(SolrCore solrCore)
  {
    this.solrCore = solrCore;
    logger = LoggerFactory.getLogger(TextDescriptionMatcher.class);
    client = new MatcherProtocolNeedServiceClient();
    client.setLinkedDataRestClient(new LinkedDataRestClient());
  }

  public TextDescriptionMatcher()
  {
    this(null);
  }

  public void executeQuery()
  {
    SolrIndexSearcher si = null;
    IndexReader ir = null;
    logger.debug("executeQuery called!!");
    //TODO: Deprecated.
    //if(SolrCore.openHandles.keySet().isEmpty())
    //    return;
    //solrCore = SolrCore.openHandles.keySet().iterator().next();
    solrCore = SolrCore.getSolrCore();
    si = solrCore.getSearcher().get();
    ir = si.getIndexReader();

    MoreLikeThis mlt = new MoreLikeThis(ir);
    mlt.setMinDocFreq(1);
    mlt.setMinTermFreq(1);
    mlt.setFieldNames(new String[]{FIELD_NTRIPLE});
    Query query = null;
    TopDocs tdocs = null;

    int numMatches = 0;

    logger.debug("maxDoc: " + si.maxDoc());
    for (int i = 0; i < si.maxDoc(); i++) {
      try {
        query = mlt.like(i);
        String fromUriString = ir.document(i).getValues(FIELD_URL)[0]; //getValues() instead of get() just to make sure we don't have more than 1
        fromUriString = fromUriString.replaceAll("^<", "").replaceAll(">$", "");
        URI fromURI = URI.create(fromUriString);

        //now load the triples from the doc and make a model so we can perform some basic checks
        String fromNtriples = ir.document(i).get(FIELD_NTRIPLE);
        Model fromModel = convertNTriplesToModel(fromNtriples);
        //check if we are talking about a need here. If not, don't consider the matches
        if (!isNeed(fromURI.toString(), fromModel)) {
          logger.debug("not a need, skipping: {}", fromURI.toString());
          continue;
        }
        //remember what need type it is so we can compare to the need type of what we find
        //TODO: move this check into the query
        Resource fromBasicNeedType = getBasicNeedType(fromUriString, fromModel);
        Point fromPoint = getPointIfPresent(fromUriString, fromModel);

        tdocs = si.search(query, 10);
        logger.debug("Found {} hits for doc {}", tdocs.totalHits, ir.document(i).getValues(FIELD_URL)[0]);
        if (tdocs.totalHits > 0) {
          logger.debug("MaxScore: {}", tdocs.getMaxScore());

          ScoreDoc[] scoreDocs = tdocs.scoreDocs;
          for (int docInd = 0; docInd < scoreDocs.length; docInd++) {
            try {
              Document toDoc = ir.document(tdocs.scoreDocs[docInd].doc);
              String toUriString = toDoc.getValues(FIELD_URL)[0];
              toUriString = toUriString.replaceAll("^<", "").replaceAll(">$", "");
              URI toURI = new URI(toUriString);
              if (fromURI.equals(toURI)) continue;

              logger.debug("from document url: {}", fromURI);
              logger.debug("to document url: {}", toURI);
              //TODO fix this hack! how do we keep the score in (0,1)?
              double score = tdocs.scoreDocs[docInd].score / 10;
              score = Math.max(0, Math.min(1, score));


              //now load the triples from the doc and make a model so we can perform some basic checks
              String toNtriples = toDoc.get(FIELD_NTRIPLE);
              Model toModel = convertNTriplesToModel(toNtriples);
              //check if we are talking about a need here. If not, don't consider the matches
              if (!isNeed(toURI.toString(), toModel)) {
                logger.debug("not a need, skipping: {}", toURI.toString());
                continue;
              }
              //remember what need type it is so we can compare to the need type of what we find
              //TODO: move this check into the query
              Resource toBasicNeedType = getBasicNeedType(toURI.toString(), toModel);

              //ignore the match if the need types don't fit
              if (!isCompatibleBasicNeedType(fromBasicNeedType, toBasicNeedType)) {
                logger.debug("basic need types incompatible (from:{}, to:{}). Ignoring match {}", new Object[]{fromBasicNeedType, toBasicNeedType, toURI.toString()});
                continue;
              }

              //weighting scalar for the score, derived from the distance
              double scoreWeight = 1.0;

              //ignore the match if the locations are more than MAX_DISTANCE_KM apart
              if (fromPoint != null) {
                Point toPoint = getPointIfPresent(toUriString, toModel);
                if (toPoint != null){
                  double distance = fromPoint.distance(toPoint);
                  if (distance > MAX_DISTANCE_KM) {
                    logger.debug("geo points are too far apart ({} km), ignoring match {}", fromPoint.distance(toPoint), toURI.toString());
                    continue;
                  }
                  scoreWeight = scoreWeight * (MAX_DISTANCE_KM - distance) / MAX_DISTANCE_KM;
                }
              }

              logger.debug("score: {}, weighted score: {}", score, score * scoreWeight);
              score = score * scoreWeight;
              String matchKey = fromURI.toString() + " <=> " + toURI.toString();
              String matchKey2 = toURI.toString() + " <=> " + fromURI.toString();
              if (this.knownMatches.contains(matchKey) || this.knownMatches.contains(matchKey2)) {
                logger.debug("ignoring known match: " + matchKey);
                //if we send one match, that's enough - but it seems we've sent it already, so break
                break;
              } else {
                //add the match key before sending the hint!
                this.knownMatches.add(matchKey);
                logger.debug("new match: " + matchKey);
                logger.debug("sending hint..");
                client.hint(fromURI, toURI, score, new URI("http://LDSpiderMatcher.webofneeds"));
                client.hint(toURI, fromURI, score, new URI("http://LDSpiderMatcher.webofneeds"));
                logger.debug("hint sent.");
                //check if we have enough matches
                if (++numMatches > MAX_MATCHES) break;
              }
            } catch (NoSuchNeedException e) {
              logger.warn("Hint failed: no such need", e);
            } catch (IllegalMessageForNeedStateException e) {
              logger.warn("Hint failed: illegal message for need state", e);
            } catch (URISyntaxException e) {
              logger.warn("Hint failed: illegal URI", e);
            } catch (Exception e) {
              logger.warn("Hint failed.", e);
            }

          }

        } else {
          logger.debug("Nothing found similar to " + fromUriString);
        }
      } catch (IOException e) {
        logger.warn("Could not generate similarity query with document " + i + "!", e);  //To change body of catch statement use File | Settings | File Templates.
      }
    }

    logger.debug("done matching. Known matches: \n {}", Arrays.toString(knownMatches.toArray(new String[knownMatches.size()])));
  }

  private Model convertNTriplesToModel(String ntriples)
  {
    Model model = ModelFactory.createDefaultModel();
    model.read(new StringReader(ntriples), WON.getURI(), "N-TRIPLES");
    return model;
  }

  private boolean isNeed(String needURI, Model model)
  {
    return model.contains(model.getResource(needURI), RDF.type, WON.NEED);
  }

  private Resource getModality(String needURI, Model model)
  {
    Statement stmt = model.getProperty(model.getResource(needURI), WON.HAS_NEED_MODALITY);
    if (stmt == null) {
      return null;
    }
    return stmt.getObject().asResource();
  }

   private Resource getBasicNeedType(String needURI, Model model)
  {
    Statement stmt = model.getProperty(model.getResource(needURI), WON.HAS_BASIC_NEED_TYPE);
    if (stmt == null) {
      return null;
    }
    return stmt.getObject().asResource();
  }


  private boolean isCompatibleBasicNeedType(Resource fromType, Resource toType)
  {
    return WON.BASIC_NEED_TYPE_DO.equals(fromType) && WON.BASIC_NEED_TYPE_DO.equals(toType) ||
        WON.BASIC_NEED_TYPE_GIVE.equals(fromType) && WON.BASIC_NEED_TYPE_TAKE.equals(toType) ||
        WON.BASIC_NEED_TYPE_TAKE.equals(fromType) && WON.BASIC_NEED_TYPE_GIVE.equals(toType);
  }

  private Point getPointIfPresent(String needURI, Model model)
  {
    Resource modalityRes = getModality(needURI,model);
    if (modalityRes == null ) {
      logger.debug("{} has no modality, can't look for location", needURI);
      return null;
    }
    Statement stmt = modalityRes.getProperty(WON.AVAILABLE_AT_LOCATION);
    if (stmt == null || !stmt.getObject().isResource()) {
      logger.debug("{} has no property {} or that property is not a resource", needURI, WON.AVAILABLE_AT_LOCATION.getLocalName());
      return null;
    }
    Resource locationRes = stmt.getObject().asResource();
    stmt = locationRes.getProperty(GEO.LATITUDE);
    if (stmt == null || !stmt.getObject().isLiteral()) {
      logger.debug("latitude of {} is null or not a literal", needURI);
      return null;
    }
    double lat = stmt.getObject().asLiteral().getDouble();
    stmt = locationRes.getProperty(GEO.LONGITUDE);
    if (stmt == null || !stmt.getObject().isLiteral()) {
      logger.debug("longitude of {} is null or not a literal", needURI);
      return null;
    }
    double lon = stmt.getObject().asLiteral().getDouble();
    Point point = new Point(lat, lon);
    logger.debug("extracted these geo coordinates {} from need {}", point, needURI);
    return point;
  }


  private class Point
  {
    public Point(double lat, double lon)
    {
      this.lat = lat;
      this.lon = lon;
    }

    public double lat;
    public double lon;

    public double distance(Point other)
    {
      double R = 6371; // km
      double dLat = Math.toRadians(other.lat - this.lat);
      double dLon = Math.toRadians(other.lon - this.lon);
      double lat1 = Math.toRadians(this.lat);
      double lat2 = Math.toRadians(other.lat);

      double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
          Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
      double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
      double d = R * c;
      logger.debug("calculated a distance of {} km between {} and {}", new Object[]{d, this,other});
      return d;
    }

    @Override
    public String toString()
    {
      return "Point{" +
          "lat=" + lat +
          ", lon=" + lon +
          '}';
    }
  }

}
