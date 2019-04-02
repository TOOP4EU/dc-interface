/**
 * Copyright (C) 2018-2019 toop.eu
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import com.helger.asic.SignatureHelper;
import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.io.stream.NonBlockingByteArrayOutputStream;

import com.helger.commons.io.stream.StreamHelper;
import eu.toop.commons.codelist.EPredefinedDocumentTypeIdentifier;
import eu.toop.commons.codelist.EPredefinedProcessIdentifier;
import eu.toop.commons.concept.ConceptValue;
import eu.toop.commons.dataexchange.v140.TDEDataRequestSubjectType;
import eu.toop.commons.dataexchange.v140.TDETOOPRequestType;
import eu.toop.commons.dataexchange.v140.TDETOOPResponseType;
import eu.toop.commons.error.ToopErrorException;
import eu.toop.commons.exchange.ToopMessageBuilder140;
import eu.toop.iface.dpsearch.ResultListType;
import eu.toop.iface.util.HttpClientInvoker;
import eu.toop.iface.util.JaxbMarshaller;
import oasis.names.specification.ubl.schema.xsd.unqualifieddatatypes_21.IdentifierType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ThreadSafe
public final class ToopInterfaceClient
{
  private static final Logger LOGGER = LoggerFactory.getLogger (ToopInterfaceClient.class);

  private ToopInterfaceClient ()
  {}

  /**
   * Execute step 1/4
   *
   * @param aRequestSubject
   *        Data request subject. May not be <code>null</code>.
   * @param sDCCountryCode
   *        DC country code as e.g. "SE"
   * @param sDPCountryCode
   *        DP country code as e.g. "SE"
   * @param aSenderParticipantID
   *        Participant ID of the sender as used by R2D2. May not be
   *        <code>null</code>.
   * @param eDocumentTypeID
   *        Document type ID to request. May not be <code>null</code>.
   * @param eProcessID
   *        Process ID to request. May not be <code>null</code>.
   * @param aConceptList
   *        list of concepts to be queried
   * @throws IOException
   *         in case of HTTP error
   * @throws ToopErrorException
   *         For known TOOP errors
   */
  @Deprecated
  public static void createRequestAndSendToToopConnector (@Nonnull final TDEDataRequestSubjectType aRequestSubject,
                                                          @Nonnull final String sDCCountryCode,
                                                          @Nonnull final String sDPCountryCode,
                                                          @Nonnull @Nonempty final IdentifierType aSenderParticipantID,
                                                          @Nonnull final EPredefinedDocumentTypeIdentifier eDocumentTypeID,
                                                          @Nonnull final EPredefinedProcessIdentifier eProcessID,
                                                          @Nullable final List <? extends ConceptValue> aConceptList) throws IOException,
                                                                                                                      ToopErrorException
  {
    // TODO this is still mock!
    final TDETOOPRequestType aRequest = ToopMessageBuilder140.createMockRequest (aRequestSubject,
                                                                              sDCCountryCode,
                                                                              sDPCountryCode,
                                                                              aSenderParticipantID,
                                                                              eDocumentTypeID,
                                                                              eProcessID,
                                                                              aConceptList);
    sendRequestToToopConnector (aRequest);
  }

  /**
   * Create a request, wrap it in an ASiC and send it to DP TOOP Connector, using
   * the configured connector URL.
   *
   * @param aRequest
   *        Request object. May not be <code>null</code>.
   * @throws IOException
   *         In case sending or the like fails
   * @throws ToopErrorException
   *         For known TOOP errors
   * @since 0.9.2
   */
  public static void sendRequestToToopConnector (@Nonnull final TDETOOPRequestType aRequest) throws IOException,
                                                                                             ToopErrorException
  {
    sendRequestToToopConnector (aRequest, ToopInterfaceConfig.getToopConnectorDCUrl ());
  }

  /**
   * Create a request, wrap it in an ASiC and send it to DP TOOP Connector, using
   * the provided URL.
   *
   * @param aRequest
   *        Request object. May not be <code>null</code>.
   * @param sTargetURL
   *        Target URL. May not be <code>null</code>.
   * @throws IOException
   *         In case sending or the like fails
   * @throws ToopErrorException
   *         For known TOOP errors
   * @since 0.10.0
   */
  public static void sendRequestToToopConnector (@Nonnull final TDETOOPRequestType aRequest,
                                                 @Nonnull final String sTargetURL) throws IOException,
                                                                                   ToopErrorException
  {
    ValueEnforcer.notNull (aRequest, "Request");
    ValueEnforcer.notNull (sTargetURL, "TargetURL");

    final SignatureHelper aSH = new SignatureHelper (ToopInterfaceConfig.getKeystoreType (),
                                                     ToopInterfaceConfig.getKeystorePath (),
                                                     ToopInterfaceConfig.getKeystorePassword (),
                                                     ToopInterfaceConfig.getKeystoreKeyAlias (),
                                                     ToopInterfaceConfig.getKeystoreKeyPassword ());

    try (final NonBlockingByteArrayOutputStream aBAOS = new NonBlockingByteArrayOutputStream ())
    {
      ToopMessageBuilder140.createRequestMessageAsic (aRequest, aBAOS, aSH);

      // Send to DC (see FromDCServlet in toop-connector-webapp)
      HttpClientInvoker.httpClientCallNoResponse (sTargetURL, aBAOS.toByteArray ());
    }
  }

  /**
   * Create a response, wrap it in an ASiC and send it to DP TOOP Connector, using
   * the configured connector URL.
   *
   * @param aResponse
   *        Response object. May not be <code>null</code>.
   * @throws IOException
   *         In case sending or the like fails
   * @throws ToopErrorException
   *         For known TOOP errors
   */
  public static void sendResponseToToopConnector (@Nonnull final TDETOOPResponseType aResponse) throws IOException,
                                                                                                ToopErrorException
  {
    sendResponseToToopConnector (aResponse, ToopInterfaceConfig.getToopConnectorDPUrl ());
  }

  /**
   * Create a response, wrap it in an ASiC and send it to DP TOOP Connector, using
   * the provided URL.
   *
   * @param aResponse
   *        Response object. May not be <code>null</code>.
   * @param sTargetURL
   *        Target URL. May not be <code>null</code>.
   * @throws IOException
   *         In case sending or the like fails
   * @throws ToopErrorException
   *         For known TOOP errors
   * @since 0.10.0
   */
  public static void sendResponseToToopConnector (@Nonnull final TDETOOPResponseType aResponse,
                                                  @Nonnull final String sTargetURL) throws IOException,
                                                                                    ToopErrorException
  {
    ValueEnforcer.notNull (aResponse, "Response");
    ValueEnforcer.notNull (sTargetURL, "TargetURL");

    final SignatureHelper aSH = new SignatureHelper (ToopInterfaceConfig.getKeystoreType (),
                                                     ToopInterfaceConfig.getKeystorePath (),
                                                     ToopInterfaceConfig.getKeystorePassword (),
                                                     ToopInterfaceConfig.getKeystoreKeyAlias (),
                                                     ToopInterfaceConfig.getKeystoreKeyPassword ());

    try (final NonBlockingByteArrayOutputStream aBAOS = new NonBlockingByteArrayOutputStream ())
    {
      ToopMessageBuilder140.createResponseMessageAsic (aResponse, aBAOS, aSH);

      // Send to DP (see FromDPServlet in toop-connector-webapp)
      HttpClientInvoker.httpClientCallNoResponse (sTargetURL, aBAOS.toByteArray ());
    }
  }

  public static ResultListType searchDataProvider (@Nonnull final String countryStr,
                                                   @Nullable final String docTypeStr) {

    ValueEnforcer.notNull (countryStr, "CountryStr");

    final StringBuilder query = new StringBuilder(ToopInterfaceConfig.getToopConnectorUrl () + "/search-dp/" + countryStr);
    if (docTypeStr != null && !docTypeStr.isEmpty()) {
      query.append("/").append(docTypeStr);
    }

    try {
      final HttpURLConnection urlConnection = (HttpURLConnection) new URL (query.toString()).openConnection ();
      if (urlConnection.getResponseCode () != HttpURLConnection.HTTP_OK) {
        throw new IllegalStateException ("HTTP status error " + urlConnection.getResponseCode ());
      }

      final byte[] allBytes = StreamHelper.getAllBytes (urlConnection.getInputStream ());
      if (allBytes != null) {
        return JaxbMarshaller.jaxbUnmarshalFromString (new String (allBytes, StandardCharsets.UTF_8));
      }
    } catch (Exception e) {
      throw new IllegalStateException (e);
    }

    return null;
  }
}
