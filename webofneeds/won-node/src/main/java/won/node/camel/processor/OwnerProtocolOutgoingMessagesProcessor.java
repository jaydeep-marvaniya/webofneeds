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

package won.node.camel.processor;

import org.apache.activemq.network.NetworkConnector;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import won.protocol.model.OwnerApplication;
import won.protocol.repository.OwnerApplicationRepository;
import won.protocol.service.OwnerManagementService;
import won.protocol.service.QueueManagementService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * User: sbyim
 * Date: 13.11.13
 */
public class OwnerProtocolOutgoingMessagesProcessor implements Processor {
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(getClass());

    private QueueManagementService queueManagementService;
    @Autowired
    private OwnerApplicationRepository ownerApplicationRepository;

    @Override
    public void process(Exchange exchange) throws Exception {
        logger.info("processing messages for dynamic recipients generation");
        Map headers = exchange.getIn().getHeaders();
        Map properties = exchange.getProperties();
        List<OwnerApplication> ownerApplications = (List<OwnerApplication>)headers.get("ownerApplications");
        String methodName =headers.get("methodName").toString();
        logger.info(ownerApplications.get(0).getOwnerApplicationId());
        List<String> queueNames = convertToQueueName(ownerApplications,methodName);
        exchange.getIn().setHeader("ownerApplicationIDs",queueNames);
    }
    private List<String> convertToQueueName(List<OwnerApplication> ownerApplications,String methodName){
        List<String> ownerApplicationQueueNames = new ArrayList<>();

        for (int i =0;i<ownerApplications.size();i++){
             OwnerApplication ownerApplication = ownerApplicationRepository.findByOwnerApplicationId(ownerApplications.get(i).getOwnerApplicationId()).get(i);
             logger.debug(queueManagementService.getEndpointsForOwnerApplication(ownerApplication.getOwnerApplicationId()).get(0));
             logger.debug("ownerApplicationID: "+ownerApplications.get(i).getOwnerApplicationId());
             ownerApplicationQueueNames.add(i, queueManagementService.getEndpointForMessage(methodName,ownerApplication.getOwnerApplicationId()));

        }
        return ownerApplicationQueueNames;
    }

    public void setQueueManagementService(QueueManagementService queueManagementService) {
        this.queueManagementService = queueManagementService;
    }

    public void setOwnerApplicationRepository(OwnerApplicationRepository ownerApplicationRepository) {
        this.ownerApplicationRepository = ownerApplicationRepository;
    }
}
