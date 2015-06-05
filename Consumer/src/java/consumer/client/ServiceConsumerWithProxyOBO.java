package consumer.client;

import consumer.util.Constant;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import consumer.util.Util;
import dk.itst.oiosaml.common.SOAPException;
import dk.itst.oiosaml.trust.TrustBootstrap;
import dk.itst.oiosaml.trust.TrustClient;
import dk.itst.oiosaml.trust.TrustException;
import java.io.FileNotFoundException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import org.apache.xml.security.transforms.InvalidTransformException;
import org.opensaml.xml.XMLObject;

/**
 * This servlet class represents for a test case Consumer call Service service
 * with a certificate as OnBehalfOf
 *
 * @author VTT
 */
@WebServlet(name = "ServiceConsumerWithProxyOBO", urlPatterns = {"/ServiceConsumerWithProxyOBO"})
public class ServiceConsumerWithProxyOBO extends HttpServlet {

    
    /*
    * Setup configuration for OIOSAML since this servlet uses parsing binarysecuritytoken 
    */
    @Override
    public void init(ServletConfig config) throws ServletException {
        TrustBootstrap.bootstrap();
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        Util.configMessageLogFile();
        
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet ServiceConsumerWithProxyOBO</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("Result Proxy OnBehalfOf at " + pingWithProxyOnBehalfOf());
            out.println("</body>");
            out.println("</html>");
        }
    }

    /**
     * Call to Service service with X509Certificate as OnBehalfOf
     *
     * @return result from ping operation
     */
    private String pingWithProxyOnBehalfOf() {
        try {
            //Add certificate as OnBehalfOf        
            XMLObject bootstrapToken = Util.generateProxyOboTokenElement(Constant.OboCertificateAlias);
            TrustClient tokenClient = Util.getTrustClient(Constant.ClientCertificateAlias);
            tokenClient.setDelegateToken(bootstrapToken);
            tokenClient.setUseReferenceForDelegateToken(false);
            tokenClient.setUseActAs(false);
            tokenClient.getToken(null);
            return Util.CallService(tokenClient);
        } catch (TrustException | SOAPException | FileNotFoundException | NoSuchAlgorithmException | CertificateException ex) {
            Logger.getLogger(ServiceConsumer.class.getName()).log(Level.SEVERE, null, ex);
            return ex.getMessage();
        } catch (KeyStoreException | IOException ex) {
            Logger.getLogger(ServiceConsumerWithNormalOBO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "Cannot negotiate security token with normal onbehalfof option";
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
