/*
 * Copyright 2012  Research Studios Austria Forschungsges.m.b.H.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package won.server.protocol.local;

import won.protocol.exception.IllegalMessageForNeedStateException;
import won.protocol.exception.NoSuchNeedException;
import won.protocol.matcher.NodeFromMatcherReceiver;
import won.server.service.NeedService;

import java.net.URI;
import java.util.Collection;

/**
 * User: fkleedorfer
 * Date: 02.11.12
 */
public class NodeFromMatcherReceiverLocalImpl implements NodeFromMatcherReceiver
{
  private NeedService needService;

  @Override
  public void hint(final URI needURI, final URI otherNeed, final double score, final URI originator) throws NoSuchNeedException, IllegalMessageForNeedStateException
  {
    needService.hint(needURI,otherNeed,score,originator);
  }

  @Override
  public Collection<URI> listNeedURIs()
  {
    return needService.listNeedURIs();
  }

  @Override
  public Collection<URI> listConnectionURIs(final URI needURI) throws NoSuchNeedException
  {
    return needService.listConnectionURIs(needURI);
  }

  public NeedService getNeedService()
  {
    return needService;
  }

  public void setNeedService(final NeedService needService)
  {
    this.needService = needService;
  }
}