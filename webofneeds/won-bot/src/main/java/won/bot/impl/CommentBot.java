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

import org.springframework.beans.factory.annotation.Qualifier;
import won.bot.framework.bot.base.EventBot;
import won.bot.framework.component.needproducer.NeedProducer;
import won.bot.framework.events.EventBus;
import won.bot.framework.events.event.*;
import won.bot.framework.events.listener.*;
import won.protocol.model.FacetType;

/**
 *
 */
public class CommentBot extends EventBot
{

  private static final int NO_OF_NEEDS = 1;


  //we use protected members so we can extend the class and
  //access the listeners for unit test assertions and stats
  //
  //we use BaseEventListener as their types so we can access the generic
  //functionality offered by that class
  protected BaseEventListener needCreator;
  protected BaseEventListener commentFacetCreator;
  protected BaseEventListener needConnector;
  protected BaseEventListener autoOpener;
  protected BaseEventListener autoResponder;
  protected BaseEventListener connectionCloser;
  protected BaseEventListener allNeedsDeactivator;
  protected BaseEventListener needDeactivator;
  protected BaseEventListener workDoneSignaller;
    private static final String NAME_NEEDS = "needs";
    private static final String NAME_COMMENTS = "comments";

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

    //count until 1 need is created, then create a comment facet
    this.commentFacetCreator = new ExecuteOnEventListener(ctx,
            new EventBotActions.CreateNeedWithFacetsAction(ctx,NAME_COMMENTS,FacetType.CommentFacet.getURI()),1) ;
    bus.subscribe(NeedCreatedEvent.class, this.commentFacetCreator);

    this.needConnector = new ExecuteOnceAfterNEventsListener(ctx,
            new EventBotActions.ConnectFromListToListAction(ctx, NAME_NEEDS, NAME_COMMENTS,  FacetType.OwnerFacet.getURI(),FacetType.CommentFacet.getURI()),
            2
    );
    bus.subscribe(NeedCreatedEvent.class, this.needConnector);

      this.autoOpener = new AutomaticConnectionOpenerListener(ctx);
      bus.subscribe(OpenFromOtherNeedEvent.class, this.autoOpener);
      bus.subscribe(ConnectFromOtherNeedEvent.class, this.autoOpener);

      //add a listener that closes the connection after it has seen 10 messages
      this.connectionCloser = new DelegateOnceAfterNEventsListener(
              ctx,
              2,
              new CloseConnectionListener(ctx));
      bus.subscribe( ConnectFromOtherNeedEvent.class, this.connectionCloser);
      bus.subscribe(OpenFromOtherNeedEvent.class,this.connectionCloser);

      //add a listener that auto-responds to a close message with a deactivation of both needs.
      //subscribe it to:
      // * close events
      this.allNeedsDeactivator = new DeactivateAllNeedsOnConnectionCloseListener(ctx);
      bus.subscribe(CloseFromOtherNeedEvent.class, this.allNeedsDeactivator);

      //add a listener that counts two NeedDeactivatedEvents and then tells the
      //framework that the bot's work is done
      this.workDoneSignaller = new ExecuteOnceAfterNEventsListener(
              ctx,
              new EventBotActions.SignalWorkDoneAction(ctx), 2
      );
      bus.subscribe(NeedDeactivatedEvent.class, this.workDoneSignaller);

  }

}
