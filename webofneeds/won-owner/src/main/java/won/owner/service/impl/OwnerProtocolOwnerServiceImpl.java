package won.owner.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import won.protocol.exception.*;
import won.protocol.model.*;
import won.protocol.owner.OwnerProtocolNeedService;
import won.protocol.owner.OwnerProtocolOwnerService;
import won.protocol.repository.ChatMessageRepository;
import won.protocol.repository.ConnectionRepository;
import won.protocol.repository.MatchRepository;
import won.protocol.repository.NeedRepository;
import won.protocol.service.ConnectionCommunicationService;
import won.protocol.service.OwnerFacingNeedCommunicationService;
import org.springframework.util.*;
import won.protocol.util.DataAccessUtils;

import java.net.URI;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Gabriel
 * Date: 03.12.12
 * Time: 14:12
 */
public class OwnerProtocolOwnerServiceImpl implements OwnerProtocolOwnerService {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private NeedRepository needRepository;

    @Autowired
    private ConnectionRepository connectionRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private OwnerProtocolNeedService ownerService;

    @Override
    public void hintReceived(final URI ownNeedURI, final URI otherNeedURI, final double score, final URI originatorURI) throws NoSuchNeedException, IllegalMessageForNeedStateException {
        logger.info(MessageFormat.format("node-facing: HINT_RECEIVED called for own need {0}, other need {1}, with score {2} from originator {3}", ObjectUtils.nullSafeToString(new Object[]{ownNeedURI, otherNeedURI, score, originatorURI})));

        if (ownNeedURI == null) throw new IllegalArgumentException("needURI is not set");
        if (otherNeedURI == null) throw new IllegalArgumentException("otherNeedURI is not set");
        if (score < 0 || score > 1) throw new IllegalArgumentException("score is not in [0,1]");
        if (originatorURI == null) throw new IllegalArgumentException("originator is not set");
        if (ownNeedURI.equals(otherNeedURI)) throw new IllegalArgumentException("needURI and otherNeedURI are the same");


        //Load need (throws exception if not found)
        Need need = DataAccessUtils.loadNeed(needRepository, ownNeedURI);
        if (! isNeedActive(need)) throw new IllegalMessageForNeedStateException(ownNeedURI, NeedMessage.HINT.name(), need.getState());

        //save match
        Match match = new Match();
        match.setFromNeed(ownNeedURI);
        match.setToNeed(otherNeedURI);
        match.setScore(score);
        match.setOriginator(originatorURI);
        matchRepository.saveAndFlush(match);
    }

    private boolean isNeedActive(final Need need) {
        return NeedState.ACTIVE == need.getState();
    }

    @Override
    public void connectionRequested(final URI ownNeedURI, final URI otherNeedURI, final URI ownConnectionURI,
                                    final String message) throws NoSuchNeedException, ConnectionAlreadyExistsException, IllegalMessageForNeedStateException
    {
        logger.info(MessageFormat.format("node-facing: CONNECTION_REQUESTED called for own need {0}, other need {1}, own connection {2} and message ''{3}''", ownNeedURI,otherNeedURI,ownConnectionURI,message));
        if (ownNeedURI == null) throw new IllegalArgumentException("needURI is not set");
        if (otherNeedURI == null) throw new IllegalArgumentException("otherNeedURI is not set");
        if (ownConnectionURI == null) throw new IllegalArgumentException("otherConnectionURI is not set");
        if (ownNeedURI.equals(otherNeedURI)) throw new IllegalArgumentException("needURI and otherNeedURI are the same");

        //Load need (throws exception if not found)
        Need need = DataAccessUtils.loadNeed(needRepository,ownNeedURI);
        if (! isNeedActive(need)) throw new IllegalMessageForNeedStateException(ownNeedURI, NeedMessage.CONNECTION_REQUESTED.name(), need.getState());
        //Create new connection object on our side

        Connection con = new Connection();
        con.setNeedURI(ownNeedURI);
        con.setState(ConnectionState.REQUEST_RECEIVED);
        con.setRemoteNeedURI(otherNeedURI);

        //TODO problem: remote connection URI not available here! Do we need it? Do we adapt the interface? (using core interface - we could split it)
        //con.setRemoteConnectionURI(otherConnectionURI);

        //set new uri
        con.setConnectionURI(ownConnectionURI);
        connectionRepository.saveAndFlush(con);
    }

    @Override
    public void accept(final URI connectionURI) throws NoSuchConnectionException, IllegalMessageForConnectionStateException
    {
        logger.info(MessageFormat.format("node-facing: ACCEPT called for connection {0}",connectionURI));
        if (connectionURI == null) throw new IllegalArgumentException("connectionURI is not set");

        //load connection, checking if it exists
        Connection con = DataAccessUtils.loadConnection(connectionRepository, connectionURI);
        //set new state and save in the db
        con.setState(con.getState().transit(ConnectionMessage.OWNER_ACCEPT));
        //save in the db
        connectionRepository.saveAndFlush(con);
    }

    @Override
    public void deny(final URI connectionURI) throws NoSuchConnectionException, IllegalMessageForConnectionStateException
    {
        logger.info(MessageFormat.format("node-facing: DENY called for connection {0}",connectionURI));
        if (connectionURI == null) throw new IllegalArgumentException("connectionURI is not set");

        //load connection, checking if it exists
        Connection con = DataAccessUtils.loadConnection(connectionRepository, connectionURI);
        //set new state and save in the db
        con.setState(con.getState().transit(ConnectionMessage.OWNER_DENY));
        //save in the db
        connectionRepository.saveAndFlush(con);
    }

    @Override
    public void close(final URI connectionURI) throws NoSuchConnectionException, IllegalMessageForConnectionStateException
    {
        logger.info(MessageFormat.format("node-facing: CLOSE called for connection {0}",connectionURI));
        if (connectionURI == null) throw new IllegalArgumentException("connectionURI is not set");

        //load connection, checking if it exists
        Connection con = DataAccessUtils.loadConnection(connectionRepository, connectionURI);
        //set new state and save in the db
        con.setState(con.getState().transit(ConnectionMessage.OWNER_CLOSE));
        //save in the db
        connectionRepository.saveAndFlush(con);
    }

    @Override
    public void sendTextMessage(final URI connectionURI, final String message) throws NoSuchConnectionException, IllegalMessageForConnectionStateException
    {
        logger.info(MessageFormat.format("node-facing: SEND_TEXT_MESSAGE called for connection {0} with message {1}",connectionURI, message));
        if (connectionURI == null) throw new IllegalArgumentException("connectionURI is not set");
        if (message == null) throw new IllegalArgumentException("message is not set");
        //load connection, checking if it exists
        Connection con = DataAccessUtils.loadConnection(connectionRepository, connectionURI);

        //perform state transit (should not result in state change)
        //ConnectionState nextState = performStateTransit(con, ConnectionMessage.OWNER_MESSAGE);
        //construct chatMessage object to store in the db
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setCreationDate(new Date());
        chatMessage.setLocalConnectionURI(con.getConnectionURI());
        chatMessage.setMessage(message);
        chatMessage.setOriginatorURI(con.getRemoteNeedURI());
        //save in the db
        chatMessageRepository.saveAndFlush(chatMessage);
    }

    public void setOwnerService(OwnerProtocolNeedService ownerService) {
        this.ownerService = ownerService;
    }
}
