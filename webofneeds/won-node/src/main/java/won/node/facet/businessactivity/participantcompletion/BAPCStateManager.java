package won.node.facet.businessactivity.participantcompletion;

import java.net.URI;

/**
 * Created with IntelliJ IDEA.
 * User: Danijel
 * Date: 23.1.14.
 * Time: 16.29
 * To change this template use File | Settings | File Templates.
 */
public interface BAPCStateManager {
    public BAPCState getStateForNeedUri(URI ownerUri, URI needUri);
    public void setupStateForNeedUri(URI needUri);
    public void setStateForNeedUri(BAPCState state, URI ownerUri, URI needURI);
}
