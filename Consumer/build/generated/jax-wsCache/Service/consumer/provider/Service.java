
package consumer.provider;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.Action;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.10-b140803.1500
 * Generated source version: 2.2
 * 
 */
@WebService(name = "Service", targetNamespace = "http://kombit.provider.dk/")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
@XmlSeeAlso({
    ObjectFactory.class
})
public interface Service {


    /**
     * 
     * @param parameters
     * @param framework
     * @return
     *     returns consumer.provider.PingResponse
     */
    @WebMethod(action = "http://kombit.provider.dk/Service/ping")
    @WebResult(name = "pingResponse", targetNamespace = "http://kombit.provider.dk/", partName = "result")
    @Action(input = "http://kombit.provider.dk/Service/pingRequest", output = "http://kombit.provider.dk/Service/pingResponse")
    public PingResponse ping(
        @WebParam(name = "ping", targetNamespace = "http://kombit.provider.dk/", partName = "parameters")
        Ping parameters,
        @WebParam(name = "Framework", targetNamespace = "urn:liberty:sb:2006-08", header = true, partName = "Framework")
        Framework framework);

}
