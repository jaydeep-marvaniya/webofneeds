package won.owner.web.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import won.owner.pojo.TextMessagePojo;
import won.protocol.exception.IllegalMessageForConnectionStateException;
import won.protocol.exception.NoSuchConnectionException;
import won.protocol.model.ChatMessage;
import won.protocol.model.Connection;
import won.protocol.model.ConnectionState;
import won.protocol.owner.OwnerProtocolNeedService;
import won.protocol.repository.ChatMessageRepository;
import won.protocol.repository.ConnectionRepository;

import java.util.Date;
import java.util.List;

import static won.protocol.model.ConnectionState.*;

/**
 * Created with IntelliJ IDEA.
 * User: Gabriel
 * Date: 19.12.12
 * Time: 15:19
 */
@Controller
//@RequestMapping("/connection")
public class ConnectionController {
    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private OwnerProtocolNeedService ownerService;

    @Autowired
    private ConnectionRepository connectionRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @RequestMapping(value = "/{conId}", method = RequestMethod.GET)
    public String viewConnection(@PathVariable String conId, Model model) {
        List<Connection> cons = connectionRepository.findById(Long.valueOf(conId));
        if(cons.isEmpty())
            return "noNeedFound";
        Connection con = cons.get(0);
        model.addAttribute("connection", con);
        model.addAttribute("command", new TextMessagePojo());

        return "viewConnection";
    }


    @RequestMapping(value = "/{conId}/body", method = RequestMethod.GET)
    public String listMessages(@PathVariable String conId, Model model) {
        List<Connection> cons = connectionRepository.findById(Long.valueOf(conId));

        if(cons.isEmpty())
            return "noNeedFound";
        Connection con = cons.get(0);
        try {
            switch (con.getState()) {
                case REQUEST_RECEIVED:
                    model.addAttribute("connection", con);
                    return "manageConnection";
                case REQUEST_SENT:
                    model.addAttribute("message", "Pending....");
                    return "showMessage";
                case CLOSED:
                    model.addAttribute("message", "Connection Closed!");
                    return "showMessage";
                case CONNECTED:
                    model.addAttribute("messages", chatMessageRepository.findByLocalConnectionURI(con.getConnectionURI()));
                    return "listMessages";
            }
        } catch (Exception e) {
            logger.warn("error reading connection from won node");
            return "error reading connection from won node: " + e.getMessage();
        }
      return "noNeedFound";
    }

    @RequestMapping(value = "/{conId}/send", method = RequestMethod.POST)
    public String sendText(@PathVariable String conId, @ModelAttribute("SpringWeb") TextMessagePojo text, Model model) {
        List<Connection> cons = connectionRepository.findById(Long.valueOf(conId));
        if(cons.isEmpty())
            return "noNeedFound";
        Connection con = cons.get(0);

        try {
            ownerService.sendTextMessage(con.getConnectionURI(), text.getText());
        } catch (Exception e) {
          logger.warn("error sending text message");
          return "error sending text message: " + e.getMessage();
        }

        return  "redirect:/connection/" + con.getId().toString();//"viewConnection";
    }


    @RequestMapping(value = "/{conId}/accept", method = RequestMethod.POST)
    public String accept(@PathVariable String conId, Model model) {
        List<Connection> cons = connectionRepository.findById(Long.valueOf(conId));
        if(cons.isEmpty())
            return "noNeedFound";
        Connection con = cons.get(0);
        try {
            //TODO: ConnectionURI should be used here instead
            ownerService.connectTo(con.getNeedURI(), con.getRemoteNeedURI(), "");
        } catch (Exception e) {
          logger.warn("error during accept", e);
          return "error during accept: " + e.getMessage();
        }

        return  "redirect:/connection/" + con.getId().toString() + "/body";
    }

    @RequestMapping(value = "/{conId}/deny", method = RequestMethod.POST)
    public String deny(@PathVariable String conId, Model model) {
        List<Connection> cons = connectionRepository.findById(Long.valueOf(conId));
        if(cons.isEmpty())
            return "noNeedFound";
        Connection con = cons.get(0);
        try {
            ownerService.close(con.getConnectionURI());
        } catch (Exception e) {
          logger.warn("error during deny", e);
          return "error during deny: " + e.getMessage();
        }

        return  "redirect:/connection/" + con.getId().toString() + "/body";
    }

    @RequestMapping(value = "/{conId}/close", method = RequestMethod.POST)
    public String close(@PathVariable String conId, Model model) {
        List<Connection> cons = connectionRepository.findById(Long.valueOf(conId));
        if(cons.isEmpty())
            return "noNeedFound";
        Connection con = cons.get(0);
        try {
            ownerService.close(con.getConnectionURI());
        } catch (Exception e) {
          logger.warn("error during close", e);
          return "error during close: " + e.getMessage();
        }

        return  "redirect:/connection/" + con.getId().toString();
    }
}
