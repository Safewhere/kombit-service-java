/*
 * The contents of this file are subject to the Mozilla Public 
 * License Version 1.1 (the "License"); you may not use this 
 * file except in compliance with the License. You may obtain 
 * a copy of the License at http://www.mozilla.org/MPL/
 * 
 * Software distributed under the License is distributed on an 
 * "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express 
 * or implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 *
 * The Original Code is OIOSAML Java Service Provider.
 * 
 * The Initial Developer of the Original Code is Trifork A/S. Portions 
 * created by Trifork A/S are Copyright (C) 2008 Danish National IT 
 * and Telecom Agency (http://www.itst.dk). All Rights Reserved.
 * 
 * Contributor(s):
 *   Joakim Recht <jre@trifork.com>
 *   Rolf Njor Jensen <rolf@trifork.com>
 *
 */
package dk.itst.oiosaml.sp.service.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import org.apache.log4j.Logger;
import org.apache.commons.io.IOUtils;
import org.opensaml.ws.soap.soap11.Envelope;
import org.opensaml.ws.soap.soap11.Fault;
import org.opensaml.ws.soap.util.SOAPConstants;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.util.XMLHelper;
import dk.itst.oiosaml.common.SAMLUtil;
import dk.itst.oiosaml.common.SOAPException;
import dk.itst.oiosaml.sp.model.OIOSamlObject;
import dk.itst.oiosaml.trust.TrustException;
import java.io.FileNotFoundException;
import org.opensaml.ws.soap.soap11.FaultCode;
import org.opensaml.ws.soap.soap11.FaultString;
import org.opensaml.ws.soap.soap11.impl.FaultCodeBuilder;
import org.opensaml.ws.soap.soap11.impl.FaultStringBuilder;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import javax.xml.namespace.QName;
import org.opensaml.ws.soap.soap11.Detail;
import org.opensaml.ws.soap.soap11.impl.DetailBuilder;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.io.UnmarshallingException;

public class HttpSOAPClient implements SOAPClient {

    private static final String START_SOAP_ENVELOPE = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" + "<soapenv:Header/><soapenv:Body>";
    private static final String END_SOAP_ENVELOPE = "</soapenv:Body></soapenv:Envelope>";
    private static final Logger log = Logger.getLogger(HttpSOAPClient.class);

    /**
     *
     * @param obj
     * @param location
     * @param ignoreCertPath
     * @return
     * @throws IOException
     */
    @Override
    public XMLObject wsCall(OIOSamlObject obj, String location, boolean ignoreCertPath) throws IOException {
        return wsCall(location, ignoreCertPath, obj.toSoapEnvelope(), "http://www.oasis-open.org/committees/security").getBody().getUnknownXMLObjects().get(0);
    }

    @Override
    public Envelope wsCall(XMLObject obj, String location, boolean ignoreCertPath) throws IOException {
        String xml = XMLHelper.nodeToString(SAMLUtil.marshallObject(obj));
        xml = START_SOAP_ENVELOPE + xml.substring(xml.indexOf("?>") + 2) + END_SOAP_ENVELOPE;
        return wsCall(location, ignoreCertPath, xml, "http://www.oasis-open.org/committees/security");
    }

