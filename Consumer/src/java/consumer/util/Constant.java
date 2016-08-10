package consumer.util;

/**
 * This class represents for all constant values used in project
 *
 * @author VTT
 */
public class Constant {
    
    public static String PingRequest = "http://kombit.provider.dk/Service/pingRequest";
    public static String LogFilePath = "C:/temp/";
    //region Endpoint
    // change it when debug at local or deploy in server   
 
    //The certificate endpoint address of Identify*STS
    public static String StsEndpointAddress = "https://adgangsstyring.eksterntest-stoettesystemerne.dk/runtime/services/kombittrust/14/certificatemixed"; 
    //The metadata endpoint of Identify*STS
    public static String StsMexEndpointAddress = "https://adgangsstyring.eksterntest-stoettesystemerne.dk/runtime/services/kombittrust/mex?wsdl";
    //The issuer name of issued security token
    public static String Issuer = "https://adgangsstyring.eksterntest-stoettesystemerne.dk/";
    
    //The certificate endpoint of STS test stub
//  public static String StsEndpointAddress = "https://adgangsstyring.eksterntest-stoettesystemerne.dk/sts/kombit/sts/certificate"; 
//  public static String StsMexEndpointAddress = "https://adgangsstyring.eksterntest-stoettesystemerne.dk/sts/kombit/sts/mex?wsdl";
//  public static String Issuer = "https://adgangsstyring.eksterntest-stoettesystemerne.dk/sts";
    
    //The url of production service which is secured by Identify*STS
    public static String ServiceEndpointAddress = "https://service.eksterntest-stoettesystemerne.dk:8181/Service/Service";
    
    //Avender context value
    public static String ClaimValue = "01234567";

    //region values to build security element for proxy on behalf of
    public static String SecurityNamespace = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
    public static String TrustStorePath = "c:\\Program Files\\glassfish-4.1\\glassfish\\domains\\domain1\\config\\cacerts.jks";
    public static String TrustStorePassword = "changeit";    
    //The public certificate of Identify*STS
    public static String StsCertificateAlias = "kombitexternal";// ‎3a 10 3c f6 0a 9d 97 7b a5 a4 99 3e 06 d8 c9 ba 36 f2 58 3e
    
    public static String KeyStorePath = "C:\\Program Files\\glassfish-4.1\\glassfish\\domains\\domain1\\config\\keystore.jks";
    public static String KeyStorePassword = "changeit";
    //The client certificate which is used for negotiating a security token from Identify*STS
    public static String ClientCertificateAlias = "tu generel foces gyldig (funktionscertifikat)";// cert with thumprint ‎15 3e 97 1c 6b ae cc 4e 4e c6 8d 82 69 30 39 01 65 4a 02 a6
    //The client certificate which is used for negotiating a bootstrap token or used in onbehalfof proxy element
    public static String OboCertificateAlias = "nets danid a/s - tu voces gyldig";// cert with thumprint 46 78 23 72 45 fe dc 80 59 d1 13 67 59 55 df b8 70 d3 6b f4
    //endregion
    
    /*All the following setting should not be touched*/
    public static String ClaimDialect = "http://docs.oasis-open.org/wsfed/authorization/200706/authclaims";
    public static String NoConnectionFoundError = "https://consumer.kombit.dk/noconnectionfound";
    public static String ClaimURI = "dk:gov:saml:attribute:CvrNumberIdentifier";
    public static String WSTrustNamespace = "http://docs.oasis-open.org/ws-sx/ws-trust/200512";
    public static String AddressNamespace = "http://www.w3.org/2005/08/addressing";
    public static String ClaimNamespace = "http://docs.oasis-open.org/wsfed/authorization/200706";
    public static String AppliesToNamespace = "http://schemas.xmlsoap.org/ws/2004/09/policy";
    
    //public static String ClaimOptional = "false";
    
}
