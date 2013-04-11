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

package won.owner.service.impl;

import com.hp.hpl.jena.rdf.model.Model;
import won.protocol.exception.*;
import won.protocol.model.Connection;
import won.protocol.model.Match;
import won.protocol.model.Need;
import won.protocol.owner.OwnerProtocolNeedService;
import won.protocol.owner.OwnerProtocolOwnerService;

import java.net.URI;
import java.util.Collection;

/**
 * Implementation for testing purposes; communicates only with partners within the same VM.
 */
public abstract class AbstractOwnerProtocolOwnerService implements OwnerProtocolOwnerService
{
  protected OwnerProtocolNeedService ownerProtocolNeedService;
  protected URI lastConnectionURI;

  @Override
  public void hintReceived(final URI ownNeedURI, final URI otherNeedURI, final double score, final URI originatorURI) throws NoSuchNeedException
  {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void connectionRequested(final URI ownNeedURI, final URI otherNeedURI, final URI ownConnectionURI, final String message) throws NoSuchNeedException, ConnectionAlreadyExistsException, IllegalMessageForNeedStateException
  {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void accept(final URI connectionURI) throws NoSuchConnectionException, IllegalMessageForConnectionStateException
  {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void deny(final URI connectionURI) throws NoSuchConnectionException, IllegalMessageForConnectionStateException
  {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void close(final URI connectionURI) throws NoSuchConnectionException, IllegalMessageForConnectionStateException
  {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void sendTextMessage(final URI connectionURI, final String message) throws NoSuchConnectionException, IllegalMessageForConnectionStateException
  {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  public URI createNeed(final URI ownerURI, final Model content, final boolean activate) throws IllegalNeedContentException
  {
    return ownerProtocolNeedService.createNeed(ownerURI, content, activate);
  }

  public Model readNeedContent(final URI needURI) throws NoSuchNeedException
  {
    return ownerProtocolNeedService.readNeedContent(needURI);
  }

  public Need readNeed(final URI needURI) throws NoSuchNeedException
  {
    return ownerProtocolNeedService.readNeed(needURI);
  }

  public Collection<Match> getMatches(final URI needURI) throws NoSuchNeedException
  {
    return ownerProtocolNeedService.listMatches(needURI);
  }


  public Connection readConnection(final URI connectionURI) throws NoSuchConnectionException
  {
    this.lastConnectionURI = connectionURI;
    return ownerProtocolNeedService.readConnection(connectionURI);
  }

  public void activate(final URI needURI) throws NoSuchNeedException
  {
    ownerProtocolNeedService.activate(needURI);
  }

  public URI connectTo(final URI needURI, final URI otherNeedURI, final String message) throws NoSuchNeedException, IllegalMessageForNeedStateException, ConnectionAlreadyExistsException
  {
    URI connUri = ownerProtocolNeedService.connectTo(needURI, otherNeedURI, message);
    this.lastConnectionURI = connUri;
    return connUri;
  }

  public Model readConnectionContent(final URI connectionURI) throws NoSuchConnectionException
  {
    this.lastConnectionURI = connectionURI;
    return ownerProtocolNeedService.readConnectionContent(connectionURI);
  }

  public void deactivate(final URI needURI) throws NoSuchNeedException
  {
    ownerProtocolNeedService.deactivate(needURI);
  }

  public Collection<URI> listConnectionURIs(final URI needURI) throws NoSuchNeedException
  {
    return ownerProtocolNeedService.listConnectionURIs(needURI);
  }

  public Collection<URI> listNeedURIs()
  {
    return ownerProtocolNeedService.listNeedURIs();
  }

  public void setOwnerProtocolNeedService(final OwnerProtocolNeedService ownerProtocolNeedService)
  {
    this.ownerProtocolNeedService = ownerProtocolNeedService;
  }
}
