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

package won.bot.framework.core.event;

import com.hp.hpl.jena.rdf.model.Model;
import won.protocol.model.Connection;

/**
 */
public class ConnectFromOtherNeedEvent extends BaseEvent
{
  private final Connection con;
  private final Model content;

  public ConnectFromOtherNeedEvent(final Connection con, final Model content)
  {
    this.con = con;
    this.content = content;
  }

  public Connection getCon()
  {
    return con;
  }

  public Model getContent()
  {
    return content;
  }
}
