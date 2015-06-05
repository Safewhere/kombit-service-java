package dk.itst.oiosaml.trust;

import org.opensaml.ws.soap.util.SOAPConstants;
import org.opensaml.xml.security.x509.X509Credential;
import dk.itst.oiosaml.security.CredentialRepository;
import dk.itst.oiosaml.sp.service.util.HttpSOAPClient;
import dk.itst.oiosaml.sp.service.util.SOAPClient;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;

public abstract class ClientBase {

    static {
        TrustBootstrap.bootstrap();
    }
    protected static final CredentialRepository credentialRepository = new CredentialRepository();

    protected SOAPClient soapClient = new HttpSOAPClient();
    private final X509Credential credential;

    protected String soapVersion = SOAPConstants.SOAP12_NS;
    protected SigningPolicy signingPolicy = new SigningPolicy(true);

    private String requestXML;
    private OIOSoapEnvelope lastResponse;

    protected Map<QName, FaultHandler> faultHandlers = new HashMap<QName, FaultHandler>();

    private String logFilePath = "c:/temp/";

    public ClientBase(X509Credential credential, String logFilePath) {
        this.credential = credential;
        this.logFilePath = logFilePath;
    }

    protected void setRequestXML(String xml) {
        requestXML = xml;
    }

    protected void setLastResponse(OIOSoapEnvelope env) {
        lastResponse = env;
    }

    public String getLastRequestXML() {
        return requestXML;
    }

    /**
     * Set the client to use when executing the request.
     *
     * @param client
     */
    public void setSOAPClient(SOAPClient client) {
        this.soapClient = client;
    }

    /**
     * Set the SOAP version to use.
     *
     * @param soapVersion Namespace of the soap version to use. The client
     * defaults to soap 1.1.
     */
    public void setSoapVersion(String soapVersion) {
        this.soapVersion = soapVersion;
    }

    public OIOSoapEnvelope getLastResponse() {
        return lastResponse;
    }

    /**
     * Set the signing policy for ws requests.
     *
     * @param signingPolicy
     */
    public void setSigningPolicy(SigningPolicy signingPolicy) {
        this.signingPolicy = signingPolicy;
    }

    protected X509Credential getCredential() {
        return credential;
    }

    protected String getLogFilePath() {
        return this.logFilePath;
    }

    /**
     * Add a new fault handler.
     *
     * @see #addFaultHandler(QName, FaultHandler)
     * @param namespace
     * @param localName
     * @param handler
     */
    public void addFaultHander(String namespace, String localName, FaultHandler handler) {
        addFaultHandler(new QName(namespace, localName), handler);
    }

    /**
     * Add a fault handler for a specific soap fault type.
     *
     * The registered type is matched against the types in Fault/Detail. If a
     * matching QName element is found, the fault handler is invoked. Only one
     * handler is invoked for a Fault - the first matching element.
     *
     * @param element Detail element to match.
     * @param handler
     */
    public void addFaultHandler(QName element, FaultHandler handler) {
        faultHandlers.put(element, handler);
    }
}
