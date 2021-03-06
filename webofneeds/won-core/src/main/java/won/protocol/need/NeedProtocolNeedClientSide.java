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

package won.protocol.need;

import com.google.common.util.concurrent.ListenableFuture;
import com.hp.hpl.jena.rdf.model.Model;
import won.protocol.exception.*;
import won.protocol.model.Connection;

import java.net.URI;

/**
 * User: fkleedorfer
 * Date: 28.11.12
 */
public interface NeedProtocolNeedClientSide   //extends ConnectionCommunicationService
{

  public ListenableFuture<URI> connect(final URI needUri, final URI otherNeedUri, final URI otherConnectionUri, final Model content) throws Exception;


  public void open(final Connection connection, final Model content) throws Exception;

  public void close(final Connection connection, final Model content) throws Exception;

  public void textMessage(final Connection connection, final Model message) throws Exception;



}