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

package won.bot.framework.bot.base;

import org.springframework.scheduling.TaskScheduler;

import java.util.Date;
import java.util.concurrent.Executor;

/**
 * Bot that has access to a scheduler for performing recurring or deferred work
 */
public class ScheduledActionBot extends BasicServiceBot
{
  private TaskScheduler taskScheduler;
  private Executor insideSchedulerExecutor = new InsideSchedulerExecutor();



  public void setTaskScheduler(final TaskScheduler taskScheduler)
  {
    this.taskScheduler = taskScheduler;
  }

  /**
   * Returns the TaskScheduler.
   * @return
   */
  protected TaskScheduler getTaskScheduler()
  {
    return taskScheduler;
  }

  /**
   * Returns an executor that passes the tasks to the TaskScheduler for immediate execution.
   * @return
   */
  protected Executor getExecutor(){ return this.insideSchedulerExecutor;  }

  private class InsideSchedulerExecutor implements Executor
  {
    @Override
    public void execute(final Runnable command)
    {
      getTaskScheduler().schedule(command,new Date());
    }
  }
}
