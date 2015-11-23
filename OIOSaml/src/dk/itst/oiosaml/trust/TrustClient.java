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
 * The Original Code is OIOSAML Trust Client.
 * 
 * The Initial Developer of the Original Code is Trifork A/S. Portions 
 * created by Trifork A/S are Copyright (C) 2008 Danish National IT 
 * and Telecom Agency (http://www.itst.dk). All Rights Reserved.
 * 
 * Contributor(s):
 *   Joakim Recht <jre@trifork.com>
 *
 */
package dk.itst.oiosaml.trust;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.XMLSignatureException;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.ws.soap.soap11.Fault;
import org.opensaml.ws.wstrust.RequestSecurityTokenResponse;
import org.opensaml.ws.wstrust.RequestSecurityTokenResponseCollection;
import org.opensaml.ws.wstrust.RequestedSecurityToken;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.util.XMLHelper;
import org.w3c.dom.Element;
import dk.itst.oiosaml.common.SAMLUtil;
import dk.itst.oiosaml.common.SOAPException;
import dk.itst.oiosaml.logging.MessageLogger;
import dk.itst.oiosaml.sp.UserAssertionHolder;
import dk.itst.oiosaml.sp.model.OIOAssertion;
import java.io.FileNotFoundException;
import java.security.KeyStoreException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import javax.xml.namespace.QName;
import org.apache.xml.security.transforms.InvalidTransformException;
import org.opensaml.ws.soap.soap11.Detail;

/**
 * Client interface for retrieving STS tokens via WS-Trust 1.3.
 *
 * <p>
 * Call {@link #getToken()} to make an STS Issue request.</p>
 *
 * <p>
 * Instances of this class are not considered thread-safe. They can, however, be
 * reused.</p>
 *
 * @author Joakim Recht
 *
 */
public class TrustClient extends ClientBase {

    private static final Logger log = Logger.getLogger(TrustClient.class);

    private String endpoint;
    //private final EndpointReference epr;
    private String appliesTo;

    private String issuer;

    private final PublicKey stsKey;

    private boolean useReferenceForDelegateToken = false;
    private boolean useActAs = true;

    private Assertion token;
    private XMLObject delegateToken;
    private XMLObject securityToken;

    private String claimsDialect;
    private final Map<String, String> claims = new HashMap<>();

    /**
     * Create a new client using default settings.
     *
     * <p>
     * The default settings are read from the OIOSAML configuration. The
     * following properties are used:</p>
     * <ul>
     * </ul>
     *
     * <p>
     * Furthermore, this constructor assumes that a valid SAML assertion has
     * been placed in {@link UserAssertionHolder} (which should be the case if
     * the OIOSAML SPFilter is configured correctly), and that the assertion
     * contains an DiscoveryEPR attribute.</p>
     *
     * @param stsAddress
     * @param clientKeyStoreFile
     * @param clientCertificatePassword
     * @param clientCertificateAlias
     * @param stsCertificate
     * @param logFilePath
     * @throws java.security.KeyStoreException
     * @throws java.io.IOException
     * @throws java.io.FileNotFoundException
     * @throws java.security.cert.CertificateException
     * @throws java.security.NoSuchAlgorithmException
     */
    public TrustClient(String stsAddress, String clientKeyStoreFile
            , String clientCertificatePassword, String clientCertificateAlias
            , X509Certificate stsCertificate, String logFilePath)
            throws KeyStoreException, IOException, FileNotFoundException, CertificateException, NoSuchAlgorithmException {
        super(credentialRepository.getCredential(clientKeyStoreFile, clientCertificatePassword, clientCertificateAlias)
                ,logFilePath);
        stsKey = stsCertificate.getPublicKey();
        endpoint = stsAddress;
    }

    public Assertion getToken() throws TrustException, ClassNotFoundException, InvalidTransformException, SOAPException {
        return getToken(null);
    }

    /**
     * Execute a Issue request against the STS.
     *
     * The retrieved token is saved in the client for use if
     * {@link #sendRequest(XMLObject, String, String, PublicKey)} is called.
     *
     * // * @param dialect The Claims dialect to add to the request. If // *
     * <code>null</code>, no Claims are added.
     *
     * @param lifetimeExpire
     * @return A DOM element with the returned token.
     * @throws TrustException If any error occurred.
     */
    public Assertion getToken(DateTime lifetimeExpire) throws TrustException {
        try {
            String xml;
            xml = toXMLRequest(lifetimeExpire);
            return getTokenByXml(xml);
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | MarshalException | XMLSignatureException | ClassNotFoundException | InvalidTransformException | SOAPException ex) {
            java.util.logging.Logger.getLogger(TrustClient.class.getName()).log(Level.SEVERE, null, ex);
            throw new TrustException(ex);
        }
    }

    /**
     * Execute a Issue request against the STS with a soap message has empty
     * body.
     *
     * Wiil throw exception
     *
     * // * @param dialect The Claims dialect to add to the request. If // *
     * <code>null</code>, no Claims are added. * @return A DOM element with the
     * returned token.
     * @return 
     * @throws TrustException If any error occurred.
     * @throws java.lang.ClassNotFoundException
     * @throws org.apache.xml.security.transforms.InvalidTransformException
     * @throws dk.itst.oiosaml.common.SOAPException
     */
    public Assertion getTokenWithEmptySoapMessageBody() throws TrustException, ClassNotFoundException, InvalidTransformException, SOAPException {
        try {
            String xml = toXMLRequestWithEmptySoapBody();
            return getTokenByXml(xml);
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | MarshalException | XMLSignatureException e) {
            throw new TrustException(e);
        }
    }

    private Assertion getTokenByXml(String xml) throws TrustException, ClassNotFoundException, InvalidTransformException, SOAPException {
        try {
            setRequestXML(xml);
            MessageLogger.LogToFile(getLogFilePath(), "Java.Consumer.SentMessageToSTS.xml", xml);
            OIOSoapEnvelope env = new OIOSoapEnvelope(
                    soapClient.wsCall(endpoint, true, xml, "http://docs.oasis-open.org/ws-sx/ws-trust/200512/RST/Issue"));
            setLastResponse(env);

            MessageLogger.LogToFile(getLogFilePath(), "Java.Consumer.ReceivedResponseFromSTS.xml", env.toXML());

//            if (stsKey != null) {
//                if (!env.verifySignature(stsKey)) {
//                    throw new TrustException("Response was not signed correctly");
//                }
//            } else {
//                log.warn("No STS certificate specified, not validating response");
//            }
            //TODO: Support tokens in security header
            XMLObject res = env.getBody();
            if (res instanceof Fault) {
                Fault f = (Fault) res;
                throw new TrustException("Unable to retrieve STS token: " + SAMLUtil.getSAMLObjectAsPrettyPrintXML(f));
            } else if (res instanceof RequestSecurityTokenResponse) {
                RequestSecurityTokenResponse tokenResponse = (RequestSecurityTokenResponse) res;

                return findToken(tokenResponse);
            } else if (res instanceof RequestSecurityTokenResponseCollection) {
                RequestSecurityTokenResponse tokenResponse = ((RequestSecurityTokenResponseCollection) res).getRequestSecurityTokenResponses().get(0);

                return findToken(tokenResponse);
            } else {
                for (XMLObject object : res.getOrderedChildren()) {
                    if (object instanceof RequestedSecurityToken) {
                        XMLObject token1 = ((RequestedSecurityToken) object).getUnknownXMLObject();
                        if (!(token1 instanceof Assertion)) {
                            throw new TrustException("Returned token is not a SAML Assertion: " + token1);
                        } else {
                            return validateToken((Assertion) token1);
                        }
                    }
                }
                throw new TrustException("Got a " + res.getElementQName() + ", expected " + RequestSecurityTokenResponse.ELEMENT_NAME);
            }
        } catch (SOAPException e) {
            Fault fault = e.getFault();
            if (fault != null) {
                Detail detail = fault.getDetail();

                QName code = null;
                if (fault.getCode() != null) {
                    code = fault.getCode().getValue();
                }

                String message = null;
                if (fault.getMessage() != null) {
                    message = fault.getMessage().getValue();
                }

                if ((detail != null) && (!detail.getUnknownXMLObjects().isEmpty())) {
                    for (XMLObject el : detail.getUnknownXMLObjects()) {
                        FaultHandler handler = faultHandlers.get(el.getElementQName());
                        if (handler != null) {
                            if (log.isDebugEnabled()) {
                                log.debug("Found fault handler for " + el.getElementQName() + ": " + handler);
                            }
                            try {
                                handler.handleFault(code, message, el);
                            } catch (Exception ex) {
                                throw new TrustException("Cannot load fault handler" + ex);
                            }
                            return null;
                        }
                    }
                } else {
                    if (faultHandlers.size() > 0) {
                        FaultHandler handler = faultHandlers.get(new QName("http://www.w3.org/2001/XMLSchema-instance", "default"));
                        if (handler != null) {
                            try {
                                handler.handleFault(code, message, null);
                            } catch (Exception ex) {
                                throw new TrustException(ex);
                            }
                            return null;
                        }
                    }
                }
                throw new TrustException("Unhandled SOAP Fault " + message, e);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Uncaughted exception was thrown " + e);
                }
                throw new TrustException("Uncaughted exception was thrown, please try again! Or check server log for more details." + e.getResponse());
            }
        } catch (IOException ioe) {
            throw new TrustException(ioe);
        }
    }

    private Assertion findToken(RequestSecurityTokenResponse tokenResponse) {
        RequestedSecurityToken rst = SAMLUtil.getFirstElement(tokenResponse, RequestedSecurityToken.class);
        XMLObject token1 = rst.getUnknownXMLObject();
        if (!(token1 instanceof Assertion)) {
            throw new TrustException("Returned token is not a SAML Assertion: " + token1);
        } else {
            return validateToken((Assertion) token1);
        }
    }

    private Assertion validateToken(Assertion token) {
        OIOAssertion a = new OIOAssertion(token);
        if (stsKey != null) {
            if (!a.verifySignature(stsKey)) {
                log.error("Token is not signed correctly by the STS");
                throw new TrustException("Token assertion does not contain a valid signature");
            }
        } else {
            log.warn("No STS certificate specified, not validating assertion");
        }
        this.token = token;
        token.detach();
        return token;
    }

    /**
     * Get the bootstrap token from the DiscoveryEPR.
     *
     * @return
     */
//    public OIOAssertion getBootstrap() {
//        Token token = getToken("urn:liberty:security:tokenusage:2006-08:SecurityToken", epr.getMetadata().getUnknownXMLObjects(SecurityContext.ELEMENT_NAME));
//        return new OIOAssertion((Assertion) SAMLUtil.unmarshallElementFromString(XMLHelper.nodeToString(SAMLUtil.marshallObject(token.getAssertion()))));
//    }
    private String toXMLRequest(DateTime lifetimeExpire) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, MarshalException, XMLSignatureException, ClassNotFoundException, InvalidTransformException {
        OIOSoapEnvelope env = getSoapEnvelope(lifetimeExpire);
        return getSignedXmlFromSoapEnvelope(env);
    }

    private String toXMLRequestWithEmptySoapBody() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, MarshalException, XMLSignatureException, ClassNotFoundException, InvalidTransformException {
        OIOIssueRequest req = OIOIssueRequest.buildEmptyRequest();
        OIOSoapEnvelope env = OIOSoapEnvelope.buildEnvelope(soapVersion, signingPolicy, false);
        env.setAction("http://docs.oasis-open.org/ws-sx/ws-trust/200512/RST/Issue");
        env.setTo(endpoint);
        env.setReplyTo("http://www.w3.org/2005/08/addressing/anonymous");
        env.setBody(req.getXMLObject());
        env.setTimestamp(5);
        return getSignedXmlFromSoapEnvelope(env);
    }

    private OIOSoapEnvelope getSoapEnvelope(DateTime lifetimeExpire) throws ClassNotFoundException, InvalidTransformException {
        OIOIssueRequest req = OIOIssueRequest.buildRequest();

        if (issuer != null) {
            req.setIssuer(issuer);
        }
        if (claimsDialect != null || claims.size() > 0) {
            req.setClaims(claimsDialect, claims);
        }
        if (lifetimeExpire != null) {
            req.setLifetime(lifetimeExpire);
        }
        req.setUseKey(getCredential());

        req.setAppliesTo(appliesTo);

        OIOSoapEnvelope env = OIOSoapEnvelope.buildEnvelope(soapVersion, signingPolicy, false);
        env.setAction("http://docs.oasis-open.org/ws-sx/ws-trust/200512/RST/Issue");
        env.setTo(endpoint);
        env.setReplyTo("http://www.w3.org/2005/08/addressing/anonymous");
        env.setBody(req.getXMLObject());
        env.setTimestamp(5);

        if (securityToken != null) {
            env.addSecurityToken(securityToken);
        }

        if (delegateToken != null) {
            if (useReferenceForDelegateToken && delegateToken instanceof Assertion) {
                delegateToken.detach();
                if (useActAs) {
                    req.setActAs(((Assertion) delegateToken).getID());
                } else {
                    req.setOnBehalfOf(((Assertion) delegateToken).getID());
                }
                env.addSecurityToken(delegateToken);
            } else {
                if (useActAs) {
                    req.setActAs(delegateToken);
                } else {
                    req.setOnBehalfOf(delegateToken);
                }
            }
        }
        return env;
    }

    private String getSignedXmlFromSoapEnvelope(OIOSoapEnvelope env) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, MarshalException, XMLSignatureException, ClassNotFoundException, InvalidTransformException {
        Element signed = env.sign(getCredential());
        return XMLHelper.nodeToString(signed);
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public void setAppliesTo(String appliesTo) {
        this.appliesTo = appliesTo;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    /**
     * Configure whether bootstrap tokens should be placed directly in
     * OnBehalfOf/ActAs or in the Security header using a
     * SecurityTokenReference.
     *
     * @param useReferenceForDelegateToken <code>true</code> to put the token in
     * the security header.
     */
    public void setUseReferenceForDelegateToken(boolean useReferenceForDelegateToken) {
        this.useReferenceForDelegateToken = useReferenceForDelegateToken;
    }

    /**
     * Set to true to include tokens in the wst:ActAs element, otherwise
     * wst:OnBehalfOf will be used.
     *
     * @param useActAs
     */
    public void setUseActAs(boolean useActAs) {
        this.useActAs = useActAs;
    }

    /**
     * Get a client for invoking web services using the token retrieved with
     * {@link TrustClient#getToken(String)}.
     *
     * The client will be configured with the same soapclient, soapversion, and
     * credentials as the trustclient.
     *
     * @return
     * @throws java.security.KeyStoreException
     * @throws java.io.IOException
     * @throws java.io.FileNotFoundException
     * @throws java.security.cert.CertificateException
     * @throws java.security.NoSuchAlgorithmException
     */
    public ServiceClient getServiceClient() throws KeyStoreException, IOException, FileNotFoundException, CertificateException, NoSuchAlgorithmException {
        ServiceClient client = new ServiceClient(getCredential(), getLogFilePath());
        client.setSOAPClient(soapClient);
        client.setSoapVersion(soapVersion);
        client.setToken(token);

        return client;
    }

    public void setClaimsDialect(String dialect) {
        claimsDialect = dialect;
    }

    public void addClaim(String claimUir, String claimValue) {
        claims.put(claimUir, claimValue);
    }

    public void setDelegateToken(XMLObject delegateToken) {
        this.delegateToken = delegateToken;
    }

    public void setSecurityToken(XMLObject securityToken) {
        this.securityToken = securityToken;
    }
}
