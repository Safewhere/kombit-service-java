package service.service;

import BasicPrivilegeProfileParser.PrivilegeGroup;
import BasicPrivilegeProfileParser.PrivilegeGroupParser;
import static com.sun.xml.ws.addressing.WsaServerTube.REQUEST_MESSAGE_ID;
import com.sun.xml.ws.server.EndpointMessageContextImpl;
import com.sun.xml.wss.SubjectAccessor;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.XWSSecurityRuntimeException;
import com.sun.xml.wss.saml.util.SAMLUtil;
import java.util.Set;
import javax.jws.WebService;
import javax.jws.WebMethod;

import javax.xml.stream.XMLStreamException;
import javax.xml.ws.WebServiceContext;
import service.config.Constant;
import org.w3c.dom.Element;
import javax.annotation.Resource;
import javax.xml.stream.XMLStreamReader;
import com.sun.xml.wss.saml.Assertion;
import com.sun.xml.wss.saml.AssertionUtil;
import com.sun.xml.wss.saml.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jws.HandlerChain;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.handler.MessageContext;
import org.xml.sax.SAXException;
import javax.jws.soap.SOAPBinding;
import javax.jws.WebResult;
import javax.jws.WebParam;
import javax.xml.ws.BindingType;
import service.oiosaml.idws.Framework;
import service.oiosaml.idws.FrameworkMismatchFault;

import javax.xml.ws.Action;
import javax.xml.ws.soap.Addressing;

/**
 * This Service class represents for a Service web service example HandlerChain
 * point to config file where point to the SOAPHandler which processing the soap
 * message receive or send out.
 *
 * @author VTT
 */
@WebService(serviceName = "Service", targetNamespace = "http://kombit.provider.dk/")
@BindingType(javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING)
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT, use = SOAPBinding.Use.LITERAL)
@HandlerChain(file = "HandlerConfig.xml")
@Addressing
public class Service {

    @Resource
    private WebServiceContext context;

    /**
     * This is a Service web service ping operation sample
     *
     * @param framework
     * @return string which if success will contains Bpp value that has been
     * sent with the claim received from STS.
     */
    @Action(input = "http://kombit.provider.dk/Service/pingRequest", output = "http://kombit.provider.dk/Service/pingResponse")
//            ,fault = { @FaultAction(className=AddNumbersException.class, 
//                value="http://calculator.com/faultAction") 
//      })
    @WebMethod(action = "http://kombit.provider.dk/Service/ping")
    public @WebResult(name = "KombitProfile", targetNamespace = "http://kombit.provider.dk/")
    String
            ping(@WebParam(name = "Framework", header = true, targetNamespace = "urn:liberty:sb:2006-08") Framework framework) {
        try {
            String result = "";
            FrameworkMismatchFault.throwIfNecessary(framework, context.getMessageContext());

            Element saml = getSAMLAssertion();
            Assertion assertion = AssertionUtil.fromElement(saml);

            String subjectInfo = getNameId(assertion);
            String bppValue = getBppValue(assertion);
            if ("".equals(bppValue)) {
                result = Constant.ResponseMessage + " but bpp value has not been found";
            }

            ArrayList<PrivilegeGroup> bppGroupsList;
            try {
                bppGroupsList = (ArrayList<PrivilegeGroup>) PrivilegeGroupParser.Parse(bppValue);

                String json = PrivilegeGroupParser.ConvertToJsonString(bppGroupsList);

                String messageId = getMessageId();

                String responseValue = Constant.ResponseMessage + "<br/>MessageId: " + messageId + "<br/> BPP: <br/>" + json;
                result = responseValue;
                return result;
            } catch (SAXException | IOException | ParserConfigurationException ex) {
                Logger.getLogger(Service.class.getName()).log(Level.SEVERE, null, ex);
                return "Something goes wrong. Please check server log for more details";
            }
        } catch (SAMLException ex) {
            Logger.getLogger(Service.class.getName()).log(Level.SEVERE, null, ex);
            return "Something goes wrong. Please check server log for more details";
        }
    }

    /**
     * Get Message Id from received message.
     */
    private String getMessageId() {
        MessageContext messageContext = context.getMessageContext();
        String id = (String) ((EndpointMessageContextImpl) messageContext).get(REQUEST_MESSAGE_ID);
        return id;
    }

    /**
     * Get Name Id from SAMLAssertion.
     *
     * @param assertion which is extracted SAMLAssertion from received message.
     */
    private String getNameId(Assertion assertion) {
        Subject subject = null;
        String nameID = "";
        try {
            subject = assertion.getSubject();
        } catch (Exception ex) {
            subject = null;
        }
        if (subject != null) {
            nameID = subject.getNameId().getValue();
        }
        return nameID;
    }

    /**
     * Get Bpp Value from SAMLAssertion. Loop through assertion, get string
     * value attribute that match a claim holding Bpp Value If not exists return
     * empty
     *
     * @param assertion which is extracted SAMLAssertion from received message.
     */
    private String getBppValue(Assertion assertion) {
        List<Object> statements = assertion.getStatements();
        for (Object s : statements) {
            if (s instanceof AttributeStatement) {
                List<Attribute> attrs = ((AttributeStatement) s).getAttributes();
                for (Attribute attr : attrs) {
                    String attrName = attr.getName();
                    if (Constant.ClaimNameHoldBppValue.equals(attrName)) {
                        List<Object> attrValues = attr.getAttributes();
                        String attrValue = ((Element) attrValues.get(0)).getFirstChild().getNodeValue();
                        return attrValue;
                    }
                }
            }
        }
        return "";
    }

    /**
     * Get SAMLAssertion.
     *
     * Process message get from context, extract Assertion out and build up
     * SAMLAssertion to return.
     */
    private Element getSAMLAssertion() {
        Element samlAssertion = null;
        try {
            javax.security.auth.Subject subj = SubjectAccessor.getRequesterSubject(context);
            Set<Object> set = subj.getPublicCredentials();
            for (Object obj : set) {
                if (obj instanceof Element) {
                    if (((Element) obj).getLocalName().equals("Assertion")) {
                        samlAssertion = (Element) obj;
                        break;
                    }
                } else if (obj instanceof XMLStreamReader) {
                    XMLStreamReader reader = (XMLStreamReader) obj;
                    //To create a DOM Element representing the Assertion :
                    samlAssertion = SAMLUtil.createSAMLAssertion(reader);
                }
            }
        } catch (XMLStreamException | XWSSecurityException ex) {
            throw new XWSSecurityRuntimeException(ex);
        }
        return samlAssertion;
    }
}
