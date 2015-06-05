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
 
    
    //production url
    public static String StsEndpointAddress = "https://adgangsstyringeksempler.test-stoettesystemerne.dk/STS/kombit/sts/certificate"; 
    public static String StsMexEndpointAddress = "https://adgangsstyringeksempler.test-stoettesystemerne.dk/STS/kombit/sts/mex?wsdl";
    public static String ServiceEndpointAddress = "https://adgangsstyringeksempler.test-stoettesystemerne.dk:8181/Service/Service";
    public static String AppliesToEndpointAddress = "https://adgangsstyringeksempler.test-stoettesystemerne.dk:8181/Service/Service";
    public static String Issuer = "https://adgangsstyringeksempler.test-stoettesystemerne.dk/STS";
    
    //endregion
    
    //region Error values for AppliesTo 
    public static String ErrorEndpointAddress = "http://kombit.samples.local/";
    public static String CommonRuntimeError = ErrorEndpointAddress + 100;
    public static String ConnectionResolutionError = ErrorEndpointAddress + 101;
    public static String MalformedRequestError = ErrorEndpointAddress + 103;
    public static String PathResolutionError = ErrorEndpointAddress + 104;
    public static String AuditUserRequestError = ErrorEndpointAddress + 106;
    public static String NotSupportedException = ErrorEndpointAddress + 110;
    public static String ConfigurationError = ErrorEndpointAddress + 111;
    public static String DatabaseError = ErrorEndpointAddress + 130;
    //endregion

    //region values for namespace
    public static String WSTrustNamespace = "http://docs.oasis-open.org/ws-sx/ws-trust/200512";
    public static String AddressNamespace = "http://www.w3.org/2005/08/addressing";
    public static String ClaimNamespace = "http://docs.oasis-open.org/wsfed/authorization/200706";
    public static String AppliesToNamespace = "http://schemas.xmlsoap.org/ws/2004/09/policy";
    //endregion

    //region values for claim
    public static String ClaimDialect = "http://docs.oasis-open.org/wsfed/authorization/200706/authclaims";
    public static String ClaimValue = "12345678";
    public static String ClaimURI = "dk:gov:saml:attribute:CvrNumberIdentifier";
    public static String ClaimOptional = "false";
    //endregion

    //region values to build security element for proxy on behalf of
    public static String SecurityNamespace = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
    public static String TrustStorePath = "c:\\Program Files\\glassfish-4.1\\glassfish\\domains\\domain1\\config\\cacerts.jks";
    public static String TrustStorePassword = "changeit";
    public static String StsCertificateAlias = "kombit";// CA95B2F383BEF8144500CD74B88BC42CD3DE936C
    
    public static String KeyStorePath = "C:\\Program Files\\glassfish-4.1\\glassfish\\domains\\domain1\\config\\keystore.jks";
    public static String KeyStorePassword = "changeit";
    public static String ClientCertificateAlias = "{46bf4ba8-c898-45b1-b79f-0d7fb690adcf}";// cert with thumprint 986085D29F8D320FE1D70A6F9E8C7841AB15A37A
    public static String OboCertificateAlias = "{46bf4ba8-c898-45b1-b79f-0d7fb690adcf}";// cert with thumprint 986085D29F8D320FE1D70A6F9E8C7841AB15A37A
    //public static String OboCertificateAlias = "kombit støttesystemer (funktionscertifikat)";// CA95B2F383BEF8144500CD74B88BC42CD3DE936C

    //endregion
}
