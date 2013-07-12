package won.matcher.protocol.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import won.matcher.ws.MatcherProtocolNeedWebServiceClient;
import won.protocol.exception.NoSuchNeedException;
import won.protocol.rest.LinkedDataRestClient;
import won.protocol.vocabulary.WON;
import won.protocol.ws.AbstractClientFactory;
import won.protocol.ws.MatcherProtocolNeedWebServiceEndpoint;

import java.net.MalformedURLException;
import java.net.URI;

/**
 * User: atus
 * Date: 22.05.13
 */
public class MatcherProtocolNeedClientFactory extends AbstractClientFactory<MatcherProtocolNeedWebServiceClient>
{
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  private LinkedDataRestClient linkedDataRestClient;

  public MatcherProtocolNeedWebServiceEndpoint getMatcherProtocolEndpointForNeed(URI needURI) throws NoSuchNeedException, MalformedURLException
  {
    URI needProtocolEndpoint = linkedDataRestClient.getURIPropertyForResource(needURI, WON.MATCHER_PROTOCOL_ENDPOINT);
    if (needProtocolEndpoint == null) throw new NoSuchNeedException(needURI);
    logger.debug("need won.matcher.protocol endpoint of need {} is {}", needURI.toString(), needProtocolEndpoint.toString());

    URI needProtocolEndpointUri = URI.create(needProtocolEndpoint.toString() + "?wsdl");

    MatcherProtocolNeedWebServiceClient client = getCachedClient(needProtocolEndpointUri);
    if (client == null) {
      client = new MatcherProtocolNeedWebServiceClient(needProtocolEndpointUri.toURL());
      cacheClient(needProtocolEndpointUri, client);
    }

    return client.getOwnerProtocolOwnerWebServiceEndpointPort();
  }

  public void setLinkedDataRestClient(LinkedDataRestClient linkedDataRestClient)
  {
    this.linkedDataRestClient = linkedDataRestClient;
  }
}