    @Override
    public Envelope wsCall(String location, boolean ignoreCertPath, String xml, String soapAction)
            throws IOException, SOAPException, FileNotFoundException {
        URI serviceLocation;
        try {
            serviceLocation = new URI(location);
        } catch (URISyntaxException e) {
            throw new IOException("Invalid uri for artifact resolve: " + location);
        }
        if (log.isDebugEnabled()) {
            log.debug("serviceLocation..:" + serviceLocation);
            log.debug("soapAction..:" + soapAction);
        }
        HttpURLConnection c = (HttpURLConnection) serviceLocation.toURL().openConnection();
        if (c instanceof HttpsURLConnection) {
            HttpsURLConnection sc = (HttpsURLConnection) c;

            if (ignoreCertPath) {
                sc.setSSLSocketFactory(new DummySSLSocketFactory());
                sc.setHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });
            }
        }
        c.setAllowUserInteraction(false);
        c.setDoInput(true);
        c.setDoOutput(true);
        c.setFixedLengthStreamingMode(xml.getBytes("UTF-8").length);
        c.setRequestMethod("POST");
        c.setReadTimeout(20000);
        c.setConnectTimeout(30000);
        c.addRequestProperty("SOAPAction", "\"" + (soapAction == null ? "" : soapAction) + "\"");
        addContentTypeHeader(xml, c);
        c.setUseCaches(false);
        c.connect();

        try (OutputStream outputStream = c.getOutputStream()) {
            outputStream.write(xml.getBytes());
            //IOUtils.write(xml, outputStream, "UTF-8");
            outputStream.flush();
            outputStream.close();
        } catch (Exception ex) {
            log.error("Cannot connection to STS " + ex);
        }

        if (c.getResponseCode() == 200) {
            String result;
            try (InputStream inputStream = c.getInputStream()) {
                result = IOUtils.toString(inputStream, "UTF-8");
            }
            XMLObject res = SAMLUtil.unmarshallElementFromString(result);

            Envelope envelope = (Envelope) res;
            if (SAMLUtil.getFirstElement(envelope.getBody(), Fault.class) != null) {
                log.warn("Result has soap11:Fault, but server returned 200 OK. Treating as error, please fix the server");
                throw new SOAPException(c.getResponseCode(), result);
            }
            return envelope;
        } else {
            log.debug("Response code: " + c.getResponseCode());

            String result;
            try (InputStream inputStream = c.getErrorStream()) {
                result = IOUtils.toString(inputStream, "UTF-8");
            }
            if (log.isDebugEnabled()) {
                log.debug("Server SOAP fault: " + result);
            }

            SOAPException soapException = new SOAPException(c.getResponseCode(), result);
            Fault fault = soapException.getFault();
            if (fault == null) 
                fault = soapException.getFault(new QName(SOAPConstants.SOAP12_NS, "Fault", "s"));
            if (fault == null){
                if (result.contains("http://schemas.microsoft.com/net/2005/12/windowscommunicationfoundation/dispatcher/fault"))
                {
                    soapException = new SOAPException(c.getResponseCode(),"Unrecognized RST specified in the incoming request.");
                }
                throw soapException;
            }
            if (fault.getCode() == null) {
                Node childNode = fault.getDOM().getFirstChild();
                while (childNode != null) {
                    if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                        updateFault(fault, (Element) childNode);
                        childNode = childNode.getNextSibling();
                    }
                }
                soapException.setFault(fault);
            }
            throw soapException;
        }
    }

    private void updateFault(Fault fault, Element node) {
        if (null != node.getLocalName()) {
            switch (node.getLocalName()) {
                case "Code":
                    FaultCodeBuilder fcb = new FaultCodeBuilder();
                    FaultCode fc = fcb.buildObject(node);
                    Node childValueNode = node.getFirstChild();
                    String codeValue = "";
                    if ((childValueNode.getNodeType() == Node.ELEMENT_NODE)
                            && "Value".equals(childValueNode.getLocalName())) {
                        codeValue = (((Element) node.getFirstChild()).getFirstChild()).getTextContent();
                    }
                    fc.setValue(new QName(codeValue));
                    fault.setCode(fc);
                    break;
                case "Reason":
                    FaultStringBuilder fsb = new FaultStringBuilder();
                    FaultString fm = fsb.buildObject(node);
                    Node childTextNode = node.getFirstChild();
                    String textValue = "";
                    if ((childTextNode.getNodeType() == Node.ELEMENT_NODE) && "Text"
                            .equals(childTextNode.getLocalName())) {
                        textValue = (((Element) node.getFirstChild()).getFirstChild()).getTextContent();
                    }
                    fm.setValue(textValue);
                    fault.setMessage(fm);
                    break;
                case "Detail":
                    DetailBuilder db = new DetailBuilder();
                    Detail d = db.buildObject(node);
                    Element childElement = (Element) node.getFirstChild();
                    UnmarshallerFactory unmarshallerFactory = org.opensaml.xml.Configuration.getUnmarshallerFactory();
                    Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(childElement);
                    if (unmarshaller == null) {
                        unmarshaller = unmarshallerFactory.getUnmarshaller(org.opensaml.xml.Configuration.getDefaultProviderQName());
                        if (unmarshaller == null) {
                            String errorMsg = "No unmarshaller available for " + XMLHelper.getNodeQName(childElement)
                                    + ", child of " + node.getLocalName();
                            log.error(errorMsg);
                            throw new TrustException(errorMsg);
                        }
                    }
                    try {
                        //d.getUnknownXMLObjects().add((XMLObject) node.getFirstChild());
                        d.getUnknownXMLObjects().add(unmarshaller.unmarshall(childElement));
                    } catch (UnmarshallingException ex) {
                        log.error("cannot parse details object" + ex);
                    }
                    fault.setDetail(d);
                    break;
            }
        }
    }

    

    private void addContentTypeHeader(String xml, HttpURLConnection c) {
        String soapVersion = Utils.getSoapVersion(xml);
        if (null != soapVersion) {
            switch (soapVersion) {
                case SOAPConstants.SOAP11_NS:
                    c.addRequestProperty("Content-Type", "text/xml; charset=utf-8");
                    break;
                case SOAPConstants.SOAP12_NS:
                    c.addRequestProperty("Content-Type", "application/soap+xml; charset=utf-8");
                    break;
                default:
                    throw new UnsupportedOperationException("SOAP version " + soapVersion + " not supported");
            }
        }
    }
}
