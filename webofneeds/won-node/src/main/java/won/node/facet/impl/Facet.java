package won.node.facet.impl;

import com.google.common.util.concurrent.ListenableFuture;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import won.node.service.impl.DataAccessService;
import won.node.service.impl.NeedFacingConnectionCommunicationServiceImpl;
import won.node.service.impl.OwnerFacingConnectionCommunicationServiceImpl;
import won.protocol.exception.*;
import won.protocol.model.Connection;
import won.protocol.model.FacetType;
import won.protocol.model.Need;
import won.protocol.model.NeedState;
import won.protocol.need.NeedProtocolNeedClientSide;
import won.protocol.owner.OwnerProtocolOwnerServiceClientSide;
import won.protocol.vocabulary.WON;

import java.io.StringWriter;
import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

/**
 * Created with IntelliJ IDEA.
 * User: gabriel
 * Date: 16.09.13
 * Time: 17:09
 * To change this template use File | Settings | File Templates.
 */
public abstract class Facet {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  /**
   * Client talking another need via the need protocol
   */
  protected NeedProtocolNeedClientSide needProtocolNeedService;
  /**
   * Client talking to the owner side via the owner protocol
   */
  protected OwnerProtocolOwnerServiceClientSide ownerProtocolOwnerService;

  /**
   * Client talking to this need service from the need side
   */
  protected NeedFacingConnectionCommunicationServiceImpl needFacingConnectionCommunicationService;
  /**
   * Client talking to this need service from the owner side
   */
  protected OwnerFacingConnectionCommunicationServiceImpl ownerFacingConnectionCommunicationService;

  protected NeedProtocolNeedClientSide needFacingConnectionClient;
  protected OwnerProtocolOwnerServiceClientSide ownerFacingConnectionClient;

  protected won.node.service.impl.URIService URIService;

  protected ExecutorService executorService;

  protected DataAccessService dataService;

  public abstract FacetType getFacetType();

  /**
   *
   * This function is invoked when an owner sends an open message to a won node and usually executes registered facet specific code.
   * It is used to open a connection which is identified by the connection object con. A rdf graph can be sent along with the request.
   *
   * @param con the connection object
   * @param content a rdf graph describing properties of the event. The null releative URI ('<>') inside that graph,
   *                as well as the base URI of the graph will be attached to the resource identifying the event.
   * @throws NoSuchConnectionException if connectionURI does not refer to an existing connection
   * @throws IllegalMessageForConnectionStateException if the message is not allowed in the current state of the connection
   */
  public void openFromOwner(final Connection con, final Model content) throws NoSuchConnectionException, IllegalMessageForConnectionStateException {
    //inform the other side
    if (con.getRemoteConnectionURI() != null) {
      executorService.execute(new Runnable() {
        @Override
        public void run() {
          try {
              needFacingConnectionClient.open(con, content);
            //needFacingConnectionClient.open(con.getRemoteConnectionURI(), content);
          } catch (WonProtocolException e) {
            logger.debug("caught Exception:", e);
          } catch (Exception e) {
             logger.debug("caught Exception",e);
          }
        }
      });
    }
  }

  /**
   *
   * This function is invoked when an owner sends a close message to a won node and usually executes registered facet specific code.
   *It is used to close a connection which is identified by the connection object con. A rdf graph can be sent along with the request.
   *
   * @param con the connection object
   * @param content a rdf graph describing properties of the event. The null releative URI ('<>') inside that graph,
   *                as well as the base URI of the graph will be attached to the resource identifying the event.
   * @throws NoSuchConnectionException if connectionURI does not refer to an existing connection
   * @throws IllegalMessageForConnectionStateException if the message is not allowed in the current state of the connection
   */
  public void closeFromOwner(final Connection con, final Model content) throws NoSuchConnectionException, IllegalMessageForConnectionStateException {
    //inform the other side
    if (con.getRemoteConnectionURI() != null) {
      executorService.execute(new Runnable()
      {
        @Override
        public void run()
        {
          try {
              needFacingConnectionClient.close(con, content);
              //needFacingConnectionClient.close(con.getRemoteConnectionURI(), content);
          } catch (WonProtocolException e) {
            logger.warn("caught WonProtocolException:", e);
          } catch (Exception e) {
              logger.warn("caught Exception: ",e);
          }
        }
      });
    }
  }

