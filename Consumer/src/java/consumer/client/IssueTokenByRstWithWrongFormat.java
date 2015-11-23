package consumer.client;

//import consumer.sts.StsFaultDetail;
import consumer.util.Constant;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import consumer.util.Util;
import dk.itst.oiosaml.common.SOAPException;
import dk.itst.oiosaml.trust.FaultHandler;
import dk.itst.oiosaml.trust.TrustClient;
import dk.itst.oiosaml.trust.TrustException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import org.apache.xml.security.transforms.InvalidTransformException;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.xml.XMLObject;

/**
 * This servlet class represents for a test case Consumer call to C# sts with an
 * empty body soap message then an error will show
 *
 * @author LXP
 */
@WebServlet(name = "IssueTokenByRstWithWrongFormat", urlPatterns = {"/IssueTokenByRstWithWrongFormat"})
public class IssueTokenByRstWithWrongFormat extends HttpServlet {

    String errorResponse = "";

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
            out.println("<title>Servlet IssueToken</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("Issue Token " + negotiateSecurityToken());
            out.println("</body>");
            out.println("</html>");
        }
    }

    /**
     * Call to C# STS service to get token returned from STS, in the flow of
     * process we inject an empty body soap message to make this call fail
     *
     * @return "Successfully" if receive token, "Failed" if any error happens.
     */
    private String negotiateSecurityToken() throws MalformedURLException {
        Assertion stsToken = null;
        try {

            try {
                TrustClient client = Util.getTrustClient(Constant.ClientCertificateAlias);

                client.addFaultHander("https://sts.kombit.dk/fault", "StsFaultDetail", new FaultHandler() {
                    @Override
                    public void handleFault(QName faultCode, String faultReason, XMLObject detail) {
                        //StsFaultDetail detailObj = Util.generateStsFaultDetail(detail);
                        errorResponse += "Error thrown: "
                            + "EventId = " + faultCode.toString()
                            + ". Reason = " + faultReason+ "<br /><br />";
//                        if ((detailObj != null) && (detailObj.getMessage()!=null))
//                        {
//                            errorResponse += ". Detail = " + detailObj.getMessage().getValue()+ "<br /><br />";
//                        }
                    }

                });
                //Call service to get issue a token
                stsToken = client.getTokenWithEmptySoapMessageBody();
            } catch (ClassNotFoundException | InvalidTransformException | SOAPException ex) {
                Logger.getLogger(IssueTokenByRstWithWrongFormat.class.getName()).log(Level.SEVERE, null, ex);
                errorResponse += "There is an uncaught exception thrown, please check server log for more details";
            } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException ex) {
                Logger.getLogger(IssueTokenByRstWithWrongFormat.class.getName()).log(Level.SEVERE, null, ex);
                errorResponse += "There is an uncaught exception thrown, please check server log for more details";
            }
        } catch (TrustException e) {
            errorResponse += "There is an uncaught exception thrown, please check server log for more details: " + e.getMessage() + "<br /><br />";
        }
        if (stsToken != null) {
            errorResponse += "Successfully";
        }
        return errorResponse;
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
