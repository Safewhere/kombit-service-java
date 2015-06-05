package consumer.client;

import consumer.util.Constant;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.WebServiceContext;
import consumer.util.Util;
import dk.itst.oiosaml.common.SOAPException;
import dk.itst.oiosaml.trust.TrustClient;
import dk.itst.oiosaml.trust.TrustException;
import java.io.FileNotFoundException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This servlet class represents for a test case Consumer call to Service
 * webservice
 *
 * @author VTT
 */
@WebServlet(name = "ServiceConsumer", urlPatterns = {"/ServiceConsumer"})
public class ServiceConsumer extends HttpServlet {

    private WebServiceContext context;
    String responseFromService;

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
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Service Consumer</title>");
            out.println("</head>");
            out.println("<body>");
            try {
                out.println("Ping to Service.... " + ping());
            } catch (KeyStoreException ex) {
                Logger.getLogger(ServiceConsumer.class.getName()).log(Level.SEVERE, null, ex);
                out.println("Ping to Service.... " + ex.getMessage());
            }
            out.println("</body>");
            out.println("</html>");
        }
    }

    /**
     * Ping to service provider and it should return kombit profile.
     *
     * @return result from ping operation, "failed" if any error happens.
     */
    private String ping() throws KeyStoreException, IOException {
        try {
            TrustClient tokenClient = Util.getTrustClient(Constant.ClientCertificateAlias);
            tokenClient.getToken(null);
            return Util.CallService(tokenClient);

        } catch (TrustException | SOAPException | FileNotFoundException | NoSuchAlgorithmException | CertificateException ex) {
            Logger.getLogger(ServiceConsumer.class.getName()).log(Level.SEVERE, null, ex);
            return ex.getMessage();
        }
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
