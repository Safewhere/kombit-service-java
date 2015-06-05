package consumer.client;

import consumer.util.Constant;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import consumer.util.Util;
import dk.itst.oiosaml.trust.TrustException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import org.opensaml.saml2.core.Assertion;

/**
 * This servlet class represents for a test case Consumer call to C# sts to get
 * an issue token
 *
 * @author LXP
 */
@WebServlet(name = "IssueToken", urlPatterns = {"/IssueToken"})
public class IssueToken extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     * @throws java.net.MalformedURLException
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        
        Util.configMessageLogFile();
        
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet IssueToken</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("Issue Token " + negotiateSecurityToken());
            out.println("</body>");
            out.println("</html>");
        }
    }

    /**
     * Call to C# STS service, then get token returned from STS. * @return
     * "Successfully" if receive token, "Failed" if any error happens.
     */
    private String negotiateSecurityToken() {

        String result = "";
        Assertion stsToken = null;
        try {
            //Call service to get issue a token 
            stsToken = Util.getTrustClient(Constant.ClientCertificateAlias).getToken(null);
            result += "Call to service ";
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException | TrustException ex) {
            result = "Failed: " + ex.getMessage() + "<br /><br />";
            Logger.getLogger(IssueToken.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (stsToken != null) {
            result += "Successfully";
        }
        return result;
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     * @throws java.net.MalformedURLException
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     * @throws java.net.MalformedURLException
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
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
