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

package won.bot.framework.events.listener;

import org.springframework.scheduling.TaskScheduler;
import won.bot.framework.bot.BotContext;
import won.bot.framework.events.EventBus;
import won.bot.framework.component.needproducer.NeedProducer;
import won.bot.framework.component.nodeurisource.NodeURISource;
import won.protocol.owner.OwnerProtocolNeedServiceClientSide;
import won.protocol.repository.ConnectionRepository;
import won.protocol.repository.NeedRepository;

import java.util.concurrent.Executor;

/**
 * Class holding references to all important services that EventListeners inside bots need to
 * access.
 */
public interface EventListenerContext
{
  /**
   * Returns the bot's taskScheduler. Used to schedule actions later without blocking other work.
   * @return
   */
  public TaskScheduler getTaskScheduler();

  /**
   * Returns the bot's NodeURISource. Used to obtain WON_BACC node URIs.
   * @return
   */
  public NodeURISource getNodeURISource();

  /**
   * Returns the bot's owner service. Used to connect to WON_BACC nodes.
   */
  public OwnerProtocolNeedServiceClientSide getOwnerService();

  /**
   * Returns the bot's needProducer. Used to obtain an RDF model that can be sent to a WON_BACC node to create a new need.
   * @return
   */
  public NeedProducer getNeedProducer();


  /**
   * The bot may have a trigger attached that causes ActEvents to be created regularly. This call stops the trigger.
   */
  public void cancelTrigger();

  /**
   * Signals to the framework that the bot's work is done and it may be shut down.
   */
  public void workIsDone();

  /**
   * Returns the bot's event bus. Used to publish events and subscribe for them.
   * @return
   */
  public EventBus getEventBus();

  /**
   * Returns the BotContext of the bot. Used to access the known need URIs.
   * @return
   */
  public BotContext getBotContext();

  /**
   * Returns an executor that passes the tasks to the TaskScheduler for immediate execution.
   * @return
   */
  public Executor getExecutor();


}