  /**
   * This function is invoked when an owner sends a text message to a won node and usually executes registered facet specific code.
   * It is used to indicate the sending of a chat message with by the specified connection object con
   * to the remote partner.
   *
   * @param con the connection object
   * @param message  the chat message
   * @throws NoSuchConnectionException if connectionURI does not refer to an existing connection
   * @throws IllegalMessageForConnectionStateException if the message is not allowed in the current state of the connection
   */
  public void textMessageFromOwner(final Connection con, final Model message) throws NoSuchConnectionException, IllegalMessageForConnectionStateException {
    final URI remoteConnectionURI = con.getRemoteConnectionURI();
    //inform the other side
    executorService.execute(new Runnable() {
      @Override
      public void run() {
        try {
            needFacingConnectionClient.textMessage(con, message);
         // needFacingConnectionClient.textMessage(remoteConnectionURI, message);
        } catch (WonProtocolException e) {
          logger.warn("caught WonProtocolException:", e);
        } catch (Exception e) {
            logger.warn("caught Exception: ",e);
        }
      }
    });
  }

  /**
   *
   * This function is invoked when an won node sends an open message to another won node and usually executes registered facet specific code.
   * It is used to open a connection which is identified by the connection object con. A rdf graph can be sent along with the request.
   *
   * @param con the connection object
   * @param content a rdf graph describing properties of the event. The null releative URI ('<>') inside that graph,
   *                as well as the base URI of the graph will be attached to the resource identifying the event.
   * @throws NoSuchConnectionException if connectionURI does not refer to an existing connection
   * @throws IllegalMessageForConnectionStateException if the message is not allowed in the current state of the connection
   */
  public void openFromNeed(final Connection con, final Model content) throws NoSuchConnectionException, IllegalMessageForConnectionStateException {
    //inform the need side
    executorService.execute(new Runnable()
    {
      @Override
      public void run()
      {
        try {
          ownerFacingConnectionClient.open(con.getConnectionURI(), content);
        } catch (WonProtocolException e) {
          logger.debug("caught Exception:", e);
        }
      }
    });
  }

  /**
   *
   * This function is invoked when an won node sends a close message to another won node and usually executes registered facet specific code.
   * It is used to close a connection which is identified by the connection object con. A rdf graph can be sent along with the request.
   *
   * @param con the connection object
   * @param content a rdf graph describing properties of the event. The null releative URI ('<>') inside that graph,
   *                as well as the base URI of the graph will be attached to the resource identifying the event.
   * @throws NoSuchConnectionException if connectionURI does not refer to an existing connection
   * @throws IllegalMessageForConnectionStateException if the message is not allowed in the current state of the connection
   */
  public void closeFromNeed(final Connection con, final Model content) throws NoSuchConnectionException, IllegalMessageForConnectionStateException {
    //inform the need side
    executorService.execute(new Runnable()
    {
      @Override
      public void run()
      {
        try {
          ownerFacingConnectionClient.close(con.getConnectionURI(), content);
        } catch (WonProtocolException e) {
          logger.warn("caught WonProtocolException:", e);
        }
      }
    });
  }

  /**
   * This function is invoked when a won node sends a text message to another won node and usually executes registered facet specific code.
   * It is used to indicate the sending of a chat message with by the specified connection object con
   * to the remote partner.
   *
   * @param con the connection object
   * @param message  the chat message
   * @throws NoSuchConnectionException if connectionURI does not refer to an existing connection
   * @throws IllegalMessageForConnectionStateException if the message is not allowed in the current state of the connection
   */
  public void textMessageFromNeed(final Connection con, final Model message) throws NoSuchConnectionException, IllegalMessageForConnectionStateException {
    //send to the need side
    executorService.execute(new Runnable() {
      @Override
      public void run() {
        try {
          ownerFacingConnectionClient.textMessage(con.getConnectionURI(), message);
        } catch (WonProtocolException e) {
          logger.warn("caught WonProtocolException:", e);
        }
      }
    });
  }
  /**
   * This function is invoked when a matcher sends a hint message to a won node and
   * usually executes registered facet specific code.
   * It notifies the need of a matching otherNeed with the specified match score. Originator
   * identifies the entity making the call. Normally, originator is a matching service.
   * A rdf graph can be sent along with the request.
   *
   * @param con the connection object
   * @param score      match score between 0.0 (bad) and 1.0 (good). Implementations treat lower values as 0.0 and higher values as 1.0.
   * @param originator an URI identifying the calling entity
   * @param content (optional) an optional RDF graph containing more detailed information about the hint. The null releative URI ('<>') inside that graph,
   *                as well as the base URI of the graph will be attached to the resource identifying the match event.
   * @throws won.protocol.exception.NoSuchNeedException
   *          if needURI is not a known need URI
   * @throws won.protocol.exception.IllegalMessageForNeedStateException
   *          if the need is not active
   */
  public void hint(final Connection con, final double score, final URI originator, final Model content)
      throws NoSuchNeedException, IllegalMessageForNeedStateException {

    final Model remoteFacetModel = changeHasRemoteFacetToHasFacet(content);

    executorService.execute(new Runnable() {
      @Override
      public void run() {
        //here, we don't really need to handle exceptions, as we don't want to flood matching services with error messages
        try {
          ownerProtocolOwnerService.hint(con.getNeedURI(), con.getRemoteNeedURI(), score, originator, remoteFacetModel);
        } catch (NoSuchNeedException e) {
          logger.warn("error sending hint message to owner - no such need:", e);
        } catch (IllegalMessageForNeedStateException e) {
          logger.warn("error sending hint content to owner - illegal need state:", e);
        } catch (Exception e) {
          logger.warn("error sending hint content to owner:", e);
        }
      }
    });
  }

