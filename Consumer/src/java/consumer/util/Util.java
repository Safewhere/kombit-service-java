/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package consumer.util;

import consumer.client.ServiceConsumer;
import consumer.provider.ObjectFactory;
import consumer.provider.Ping;
import consumer.provider.PingResponse;
import consumer.sts.StsFaultMessage;
import dk.itst.oiosaml.common.SAMLUtil;
import dk.itst.oiosaml.common.SOAPException;
import dk.itst.oiosaml.trust.ResultHandler;
import dk.itst.oiosaml.trust.TrustClient;
import dk.itst.oiosaml.trust.TrustException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import org.opensaml.ws.soap.util.SOAPConstants;
import org.opensaml.ws.wssecurity.BinarySecurityToken;
import org.opensaml.xml.XMLObject;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author VTT
 */
public class Util {
    static String responseFromService;
    
    /*
     * Setup log parameter for in and out message 
     * which helps to see clear request and response message in server.log file 
     */
    public static void configMessageLogFile() {
        com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump = true;
        com.sun.xml.ws.transport.http.HttpAdapter.dump = true;
        com.sun.xml.ws.transport.http.HttpAdapter.dump_threshold = 16000;
    }
    
    /**
     * generate trust client which will handle all the communication to STS
     * @param clientCertificateAlias
     * @return
     * @throws KeyStoreException
     * @throws IOException
     * @throws FileNotFoundException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException 
     */
    public static TrustClient getTrustClient(String clientCertificateAlias) throws KeyStoreException, IOException, FileNotFoundException, NoSuchAlgorithmException, CertificateException
    {
        String serviceEndpointAddress = Constant.ServiceEndpointAddress;

        X509Certificate stsCertificate = readcert(Constant.TrustStorePath
                , Constant.StsCertificateAlias, Constant.TrustStorePassword);

        final TrustClient tokenClient = new TrustClient(Constant.StsEndpointAddress
                , Constant.KeyStorePath, Constant.KeyStorePassword, clientCertificateAlias
                , stsCertificate, Constant.LogFilePath);
        tokenClient.setSoapVersion(SOAPConstants.SOAP12_NS);
        tokenClient.setUseReferenceForDelegateToken(false);
        tokenClient.setIssuer(Constant.Issuer);
        tokenClient.setAppliesTo(serviceEndpointAddress);
        tokenClient.setClaimsDialect(Constant.ClaimDialect);        
        tokenClient.addClaim(Constant.ClaimURI, Constant.ClaimValue);
        
        return tokenClient;
    }
    /**
     * This method is to negotiate security token and using it to invoke operation from service which is secured by STS
     * @param tokenClient
     * @return 
     */
    public static String CallService(TrustClient tokenClient)
    {
        try {            
            Ping request;
            request = new Ping();
            tokenClient.getServiceClient().sendRequest(request,
                    JAXBContext.newInstance(Ping.class, PingResponse.class, ObjectFactory.class),
                    Constant.ServiceEndpointAddress,
                    Constant.PingRequest,
                    null,
                    new ResultHandler<consumer.provider.PingResponse>() {
                        @Override
                        public void handleResult(consumer.provider.PingResponse result) throws Exception {
                            responseFromService = result.getKombitProfile();
                        }
                    });
            return responseFromService;
        } catch (TrustException | SOAPException | FileNotFoundException | NoSuchAlgorithmException | CertificateException | JAXBException ex ) {
            Logger.getLogger(ServiceConsumer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (KeyStoreException | IOException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "Cannot ping to service, please look at the server log for more details";
    }
    
    /**
     * Build BinarySecurityToken for Proxy OBO token
     *
     * @param oboCertificateAlias
     * @return
     */
    public static BinarySecurityToken generateProxyOboTokenElement(String oboCertificateAlias) throws KeyStoreException, IOException, FileNotFoundException, NoSuchAlgorithmException, CertificateException{
            //Read cert from store
            X509Certificate cert = readcert(Constant.KeyStorePath, oboCertificateAlias, Constant.KeyStorePassword);
            //Get base64 string of certificate
            String psB64Certificate = DatatypeConverter.printBase64Binary(cert.getEncoded());                
            BinarySecurityToken token = SAMLUtil.buildXMLObject(BinarySecurityToken.class);
            token.getUnknownAttributes().put(new QName("ValueType"), "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509v3");
            token.setEncodingType("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary");
            token.setValue(psB64Certificate);            
            return token;     
    }  
    
    /**
     * Read certificate from key store file, alias and password
     *
     * @param fileName
     * @param alias
     * @param password
     * @return X509Certificate
     * @throws java.io.FileNotFoundException
     * @throws java.security.KeyStoreException
     * @throws java.security.cert.CertificateException
     * @throws java.security.NoSuchAlgorithmException
     */
    public static X509Certificate readcert(String fileName, String alias, String password) throws FileNotFoundException, KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {

        char[] passwordC = password.toCharArray();
        FileInputStream fIn = new FileInputStream(fileName);
        KeyStore keystore = KeyStore.getInstance("JKS");
        keystore.load(fIn, passwordC);
        X509Certificate cert = (X509Certificate) keystore.getCertificate(alias);

        return cert;
    }
    
    /**
     * Generate STSFaultMessage object from soap fault response message
     * @param detail
     * @return 
     */
    public static StsFaultMessage generateStsFaultMessage(XMLObject detail) {
        StsFaultMessage ms = new StsFaultMessage();
        Node childNode = detail.getDOM().getFirstChild();
        while (childNode != null) {
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                updateStsFaultMessage(ms, (Element) childNode);
                childNode = childNode.getNextSibling();
            }
        }
        return ms;
    }
    
    /**
     * Update StsFaultMessage 
     * @param ms
     * @param node 
     */
    private static void updateStsFaultMessage(StsFaultMessage ms, Element node) {
        consumer.sts.ObjectFactory factory = new consumer.sts.ObjectFactory();
        if (null != node.getLocalName()) {
            switch (node.getLocalName()) {
                case "EventId":
                    String eventIdValue = node.getTextContent();
                    JAXBElement<String> eventIdValueElement = factory.createString(eventIdValue);
                    ms.setEventId(eventIdValueElement);
                    break;
                case "Message":
                    String messageValue = node.getTextContent();
                    JAXBElement<String> messageValueElement = factory.createString(messageValue);
                    ms.setMessage(messageValueElement);
                    break;
            }
        }
    }
}
