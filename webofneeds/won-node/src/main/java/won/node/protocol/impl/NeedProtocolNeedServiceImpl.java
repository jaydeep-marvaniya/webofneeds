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

package won.node.protocol.impl;

import com.hp.hpl.jena.rdf.model.Model;
import won.node.service.impl.NeedFacingConnectionCommunicationServiceImpl;
import won.protocol.exception.*;
import won.protocol.need.NeedProtocolNeedService;
import won.protocol.service.NeedFacingNeedCommunicationService;

import java.net.URI;

//import com.hp.hpl.jena.util.ModelQueryUtil;
//import com.sun.xml.internal.bind.v2.TODO;

/**
 * User: fkleedorfer
 * Date: 02.11.12
 */
public class NeedProtocolNeedServiceImpl implements NeedProtocolNeedService
{
  protected NeedFacingNeedCommunicationService needFacingNeedCommunicationService;
  protected NeedFacingConnectionCommunicationServiceImpl connectionCommunicationService;

  @Override
  public URI connect(final URI need, final URI otherNeedURI, final URI otherConnectionURI, final Model content) throws NoSuchNeedException, IllegalMessageForNeedStateException, ConnectionAlreadyExistsException {
    return this.needFacingNeedCommunicationService.connect(need, otherNeedURI, otherConnectionURI, content);
  }

  @Override
  public void open(final URI connectionURI, final Model content) throws NoSuchConnectionException, IllegalMessageForConnectionStateException {
      connectionCommunicationService.open(connectionURI, content);
  }

  @Override
  public void close(final URI connectionURI, final Model content) throws NoSuchConnectionException, IllegalMessageForConnectionStateException {
    connectionCommunicationService.close(connectionURI, content);
  }

  @Override
  public void textMessage(final URI connectionURI, final Model message) throws NoSuchConnectionException, IllegalMessageForConnectionStateException {
    connectionCommunicationService.textMessage(connectionURI, message);
  }


  public void setNeedFacingNeedCommunicationService(final NeedFacingNeedCommunicationService needFacingNeedCommunicationService)
  {
    this.needFacingNeedCommunicationService = needFacingNeedCommunicationService;
  }

  public void setConnectionCommunicationService(final NeedFacingConnectionCommunicationServiceImpl connectionCommunicationService)
  {
    this.connectionCommunicationService = connectionCommunicationService;
  }
}
