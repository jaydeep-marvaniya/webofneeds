/*
 * Copyright 2012  Research Studios Austria Forschungsges.m.b.H.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package won.bot.framework.bot.context;

import won.bot.framework.bot.BotContext;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Straightforward BotContext implementation using a List and a Map.
 */
public class InMemoryBotContext implements BotContext
{
  private List<URI> needUris = new ArrayList<URI>();
  private List<URI> groupUris = new ArrayList<URI>();
  private Map<String,URI> namedNeedUris = new HashMap<String, URI>();
  private Map<URI, Integer> sentMessagesCount = new HashMap<URI,Integer>();
  private Map<URI, Integer> receivedMessagesCount = new HashMap<URI,Integer>();

  @Override
  public List<URI> listNeedUris()
  {
    return needUris;
  }

    @Override
    public List<URI> listGroupUris() {
        return groupUris;
    }


    @Override
    public Map<URI, Integer> getSentMessagesCount() {
        return sentMessagesCount;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Map<URI, Integer> getReceivedMessagesCount() {
        return receivedMessagesCount;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void increaseSentMessagesCount(URI needURI) {

        if (sentMessagesCount.containsKey(needURI)) {
            int count = sentMessagesCount.get(needURI);
            sentMessagesCount.put(needURI, count++);
        }else{
            sentMessagesCount.put(needURI,1);
        }
    }

    @Override
    public void increaseReceivedMessagesCount(URI needURI) {
        if (receivedMessagesCount.containsKey(needURI)) {
            int count = receivedMessagesCount.get(needURI);
            receivedMessagesCount.put(needURI, count++);
        }else{
            receivedMessagesCount.put(needURI, 1);
        }
    }


    @Override
  public boolean isNeedKnown(final URI needURI)
  {
    if (needUris.contains(needURI))
        return needUris.contains(needURI);
    else if (groupUris.contains(needURI))
        return  groupUris.contains(needURI);
    else return false;
  }

  /**
   * Caution, this call is expensive as it uses List.contains(uri).
   * @param uri
   * @param name
   */
  @Override
  public void rememberNeedUriWithName(final URI uri, final String name)
  {
    if (!needUris.contains(uri)){
      needUris.add(uri);
    }
    namedNeedUris.put(name, uri);
  }

  /**
   * @param uri
   */
  @Override
  public void rememberNeedUri(final URI uri)
  {
    needUris.add(uri);
  }

    @Override
    public void rememberGroupUri(URI uri) {
        groupUris.add(uri);
    }

    @Override
    public void forgetNeedUri(URI uri) {
        needUris.remove(uri);
    }

    @Override
    public void forgetGroupUri(URI uri) {
        groupUris.remove(uri);
    }


    @Override
  public URI getNeedByName(final String name)
  {
    return namedNeedUris.get(name);
  }

  @Override
  public List<String> listNeedUriNames()
  {
    List ret = new ArrayList<URI>(namedNeedUris.size());
    ret.addAll(namedNeedUris.keySet());
    return ret;
  }
}