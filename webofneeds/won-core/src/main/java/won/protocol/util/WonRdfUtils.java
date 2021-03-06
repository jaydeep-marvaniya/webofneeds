package won.protocol.util;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import won.protocol.vocabulary.WON;

import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Utilities for populating/manipulating the RDF models used throughout the WON application.
 */
public class WonRdfUtils
{
  private static final Logger logger = LoggerFactory.getLogger(WonRdfUtils.class);
  public static class MessageUtils {
    /**
     * Creates an RDF model containing a text message.
     * @param message
     * @return
     */
    public static Model textMessage(String message) {
      com.hp.hpl.jena.rdf.model.Model messageModel = ModelFactory.createDefaultModel();
      messageModel.setNsPrefix("", "no:uri");
      Resource baseRes = messageModel.createResource(messageModel.getNsPrefixURI(""));
      baseRes.addProperty(RDF.type, WON.TEXT_MESSAGE);
      baseRes.addProperty(WON.HAS_TEXT_MESSAGE,message, XSDDatatype.XSDstring);
      return messageModel;
    }
  }

  public static class FacetUtils {

    /**
     * Returns the first facet found in the model, attached to the null relative URI '<>'.
     * Returns null if there is no such facet.
     * @param content
     * @return
     */
    public static URI getFacet(Model content) {
      logger.debug("getFacet(model) called");
      Resource baseRes = RdfUtils.getBaseResource(content);
      StmtIterator stmtIterator = baseRes.listProperties(WON.HAS_FACET);
      if (!stmtIterator.hasNext()) {
        logger.debug("no facet found in model");
        return null;
      }
      URI facetURI = URI.create(stmtIterator.next().getObject().asResource().getURI());
      if (logger.isDebugEnabled()){
        if (stmtIterator.hasNext()){
          logger.debug("returning facet {}, but model has more facets than just this one.");
        }
      }
      return facetURI;
    }

    /**
     * Returns all facets found in the model, attached to the null relative URI '<>'.
     * Returns an empty collection if there is no such facet.
     * @param content
     * @return
     */
    public static Collection<URI> getFacets(Model content) {
      Resource baseRes = RdfUtils.getBaseResource(content);
      StmtIterator stmtIterator = baseRes.listProperties(WON.HAS_FACET);
      LinkedList<URI> ret = new LinkedList<URI>();
      while (stmtIterator.hasNext()){
        RDFNode object = stmtIterator.nextStatement().getObject();
        if (object.isURIResource()){
          ret.add(URI.create(object.asResource().getURI()));
        }
      }
      return ret;
    }

    /**
     * Adds a triple to the model of the form <> won:hasFacet [facetURI].
     * @param content
     * @param facetURI
     */
    public static void addFacet(final Model content, final URI facetURI)
    {
      Resource baseRes = RdfUtils.getBaseResource(content);
      baseRes.addProperty(WON.HAS_FACET, content.createResource(facetURI.toString()));
    }

    /**
     * Adds a triple to the model of the form <> won:hasRemoteFacet [facetURI].
     * @param content
     * @param facetURI
     */
    public static void addRemoteFacet(final Model content, final URI facetURI)
    {
      Resource baseRes = RdfUtils.getBaseResource(content);
      baseRes.addProperty(WON.HAS_REMOTE_FACET, content.createResource(facetURI.toString()));
    }

    /**
     * Creates a model for connecting two facets.
     * @return
     */
    public static Model createModelForConnect(URI facet, URI remoteFacet)
    {
      Model model = ModelFactory.createDefaultModel();
      model.setNsPrefix("", "no:uri");
      WonRdfUtils.FacetUtils.addFacet(model, facet);
      WonRdfUtils.FacetUtils.addRemoteFacet(model, remoteFacet);
      return model;
    }
  }


}
