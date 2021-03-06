/**
 * Copyright (C) 2018-2020 toop.eu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.toop.iface;

import java.io.IOException;

import javax.annotation.Nonnull;

import eu.toop.commons.dataexchange.v140.TDETOOPResponseType;
import eu.toop.commons.exchange.ToopResponseWithAttachments140;

/**
 * This interface must be implemented by DC receiving components to retrieve
 * incoming requests (step 4/4). The content of the request is an ASiC archive
 * containing a {@link TDETOOPResponseType} which contains response data and/or
 * errors.
 *
 * @author Anton
 * @author Philip Helger
 */
public interface IToopInterfaceDC
{
  /**
   * Invoked every time a TOOP Response Message is received
   *
   * @param aResponse
   *        Message object. Never <code>null</code>.
   * @throws IOException
   *         in case of processing errors
   */
  void onToopResponse (@Nonnull ToopResponseWithAttachments140 aResponse) throws IOException;
}
