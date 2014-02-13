package won.bot.core.eventlistener;

import com.hp.hpl.jena.rdf.model.Model;
import won.bot.core.event.MessageFromOtherNeedEvent;
import won.bot.core.event.OpenFromOtherNeedEvent;
import won.bot.core.event.BAStateChangeEvent;
import won.bot.events.Event;
import won.protocol.model.ConnectionState;
import won.protocol.util.WonRdfUtils;

import java.net.URI;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Danijel
 * Date: 13.2.14.
 * Time: 10.36
 * To change this template use File | Settings | File Templates.
 */
public class BAPCMessageListener extends BaseEventListener {
    private int targetNumberOfMessages = -1;
    private int numberOfMessages = 0;
    private long millisTimeoutBeforeReply = 1000;
    private Object monitor = new Object();

    public BAPCMessageListener(final EventListenerContext context, final int targetNumberOfMessages, final long millisTimeoutBeforeReply)
    {
        super(context);
        this.targetNumberOfMessages = targetNumberOfMessages;
    }
    public void onEvent(final Event event) throws Exception
    {
        if (event instanceof BAStateChangeEvent){
            handleMessageEvent((BAStateChangeEvent) event);
        } else if (event instanceof OpenFromOtherNeedEvent) {
            handleOpenEvent((OpenFromOtherNeedEvent) event);
        }

    }

    /**
     * React to open event by sending a message.
     *
     * @param openEvent
     */
    private void handleOpenEvent(final OpenFromOtherNeedEvent openEvent)
    {
        if (openEvent.getCon().getState() == ConnectionState.CONNECTED){
            getEventListenerContext().getTaskScheduler().schedule(new Runnable()
            {
                @Override
                public void run()
                {
                    URI connectionUri = openEvent.getCon().getConnectionURI();
                    try {
                        getEventListenerContext().getOwnerService().textMessage(connectionUri, WonRdfUtils.MessageUtils.textMessage(createMessage()));
                    } catch (Exception e){
                        logger.warn("could not send message via connection {}", connectionUri,e);
                    }
                }
            }, new Date(System.currentTimeMillis() + millisTimeoutBeforeReply));
        }
    }

    public void handleMessageEvent(final BAStateChangeEvent messageEvent){
        logger.debug("got message '{}' for need: {}", messageEvent.getMessage().getMessage(), messageEvent.getCon().getNeedURI());

    }

    private void handleMessageEvent(final MessageFromOtherNeedEvent messageEvent){
        logger.debug("got message '{}' for need: {}", messageEvent.getMessage().getMessage(), messageEvent.getCon().getNeedURI());
        getEventListenerContext().getTaskScheduler().schedule(new Runnable(){
            @Override
            public void run()
            {
                String message = createMessage();
                Model messageContent = WonRdfUtils.MessageUtils.textMessage(message);
                URI connectionUri = messageEvent.getCon().getConnectionURI();
                try {
                    getEventListenerContext().getOwnerService().textMessage(connectionUri, messageContent);
                    countMessageAndUnsubscribeIfNecessary();
                } catch (Exception e) {
                    logger.warn("could not send message via connection {}", connectionUri, e);
                }
            }
        }, new Date(System.currentTimeMillis() + this.targetNumberOfMessages));
    }

    private void countMessageAndUnsubscribeIfNecessary()
    {
        synchronized (monitor){
            numberOfMessages++;
            if (targetNumberOfMessages > 0 && targetNumberOfMessages >= numberOfMessages ){
                unsubscribe();
            }
        }
    }

    private String createMessage()
    {
        String message = "auto reply no " + numberOfMessages;
        if (targetNumberOfMessages > 0){
            message += " of " + targetNumberOfMessages;
        }
        return message;
    }

    private void unsubscribe()
    {
        getEventListenerContext().getEventBus().unsubscribe(MessageFromOtherNeedEvent.class, this);
    }
}
