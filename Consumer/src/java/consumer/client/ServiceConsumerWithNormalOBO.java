package consumer.client;

import consumer.util.Constant;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.logging.Level;
import java.util.logging.Logger;

import consumer.util.Util;
import dk.itst.oiosaml.common.SOAPException;
import dk.itst.oiosaml.trust.TrustClient;
import dk.itst.oiosaml.trust.TrustException;
import java.io.FileNotFoundException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import org.opensaml.saml2.core.Assertion;

/**
 * This servlet class represents for a test case Consumer get an issue token
 * from C# STS then call Service service with that token as OnBehalfOf
 *
 * @author VTT
 */
public class ServiceConsumerWithNormalOBO extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");        
        
        Util.configMessageLogFile();
        
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet ServiceConsumerWithNormalOBO</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("Result NormalOnBehalfOf: " + pingWithNormalOnBehalfOf());
            out.println("</body>");
            out.println("</html>");
        }
    }

    /**
     * Call to Service service with SAMLAssertion as OnBehalfOf
     *
     * @return result from ping operation
     */
    private String pingWithNormalOnBehalfOf() {
        try {
            //Get bootstrap token by request a token 
            TrustClient bootstrapTokenClient = Util.getTrustClient(Constant.ClientCertificateAlias);
            Assertion bootstrapToken = bootstrapTokenClient.getToken(null);
            
            TrustClient tokenClient = Util.getTrustClient(Constant.ClientCertificateAlias);
            //set bootstrap token as obo into tokenClient to request a token from STS
            tokenClient.setDelegateToken(bootstrapToken);
            tokenClient.setUseReferenceForDelegateToken(false);
            tokenClient.setUseActAs(false);
            tokenClient.getToken(null);
            //then call Service
            return Util.CallService(tokenClient);
        } catch (TrustException | SOAPException | FileNotFoundException | NoSuchAlgorithmException | CertificateException ex) {
            Logger.getLogger(ServiceConsumer.class.getName()).log(Level.SEVERE, null, ex);
            return ex.getMessage();
        } catch (KeyStoreException | IOException ex) {
            Logger.getLogger(ServiceConsumerWithNormalOBO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "Cannot negotiate security token with normal onbehalfof option";
    }

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
