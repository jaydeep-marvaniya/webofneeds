package won.matcher.query;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.search.SolrIndexSearcher;
import won.protocol.vocabulary.WON;

import java.io.IOException;

/**
 * User: atus
 * Date: 05.07.13
 */
public class BasicNeedTypeQueryFactory extends AbstractQueryFactory
{
  private final String needTypeField;

  public BasicNeedTypeQueryFactory(BooleanClause.Occur occur, float boost, String needTypeField)
  {
    super(occur, boost);
    this.needTypeField = needTypeField;
  }

  public BasicNeedTypeQueryFactory(final BooleanClause.Occur occur, final String needTypeField)
  {
    super(occur);
    this.needTypeField = needTypeField;
  }

  @Override
  public Query createQuery(final SolrIndexSearcher indexSearcher, final SolrInputDocument inputDocument) throws IOException
  {
    if (!inputDocument.containsKey(needTypeField))
      return null;

    String matchingNeedType = getMatchingNeedType(inputDocument.getFieldValue(needTypeField).toString());
    if (matchingNeedType == null)
      return null;

    Query query = new TermQuery(new Term(needTypeField, matchingNeedType));

    return query;
  }

  protected String getMatchingNeedType(String needType)
  {
    if (needType.equals(WON.BASIC_NEED_TYPE_SUPPLY.toString()))
      return WON.BASIC_NEED_TYPE_DEMAND.toString();
    else if (needType.equals(WON.BASIC_NEED_TYPE_DEMAND.toString()))
      return WON.BASIC_NEED_TYPE_SUPPLY.toString();
    else if (needType.equals(WON.BASIC_NEED_TYPE_DO_TOGETHER.toString()))
      return WON.BASIC_NEED_TYPE_DO_TOGETHER.toString();
    else if (needType.equals(WON.BASIC_NEED_TYPE_CRITIQUE.toString()))
      return WON.BASIC_NEED_TYPE_CRITIQUE.toString();
    else
      return null;
  }
}
