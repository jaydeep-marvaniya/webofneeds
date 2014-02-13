package won.bot.core.impl;

import won.bot.core.base.EventBot;
import won.bot.core.event.*;
import won.bot.core.eventlistener.*;
import won.protocol.model.FacetType;
import won.protocol.util.WonRdfUtils;

import java.net.URI;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Danijel
 * Date: 12.2.14.
 * Time: 20.45
 * To change this template use File | Settings | File Templates.
 */
public class BAPCBot extends EventBot {
    private static final int NO_OF_NEEDS = 5;
    private static final int NO_OF_MESSAGES = 10;
    private static final long MILLIS_BETWEEN_MESSAGES = 1000;

    @Override
    protected void initializeEventListeners()
    {
        //create needs every trigger execution until 2 needs are created
        getEventBus().subscribe(ActEvent.class, new CreateNeedOnActListener(getEventListenerContext(), NO_OF_NEEDS));

        //count until 2 needs were created, then
        //   * connect the 2 needs
        getEventBus().subscribe(NeedCreatedEvent.class, new ExecuteOnceAfterNEventsListener(getEventListenerContext(), NO_OF_NEEDS, new Runnable(){
            @Override
            public void run()
            {
                List<URI> needs = getBotContext().listNeedUris();
                try {
                    for(int i=1; i<NO_OF_NEEDS; i++)
                    {
                        getOwnerService().connect(needs.get(0), needs.get(i), WonRdfUtils.FacetUtils.createModelForConnect(FacetType.BAPCCoordinatorFacet.getURI(), FacetType.BAPCParticipantFacet.getURI()));
                    }
                } catch (Exception e) {
                    logger.warn("could not connect {} and {}", new Object[]{needs.get(0), needs.get(1)},e);
                }
            }
        }));

        //add a listener that is informed of the connect/open events and that auto-opens
        //subscribe it to:
        // * connect events - so it responds with open
        // * open events - so it responds with open (if the open received was the first open, and we still need to accept the connection)
        AutomaticConnectionOpenerListener autoOpener = new AutomaticConnectionOpenerListener(getEventListenerContext());
        getEventBus().subscribe(OpenFromOtherNeedEvent.class, autoOpener);
        getEventBus().subscribe(ConnectFromOtherNeedEvent.class, autoOpener);

        //add a listener that auto-responds to messages by a message
        //after 10 messages, it closes the connections
        //subscribe it to:
        // * message events - so it responds
        // * open events - so it initiates the chain reaction of responses
        BAPCMessageListener autoMessageResponder = new BAPCMessageListener(getEventListenerContext(), NO_OF_MESSAGES, MILLIS_BETWEEN_MESSAGES);
        getEventBus().subscribe(OpenFromOtherNeedEvent.class, autoMessageResponder);
        getEventBus().subscribe(MessageFromOtherNeedEvent.class, autoMessageResponder);

        //add a listener that auto-responds to a close message with a deactivation of both needs.
        //subscribe it to:
        // * close events
        DeactivateNeedsOnConnectionCloseListener needDeactivator= new DeactivateNeedsOnConnectionCloseListener(getEventListenerContext());
        getEventBus().subscribe(CloseFromOtherNeedEvent.class, needDeactivator);


    }

}
