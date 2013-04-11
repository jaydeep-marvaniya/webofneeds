package won.owner.web.need;

import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import won.owner.pojo.NeedPojo;
import won.owner.protocol.impl.OwnerProtocolNeedServiceClient;
import won.owner.service.impl.URIService;
import won.protocol.exception.*;
import won.protocol.model.*;
import won.protocol.owner.OwnerProtocolNeedService;
import won.protocol.repository.ConnectionRepository;
import won.protocol.repository.MatchRepository;
import won.protocol.repository.NeedRepository;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Gabriel
 * Date: 17.12.12
 * Time: 13:38
 */

@Controller
public class NeedController {
    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private OwnerProtocolNeedService ownerService;

    @Autowired
    private NeedRepository needRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private ConnectionRepository connectionRepository;

    @Autowired
    private URIService uriService;


  public URIService getUriService()
  {
    return uriService;
  }

  public void setUriService(final URIService uriService)
  {
    this.uriService = uriService;
  }

  public void setOwnerService(OwnerProtocolNeedService ownerService) {
        this.ownerService = ownerService;
    }

    public void setConnectionRepository(ConnectionRepository connectionRepository) {
        this.connectionRepository = connectionRepository;
    }

    public void setMatchRepository(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    public void setNeedRepository(NeedRepository needRepository) {
        this.needRepository = needRepository;
    }


    @RequestMapping(value = "/create", method = RequestMethod.GET)
    public String createNeedGet(Model model) {
        model.addAttribute("command", new NeedPojo());
        return "createNeed";
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public String createNeedPost(@ModelAttribute("SpringWeb") NeedPojo needPojo, Model model) {
        URI needURI;

        try {
            URI ownerURI = this.uriService.getOwnerProtocolOwnerServiceEndpointURI();
            com.hp.hpl.jena.rdf.model.Model m = ModelFactory.createDefaultModel();
            // use the FileManager to find the input file
            InputStream in = FileManager.get().open( "/offer.ttl" );
            if (in == null) {
                throw new IllegalArgumentException(
                        "File: offer.ttl not found");
            }
            m.read(in, null, "TTL");

            in.close();
            ResIterator it = m.listSubjectsWithProperty(RDF.type, WON.NEED_DESCRIPTION);
            if (it.hasNext()){
                Resource mainContentNode = it.next();
                m.add(m.createStatement(mainContentNode, WON.TEXT_DESCRIPTION, needPojo.getTextDescription()));
            }

            if(needPojo.getWonNode().equals("")) {
                needURI = ownerService.createNeed(ownerURI, m, needPojo.isActive());
            } else {
                needURI = ((OwnerProtocolNeedServiceClient) ownerService).createNeed(ownerURI, m, needPojo.isActive(), needPojo.getWonNode());
            }

            List<Need> needs = needRepository.findByNeedURI(needURI);

            model.addAttribute("command", new NeedPojo());

            if(needs.size() == 1)
                return "redirect:/need/" + needs.get(0).getId().toString();
            // return viewNeed(need.getId().toString(), model);
        } catch (IllegalNeedContentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return "createNeed";
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String listNeeds(Model model) {

        model.addAttribute("needs", needRepository.findAll());

        return "listNeeds";
    }

    @RequestMapping(value = "reload", method = RequestMethod.GET)
    public String reload(Model model) {

        return "redirect:/need/";
    }

    @RequestMapping(value = "/{needId}", method = RequestMethod.GET)
    public String viewNeed(@PathVariable String needId, Model model) {

        model.addAttribute("needId", needId);

        List<Need> needs = needRepository.findById(Long.valueOf(needId));
        if(needs.isEmpty())
            return "noNeedFound";

        Need need = needs.get(0);
        model.addAttribute("active", (need.getState() != NeedState.ACTIVE ? "activate" : "deactivate"));
        model.addAttribute("needURI", need.getNeedURI());
        model.addAttribute("command", new NeedPojo());

        return "viewNeed";
    }


    @RequestMapping(value = "/{needId}/listMatches", method = RequestMethod.GET)
    public String listMatches(@PathVariable String needId, Model model) {
        List<Need> needs = needRepository.findById(Long.valueOf(needId));
        if(needs.isEmpty())
            return "noNeedFound";

        Need need = needs.get(0);
        model.addAttribute("matches", matchRepository.findByFromNeed(need.getNeedURI()));

        return "listMatches";
    }

    @RequestMapping(value = "/{needId}/listConnections", method = RequestMethod.GET)
    public String listConnections(@PathVariable String needId, Model model) {

        List<Need> needs = needRepository.findById(Long.valueOf(needId));
        if(needs.isEmpty())
            return "noNeedFound";

        Need need = needs.get(0);

        model.addAttribute("connections", connectionRepository.findByNeedURI(need.getNeedURI()));

        return "listConnections";
    }

    @RequestMapping(value = "/{needId}/connect", method = RequestMethod.POST)
    public String connect2Need(@PathVariable String needId, @ModelAttribute("SpringWeb") NeedPojo needPojo, Model model) {
        try {
            List<Need> needs = needRepository.findById(Long.valueOf(needId));
            if(needs.isEmpty())
                return "noNeedFound";

            Need need1 = needs.get(0);
            ownerService.connectTo(need1.getNeedURI(), new URI(needPojo.getNeedURI()), "");
            return "redirect:/need/" + need1.getId().toString();//viewNeed(need1.getId().toString(), model);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (ConnectionAlreadyExistsException e) {
            e.printStackTrace();
        } catch (IllegalMessageForNeedStateException e) {
            e.printStackTrace();
        } catch (NoSuchNeedException e) {
            e.printStackTrace();
        }

        return "noNeedFound";
    }

    @RequestMapping(value = "/{needId}/toggle", method = RequestMethod.POST)
    public String toggleNeed(@PathVariable String needId, Model model) {
        List<Need> needs = needRepository.findById(Long.valueOf(needId));
        if(needs.isEmpty())
            return "noNeedFound";
        Need need = needs.get(0);
        try {
            if(need.getState() == NeedState.ACTIVE) {
                ownerService.deactivate(need.getNeedURI());
            } else {
                ownerService.activate(need.getNeedURI());
            }
        } catch (NoSuchNeedException e) {
            e.printStackTrace();
        }
        return "redirect:/need/" + need.getId().toString();
        //return viewNeed(need.getId().toString(), model);
    }

    @RequestMapping(value = "/match/{matchId}/connect", method = RequestMethod.POST)
    public String connect(@PathVariable String matchId, Model model) {
        String ret = "noNeedFound";

        try {
            List<Match> matches = matchRepository.findById(Long.valueOf(matchId));
            if(!matches.isEmpty()) {
                Match match = matches.get(0);
                List<Need> needs = needRepository.findByNeedURI(match.getFromNeed());
                if(!needs.isEmpty())
                    ret =  "redirect:/need/" + needs.get(0).getId().toString();//viewNeed(needs.get(0).getId().toString(), model);
                ownerService.connectTo(match.getFromNeed(), match.getToNeed(), "");
            }
        } catch (ConnectionAlreadyExistsException e) {
            e.printStackTrace();
        } catch (IllegalMessageForNeedStateException e) {
            e.printStackTrace();
        } catch (NoSuchNeedException e) {
            e.printStackTrace();
        }

        return ret;
    }
}
