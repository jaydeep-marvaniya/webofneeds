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

package won.bot.framework.events.event;

import com.hp.hpl.jena.rdf.model.Model;
import won.protocol.model.FacetType;

import java.net.URI;

/**
 *
 */
public class NeedCreatedEvent extends BaseEvent
{
  private final URI needUri;
  private final URI wonNodeUri;
  private final Model needModel;
  private final FacetType facetType;

  public NeedCreatedEvent(final URI needUri, final URI wonNodeUri, final Model needModel,final FacetType facetType)
  {
    this.needUri = needUri;
    this.wonNodeUri = wonNodeUri;
    this.needModel = needModel;
    this.facetType = facetType;
  }

  public URI getNeedUri()
  {
    return needUri;
  }

  public URI getWonNodeUri()
  {
    return wonNodeUri;
  }

  public Model getNeedModel()
  {
    return needModel;
  }

}
