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
import org.springframework.beans.factory.annotation.Autowired;
import won.node.service.impl.OwnerFacingConnectionCommunicationServiceImpl;
import won.protocol.exception.*;
import won.protocol.model.Connection;
import won.protocol.model.ConnectionEvent;
import won.protocol.model.Need;
import won.protocol.owner.OwnerProtocolNeedService;
import won.protocol.repository.OwnerApplicationRepository;
import won.protocol.service.*;

import java.net.URI;
import java.util.Collection;
import java.util.List;

/**
 * User: fkleedorfer
 * Date: 02.11.12
 */
public class OwnerProtocolNeedServiceImpl implements OwnerProtocolNeedService {
    private OwnerFacingNeedCommunicationService needCommunicationService;
    private OwnerFacingConnectionCommunicationServiceImpl connectionCommunicationService;
    private NeedManagementService needManagementService;
    private NeedInformationService needInformationService;
    private OwnerManagementService ownerManagementService;

    @Autowired
    private OwnerApplicationRepository ownerApplicationRepository;

    @Override
    public URI createNeed(URI ownerURI, final Model content, final boolean activate, String ownerApplicationID) throws IllegalNeedContentException {
        URI needURI = this.needManagementService.createNeed(ownerURI, content, activate, ownerApplicationID);

        return needURI;
    }

    @Override
    public void authorizeOwnerApplicationForNeed(String ownerApplicationID, URI needURI) {
        needManagementService.authorizeOwnerApplicationForNeed(ownerApplicationID,needURI);
    }

    @Override
    public Collection<URI> listConnectionURIs() {
        return needInformationService.listConnectionURIs();
    }

    @Override
    public Collection<URI> listConnectionURIs(int page) {
        return needInformationService.listConnectionURIs(page);
    }

    @Override
    public Collection<URI> listNeedURIs(int page) {
        return needInformationService.listNeedURIs(page);
    }

    @Override
    public Collection<URI> listConnectionURIs(URI needURI, int page) throws NoSuchNeedException {
        return needInformationService.listConnectionURIs(needURI, page);
    }

   // @Override
    public void activate(final URI needURI) throws NoSuchNeedException {
        this.needManagementService.activate(needURI);
    }

   // @Override
    public void deactivate(final URI needURI) throws NoSuchNeedException, NoSuchConnectionException, IllegalMessageForConnectionStateException {
        this.needManagementService.deactivate(needURI);
    }

    @Override
    public URI connect(final URI need, final URI otherNeedURI, final Model content) throws NoSuchNeedException, IllegalMessageForNeedStateException, ConnectionAlreadyExistsException {
        return this.needCommunicationService.connect(need, otherNeedURI, content);
    }

    @Override
    public void open(final URI connectionURI, final Model content) throws NoSuchConnectionException, IllegalMessageForConnectionStateException {
        this.connectionCommunicationService.open(connectionURI, content);
    }

    @Override
    public void close(final URI connectionURI, final Model content) throws NoSuchConnectionException, IllegalMessageForConnectionStateException {
        this.connectionCommunicationService.close(connectionURI, content);
    }

    @Override
    public void textMessage(final URI connectionURI, final Model message) throws NoSuchConnectionException, IllegalMessageForConnectionStateException {
        this.connectionCommunicationService.textMessage(connectionURI, message);
    }

    @Override
    public Collection<URI> listNeedURIs() {
        return this.needInformationService.listNeedURIs();
    }

    @Override
    public Collection<URI> listConnectionURIs(final URI needURI) throws NoSuchNeedException {
        return this.needInformationService.listConnectionURIs(needURI);
    }

    @Override
    public Need readNeed(final URI needURI) throws NoSuchNeedException {
        return needInformationService.readNeed(needURI);
    }

    @Override
    public Model readNeedContent(final URI needURI) throws NoSuchNeedException {
        return needInformationService.readNeedContent(needURI);
    }

    @Override
    public Connection readConnection(final URI connectionURI) throws NoSuchConnectionException {
        return needInformationService.readConnection(connectionURI);
    }

    @Override
    public Model readConnectionContent(final URI connectionURI) throws NoSuchConnectionException {
        return needInformationService.readConnectionContent(connectionURI);
    }

  @Override
  public List<ConnectionEvent> readEvents(final URI connectionURI) throws NoSuchConnectionException
  {
    return needInformationService.readEvents(connectionURI);
  }

  public void setNeedCommunicationService(final OwnerFacingNeedCommunicationService needCommunicationService) {
        this.needCommunicationService = needCommunicationService;
    }

    public void setConnectionCommunicationService(final OwnerFacingConnectionCommunicationServiceImpl connectionCommunicationService) {
        this.connectionCommunicationService = connectionCommunicationService;
    }

    public void setNeedManagementService(final NeedManagementService needManagementService) {
        this.needManagementService = needManagementService;
    }

    public void setNeedInformationService(final NeedInformationService needInformationService) {
        this.needInformationService = needInformationService;
    }
}
