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

package won.node.messaging;

import com.hp.hpl.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import won.node.protocol.impl.OwnerProtocolOwnerClientFactory;
import won.protocol.exception.*;
import won.protocol.jms.MessagingService;
import won.protocol.model.Connection;
import won.protocol.model.Need;
import won.protocol.model.OwnerApplication;
import won.protocol.owner.OwnerProtocolOwnerServiceClientSide;
import won.protocol.repository.ConnectionRepository;
import won.protocol.repository.NeedRepository;
import won.protocol.util.DataAccessUtils;
import won.protocol.util.RdfUtils;
import won.protocol.ws.OwnerProtocolOwnerWebServiceEndpoint;
import won.protocol.ws.fault.*;

import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OwnerProtocolOwnerClientImplJMSBased implements OwnerProtocolOwnerServiceClientSide
{
  final Logger logger = LoggerFactory.getLogger(getClass());


  @Autowired
  private OwnerProtocolOwnerClientFactory clientFactory;

  private MessagingService messagingService;

  @Autowired
  private NeedRepository needRepository;

    @Autowired
    private ConnectionRepository connectionRepository;

    @Override
  public void hint(final URI ownNeedURI, final URI otherNeedURI, final double score, final URI originatorURI, final Model content) throws NoSuchNeedException, IllegalMessageForNeedStateException
  {
    logger.info(MessageFormat.format("owner-facing: HINT_RECEIVED called for own need {0}, other need {1}, with score {2} from originator {3} and content {4}", ownNeedURI, otherNeedURI, score, originatorURI, content));
    try {
      OwnerProtocolOwnerWebServiceEndpoint proxy = clientFactory.getOwnerProtocolEndpointForNeed(ownNeedURI);
      StringWriter sw = new StringWriter();
      content.write(sw, "TTL");
      proxy.hint(ownNeedURI, otherNeedURI, score, originatorURI, sw.toString());
    } catch (MalformedURLException e) {
      logger.warn("couldn't create URL for needProtocolEndpoint", e);
    } catch (NoSuchNeedFault noSuchNeedFault) {
      throw NoSuchNeedFault.toException(noSuchNeedFault);
    } catch (IllegalMessageForNeedStateFault illegalMessageForNeedStateFault) {
      logger.warn("couldn't send hint", illegalMessageForNeedStateFault);
    }
  }

    @Override
    public void connect(final URI ownNeedURI, final URI otherNeedURI, final URI ownConnectionURI, final Model content) throws NoSuchNeedException, ConnectionAlreadyExistsException, IllegalMessageForNeedStateException
    {
        logger.info(MessageFormat.format("owner-facing: CONNECTION_REQUESTED called for own need {0}, other need {1}, own connection {2} and message ''{3}''", ownNeedURI, otherNeedURI, ownConnectionURI, content));
        StringWriter sw = new StringWriter();
        content.write(sw, "TTL");

        URI ownerURI = clientFactory.getOwnerProtocolOwnerURI(ownNeedURI);
        Map headerMap = new HashMap<String, String>();
        List<Need> needs = needRepository.findByNeedURI(ownNeedURI);
        List<Need> needs2 = needRepository.findByNeedURI(otherNeedURI);
        Need need = needs.get(0);
       // Need otherNeed = needs2.get(0);
        List<OwnerApplication> ownerApplications = need.getAuthorizedApplications();

        logger.info(ownerApplications.get(0).getOwnerApplicationId());
        headerMap.put("ownNeedURI", ownNeedURI.toString()) ;
        headerMap.put("otherNeedURI", otherNeedURI.toString());
        headerMap.put("ownConnectionURI", ownConnectionURI.toString()) ;
        headerMap.put("content",RdfUtils.toString(content));
        headerMap.put("ownerURI", ownerURI.toString());
        headerMap.put("ownerApplications", ownerApplications);


        headerMap.put("protocol","OwnerProtocol");
        headerMap.put("methodName", "connect");
        messagingService.sendInOnlyMessage(null,headerMap,null,"outgoingMessages");


    }

    @Override
    public void open(final URI connectionURI, final Model content) throws NoSuchConnectionException, IllegalMessageForConnectionStateException
    {
        logger.info(MessageFormat.format("owner-facing: OPEN called for connection {0}", connectionURI));
        Connection con = DataAccessUtils.loadConnection(connectionRepository, connectionURI);
        URI needURI = con.getNeedURI();
        Need need = needRepository.findByNeedURI(needURI).get(0);
        List<OwnerApplication> ownerApplicationList = need.getAuthorizedApplications();
        Map headerMap = new HashMap<String, String>();
        logger.info(ownerApplicationList.get(0).getOwnerApplicationId());
        headerMap.put("connectionURI", connectionURI.toString()) ;
        headerMap.put("content",RdfUtils.toString(content));
        headerMap.put("ownerApplications", ownerApplicationList);
        headerMap.put("protocol","OwnerProtocol");
        headerMap.put("methodName", "open");
        messagingService.sendInOnlyMessage(null,headerMap,null,"outgoingMessages");
    }
    /*
  @Override
  public void close(final URI connectionURI, final Model content) throws NoSuchConnectionException, IllegalMessageForConnectionStateException
  {
    logger.info(MessageFormat.format("owner-facing: CLOSE called for connection {0}", connectionURI));
    try {
      OwnerProtocolOwnerWebServiceEndpoint proxy = clientFactory.getOwnerProtocolEndpointForConnection(connectionURI);
      proxy.close(connectionURI, RdfUtils.toString(content));
    } catch (MalformedURLException e) {
      logger.warn("couldn't create URL for needProtocolEndpoint", e);
    } catch (NoSuchNeedException e) {
      logger.warn("could not get owner protocol endpoint", e);
    } catch (IllegalMessageForConnectionStateFault illegalMessageForConnectionStateFault) {
      illegalMessageForConnectionStateFault.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    } catch (NoSuchConnectionFault noSuchConnectionFault) {
      noSuchConnectionFault.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
  }      */
    @Override
    public void close(final URI connectionURI, final Model content) throws NoSuchConnectionException, IllegalMessageForConnectionStateException
    {
        logger.info(MessageFormat.format("owner-facing: CLOSE called for connection {0}", connectionURI));
        Connection con = DataAccessUtils.loadConnection(connectionRepository, connectionURI);
        URI needURI = con.getNeedURI();
        Need need = needRepository.findByNeedURI(needURI).get(0);
        List<OwnerApplication> ownerApplicationList = need.getAuthorizedApplications();
        Map headerMap = new HashMap<String, String>();
        logger.info(ownerApplicationList.get(0).getOwnerApplicationId());
        headerMap.put("connectionURI", connectionURI.toString()) ;
        headerMap.put("content",RdfUtils.toString(content));
        headerMap.put("ownerApplications", ownerApplicationList);
        headerMap.put("protocol","OwnerProtocol");
        headerMap.put("methodName", "close");
        messagingService.sendInOnlyMessage(null,headerMap,null,"outgoingMessages");
    }
   /*
  @Override
  public void textMessage(final URI connectionURI, final String message) throws NoSuchConnectionException, IllegalMessageForConnectionStateException
  {
    logger.info(MessageFormat.format("owner-facing: SEND_TEXT_MESSAGE called for connection {0} with message {1}", connectionURI, message));
    try {
      OwnerProtocolOwnerWebServiceEndpoint proxy = clientFactory.getOwnerProtocolEndpointForConnection(connectionURI);
      proxy.textMessage(connectionURI, message);
    } catch (MalformedURLException e) {
      logger.warn("couldn't create URL for needProtocolEndpoint", e);
    } catch (NoSuchNeedException e) {
      logger.warn("could not get owner protocol endpoint", e);
    } catch (IllegalMessageForConnectionStateFault illegalMessageForConnectionStateFault) {
      throw IllegalMessageForConnectionStateFault.toException(illegalMessageForConnectionStateFault);
    } catch (NoSuchConnectionFault noSuchConnectionFault) {
      throw NoSuchConnectionFault.toException(noSuchConnectionFault);
    }
  }   */
    @Override
    public void textMessage(final URI connectionURI, final Model message) throws NoSuchConnectionException, IllegalMessageForConnectionStateException
    {
        logger.info(MessageFormat.format("owner-facing: TEXTMESSAGE_REQUESTED called for connectionURI {0}, message {1}", connectionURI,message));
        String messageConvert = RdfUtils.toString(message);
        Connection con = DataAccessUtils.loadConnection(connectionRepository, connectionURI);
        URI needURI = con.getNeedURI();
        Need need = needRepository.findByNeedURI(needURI).get(0);
        List<OwnerApplication> ownerApplicationList = need.getAuthorizedApplications();
        Map headerMap = new HashMap<String, String>();
        logger.info(ownerApplicationList.get(0).getOwnerApplicationId());
        headerMap.put("connectionURI", connectionURI.toString()) ;
        headerMap.put("message",messageConvert);
        headerMap.put("ownerApplications", ownerApplicationList);
        headerMap.put("protocol","OwnerProtocol");
        headerMap.put("methodName", "textMessage");
        messagingService.sendInOnlyMessage(null,headerMap,null,"outgoingMessages");

    }
  public void setClientFactory(final OwnerProtocolOwnerClientFactory clientFactory)
  {
    this.clientFactory = clientFactory;
  }

    public void setNeedRepository(NeedRepository needRepository) {
        this.needRepository = needRepository;
    }

    public void setMessagingService(MessagingService messagingService) {
        this.messagingService = messagingService;
    }
}