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

package won.protocol.ws.fault;

import won.protocol.exception.NoSuchConnectionException;
import won.protocol.ws.fault.info.NoSuchConnectionFaultInfo;

import javax.xml.ws.WebFault;

/**
 * User: fkleedorfer
 * Date: 02.11.12
 */
@WebFault(faultBean="NoSuchConnectionFaultInfo")
public class NoSuchConnectionFault extends WonProtocolFault
{
  private NoSuchConnectionFaultInfo faultInfo;

  public NoSuchConnectionFault(final String message)
  {
    super(message);
  }

  public NoSuchConnectionFault(final String message, final NoSuchConnectionFaultInfo faultInfo)
  {
    super(message);
    this.faultInfo = faultInfo;
  }

  public NoSuchConnectionFault(final String message, final NoSuchConnectionFaultInfo faultInfo, final Throwable cause)
  {
    super(message, cause);
    this.faultInfo = faultInfo;
  }

  public static NoSuchConnectionException toException(NoSuchConnectionFault fault){
    NoSuchConnectionFaultInfo faultInfo = fault.getFaultInfo();
    return new NoSuchConnectionException(faultInfo.getUnknownConnectionURI());
  }

  public static NoSuchConnectionFault fromException(NoSuchConnectionException e) {
    NoSuchConnectionFaultInfo faultInfo = new NoSuchConnectionFaultInfo();
    faultInfo.setUnknownConnectionURI(e.getUnknownConnectionURI());
    return new NoSuchConnectionFault(e.getMessage(), faultInfo, e.getCause());
  }


  public NoSuchConnectionFaultInfo getFaultInfo()
  {
    return faultInfo;
  }

}
