/*
 * Copyright 2012  Research Studios Austria Forschungsges.m.b.H.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package won.bot.impl;

import won.bot.framework.bot.base.EventBot;
import won.bot.framework.events.EventBus;
import won.bot.framework.events.event.*;
import won.bot.framework.events.listener.*;
import won.protocol.model.FacetType;

/**
 *
 */
public class Create2NeedsShortConversationBot extends EventBot
{

  private static final int NO_OF_NEEDS = 2;
  private static final int NO_OF_MESSAGES = 10;
  private static final long MILLIS_BETWEEN_MESSAGES = 1000;
    private static final String NAME_NEEDS = "needs";


  //we use protected members so we can extend the class and
  //access the listeners for unit test assertions and stats
  //
  //we use BaseEventListener as their types so we can access the generic
  //functionality offered by that class
  protected BaseEventListener needCreator;
  protected BaseEventListener needConnector;
  protected BaseEventListener autoOpener;
  protected BaseEventListener autoResponder;
  protected BaseEventListener connectionCloser;
  protected BaseEventListener needDeactivator;
  protected BaseEventListener workDoneSignaller;

  @Override
  protected void initializeEventListeners()
  {
    EventListenerContext ctx = getEventListenerContext();
    EventBus bus = getEventBus();

    //create needs every trigger execution until 2 needs are created
    this.needCreator = new ExecuteOnEventListener(
        ctx,
        new EventBotActions.CreateNeedAction(ctx,NAME_NEEDS),
        NO_OF_NEEDS
    );
    bus.subscribe(ActEvent.class,this.needCreator);

    //count until 2 needs were created, then
    //   * connect the 2 needs
    this.needConnector = new ExecuteOnceAfterNEventsListener(ctx,
        new EventBotActions.ConnectFromListToListAction(ctx,NAME_NEEDS,NAME_NEEDS,FacetType.OwnerFacet.getURI(),FacetType.OwnerFacet.getURI()),NO_OF_NEEDS);
    bus.subscribe(NeedCreatedEvent.class, this.needConnector);

    //add a listener that is informed of the connect/open events and that auto-opens
    //subscribe it to:
    // * connect events - so it responds with open
    // * open events - so it responds with open (if the open received was the first open, and we still need to accept the connection)
    this.autoOpener = new AutomaticConnectionOpenerListener(ctx);
    bus.subscribe(OpenFromOtherNeedEvent.class, this.autoOpener);
    bus.subscribe(ConnectFromOtherNeedEvent.class, this.autoOpener);

    //add a listener that auto-responds to messages by a message
    //after 10 messages, it unsubscribes from all events
    //subscribe it to:
    // * message events - so it responds
    // * open events - so it initiates the chain reaction of responses
    this.autoResponder = new AutomaticMessageResponderListener(ctx, NO_OF_MESSAGES, MILLIS_BETWEEN_MESSAGES);
    bus.subscribe(OpenFromOtherNeedEvent.class, this.autoResponder);
    bus.subscribe(MessageFromOtherNeedEvent.class, this.autoResponder);

    //add a listener that closes the connection after it has seen 10 messages
    this.connectionCloser = new DelegateOnceAfterNEventsListener(
        ctx,
        NO_OF_MESSAGES,
        new CloseConnectionListener(ctx));
    bus.subscribe( MessageFromOtherNeedEvent.class, this.connectionCloser);

    //add a listener that auto-responds to a close message with a deactivation of both needs.
    //subscribe it to:
    // * close events
    this.needDeactivator = new DeactivateNeedOnConnectionCloseListener(ctx);
    bus.subscribe(CloseFromOtherNeedEvent.class, this.needDeactivator);

    //add a listener that counts two NeedDeactivatedEvents and then tells the
    //framework that the bot's work is done
    this.workDoneSignaller = new ExecuteOnceAfterNEventsListener(
        ctx,
        new EventBotActions.SignalWorkDoneAction(ctx), NO_OF_NEEDS
    );
    bus.subscribe(NeedDeactivatedEvent.class, this.workDoneSignaller);
  }


}
