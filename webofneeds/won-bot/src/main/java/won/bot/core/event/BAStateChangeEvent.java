package won.bot.core.event;

import com.hp.hpl.jena.rdf.model.Model;
import won.bot.events.Event;
import won.protocol.model.ChatMessage;
import won.protocol.model.Connection;

/**
 * Created with IntelliJ IDEA.
 * User: Danijel
 * Date: 13.2.14.
 * Time: 11.04
 * To change this template use File | Settings | File Templates.
 */
public class BAStateChangeEvent implements Event
{
    private final Connection con;
    private final ChatMessage message;
    private final Model content;

    public BAStateChangeEvent(final Connection con, final ChatMessage message, final Model content)
    {
        this.con = con;
        this.message = message;
        this.content = content;
    }

    public Connection getCon()
    {
        return con;
    }

    public ChatMessage getMessage()
    {
        return message;
    }

    public Model getContent()
    {
        return content;
    }
}
