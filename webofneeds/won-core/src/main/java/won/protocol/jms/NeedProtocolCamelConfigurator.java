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

package won.protocol.jms;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import won.protocol.exception.CamelConfigurationFailedException;

import java.net.URI;
import java.util.List;

/**
 * User: LEIH-NB
 * Date: 24.02.14
 */
public interface NeedProtocolCamelConfigurator extends CamelConfigurator {

    public String configureCamelEndpointForNeedUri(URI brokerUri, String needProtocolQueueName);
    public void addCamelComponentForWonNodeBroker(URI brokerUri,String brokerComponentName);

    void addRouteForEndpoint(String startingEndpoint, URI wonNodeURI) throws CamelConfigurationFailedException;

    void setCamelContext(CamelContext camelContext);

    @Override
    CamelContext getCamelContext();

    String getEndpoint(URI wonNodeUri);

    public String getBrokerComponentNameWithBrokerUri(URI brokerUri);
}