  /**
   *
   * This function is invoked when an won node sends an connect message to another won node and usually executes registered facet specific code.
   * The connection is identified by the connection object con. A rdf graph can be sent along with the request.
   *
   * @param con the connection object
   * @param content a rdf graph describing properties of the event. The null releative URI ('<>') inside that graph,
   *                as well as the base URI of the graph will be attached to the resource identifying the event.
   * @throws NoSuchConnectionException if connectionURI does not refer to an existing connection
   * @throws IllegalMessageForConnectionStateException if the message is not allowed in the current state of the connection
   */
  public void connectFromNeed(final Connection con, final Model content) throws NoSuchNeedException, IllegalMessageForNeedStateException, ConnectionAlreadyExistsException {


    final Connection connectionForRunnable = con;
    executorService.execute(new Runnable() {
      @Override
      public void run() {
        try {
          ownerProtocolOwnerService.connect(con.getNeedURI(), con.getRemoteNeedURI(), connectionForRunnable.getConnectionURI(), content);
        } catch (WonProtocolException e) {
          // we can't connect the connection. we send a deny back to the owner
          // TODO should we introduce a new protocol method connectionFailed (because it's not an owner deny but some protocol-level error)?
          // For now, we call the close method as if it had been called from the owner side
          // TODO: even with this workaround, it would be good to send a content along with the close (so we can explain what happened).
          try {
            ownerFacingConnectionCommunicationService.close(connectionForRunnable.getConnectionURI(), content);
          } catch (NoSuchConnectionException e1) {
            logger.warn("caught NoSuchConnectionException:", e1);
          } catch (IllegalMessageForConnectionStateException e1) {
            logger.warn("caught IllegalMessageForConnectionStateException:", e1);
          }
        }
      }
    });
  }

