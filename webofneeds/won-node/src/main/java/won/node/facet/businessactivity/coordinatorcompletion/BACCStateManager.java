package won.node.facet.businessactivity.coordinatorcompletion;

import java.net.URI;

/**
 * Created with IntelliJ IDEA.
 * User: Danijel
 * Date: 6.2.14.
 * Time: 16.05
 * To change this template use File | Settings | File Templates.
 */
public interface BACCStateManager {
    public BACCState getStateForNeedUri(URI ownerUri, URI needUri);
    public void setupStateForNeedUri(URI needUri);
    public void setStateForNeedUri(BACCState state, URI ownerUri, URI needURI);
}