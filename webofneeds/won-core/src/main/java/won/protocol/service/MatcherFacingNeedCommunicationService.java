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

package won.protocol.service;

import com.hp.hpl.jena.rdf.model.Model;
import won.protocol.exception.IllegalMessageForNeedStateException;
import won.protocol.exception.NoSuchNeedException;

import java.net.URI;

/**
 * Interface defining methods for need communication.
 */
public interface MatcherFacingNeedCommunicationService
{
  /**
   * Notifies the need of a matching otherNeed with the specified match score. Originator
   * identifies the entity making the call. Normally, originator is a matching service.
   *
   * @param needURI    the URI of the need
   * @param otherNeed  URI of the other need (may be on the local needserver)
   * @param score      match score between 0.0 (bad) and 1.0 (good). Implementations treat lower values as 0.0 and higher values as 1.0.
   * @param originator an URI identifying the calling entity
   * @param content
   * @throws won.protocol.exception.NoSuchNeedException
   *          if needURI is not a known need URI
   * @throws won.protocol.exception.IllegalMessageForNeedStateException
   *          if the need is not active
   */
  public void hint(URI needURI, URI otherNeed, double score, URI originator, Model content) throws NoSuchNeedException, IllegalMessageForNeedStateException;

}