  /**
   *
   * This function is invoked when an owner sends an open message to the won node and usually executes registered facet specific code.
   * The connection is identified by the connection object con. A rdf graph can be sent along with the request.
   *
   * @param con the connection object
   * @param content a rdf graph describing properties of the event. The null releative URI ('<>') inside that graph,
   *                as well as the base URI of the graph will be attached to the resource identifying the event.
   * @throws NoSuchConnectionException if connectionURI does not refer to an existing connection
   * @throws IllegalMessageForConnectionStateException if the message is not allowed in the current state of the connection
   */
  public void connectFromOwner(final Connection con, final Model content) throws NoSuchNeedException, IllegalMessageForNeedStateException, ConnectionAlreadyExistsException {

    final Model remoteFacetModel = changeHasRemoteFacetToHasFacet(content);
    final Connection connectionForRunnable = con;
    //send to need
    executorService.execute(new Runnable() {
      @Override
      public void run() {
        try {
          ListenableFuture<URI> remoteConnectionURI = needProtocolNeedService.connect(con.getRemoteNeedURI(),con.getNeedURI(), connectionForRunnable.getConnectionURI(), remoteFacetModel);
          dataService.updateRemoteConnectionURI(con, remoteConnectionURI.get());
        } catch (WonProtocolException e) {
          // we can't connect the connection. we send a close back to the owner
          // TODO should we introduce a new protocol method connectionFailed (because it's not an owner deny but some protocol-level error)?
          // For now, we call the close method as if it had been called from the remote side
          // TODO: even with this workaround, it would be good to send a content along with the close (so we can explain what happened).
          try {
            needFacingConnectionCommunicationService.close(connectionForRunnable.getConnectionURI(), content);
          } catch (NoSuchConnectionException e1) {
            logger.warn("caught NoSuchConnectionException:", e1);
          } catch (IllegalMessageForConnectionStateException e1) {
            logger.warn("caught IllegalMessageForConnectionStateException:", e1);
          }
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ExecutionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (Exception e) {
            logger.warn("caught Excpetion: ",e);
        }
      }
    });

  }

  /**
   * Creates a copy of the specified model, replacing won:hasRemoteFacet by won:hasFacet and vice versa.
   * @param model
   * @return
   */
  private Model changeHasRemoteFacetToHasFacet(Model model) {
    Resource baseRes = model.getResource(model.getNsPrefixURI(""));

    StmtIterator stmtIterator = baseRes.listProperties(WON.HAS_REMOTE_FACET);
    if (!stmtIterator.hasNext())
      throw new IllegalArgumentException("at least one facet must be specified with won:hasRemoteFacet");


    final Model newModel = ModelFactory.createDefaultModel();
    newModel.setNsPrefix("", model.getNsPrefixURI(""));
    newModel.add(model);
    newModel.removeAll(null, WON.HAS_REMOTE_FACET, null);
    newModel.removeAll(null, WON.HAS_FACET, null);
    Resource newBaseRes = newModel.createResource(newModel.getNsPrefixURI(""));
    //replace won:hasFacet
    while (stmtIterator.hasNext()) {
      Resource facet = stmtIterator.nextStatement().getObject().asResource();
      newBaseRes.addProperty(WON.HAS_FACET, facet);
    }
    //replace won:hasRemoteFacet
    stmtIterator = baseRes.listProperties(WON.HAS_FACET);
    if (stmtIterator != null) {
        while (stmtIterator.hasNext()) {
          Resource facet = stmtIterator.nextStatement().getObject().asResource();
          newBaseRes.addProperty(WON.HAS_REMOTE_FACET, facet);
        }
    }
    if (logger.isDebugEnabled()){
      StringWriter modelAsString = new StringWriter();
      RDFDataMgr.write(modelAsString, model, Lang.TTL);
      StringWriter newModelAsString = new StringWriter();
      RDFDataMgr.write(newModelAsString, model, Lang.TTL);
      logger.debug("changed hasRemoteFacet to hasFacet. Old: \n{},\n new: \n{}",modelAsString.toString(), newModelAsString.toString());
    }
    return newModel;

  }

  private boolean isNeedActive(final Need need) {
    return NeedState.ACTIVE == need.getState();
  }

  public void setOwnerFacingConnectionClient(OwnerProtocolOwnerServiceClientSide ownerFacingConnectionClient) {
    this.ownerFacingConnectionClient = ownerFacingConnectionClient;
  }

  public void setDataService(DataAccessService dataService) {
    this.dataService = dataService;
  }

  public void setExecutorService(ExecutorService executorService) {
    this.executorService = executorService;
  }

  public void setURIService(won.node.service.impl.URIService URIService) {
    this.URIService = URIService;
  }

  public void setNeedFacingConnectionClient(NeedProtocolNeedClientSide needFacingConnectionClient) {
    this.needFacingConnectionClient = needFacingConnectionClient;
  }

  public void setOwnerFacingConnectionCommunicationService(OwnerFacingConnectionCommunicationServiceImpl ownerFacingConnectionCommunicationService) {
    this.ownerFacingConnectionCommunicationService = ownerFacingConnectionCommunicationService;
  }

  public void setNeedFacingConnectionCommunicationService(NeedFacingConnectionCommunicationServiceImpl needFacingConnectionCommunicationService) {
    this.needFacingConnectionCommunicationService = needFacingConnectionCommunicationService;
  }

  public void setNeedProtocolNeedService(NeedProtocolNeedClientSide needProtocolNeedServiceClient) {
    this.needProtocolNeedService = needProtocolNeedServiceClient;
  }

  public void setOwnerProtocolOwnerService(OwnerProtocolOwnerServiceClientSide ownerProtocolOwnerService) {
    this.ownerProtocolOwnerService = ownerProtocolOwnerService;
  }
}
