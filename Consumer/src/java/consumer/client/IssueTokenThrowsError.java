package consumer.client;

import consumer.sts.ObjectFactory;
//import consumer.sts.StsFaultDetail;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import consumer.util.Constant;
import consumer.util.Util;
import dk.itst.oiosaml.trust.FaultHandler;
import dk.itst.oiosaml.trust.TrustClient;
import dk.itst.oiosaml.trust.TrustException;
import java.io.FileNotFoundException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBElement;

import javax.xml.namespace.QName;
import org.opensaml.xml.XMLObject;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This servlet class represents for a test case Consumer call to C# sts with
 * some special AppliesTo values then associated error will shows. Example if
 * AppliesTo (in requestsecuritytoken) is http://kombit.samples.local/100 then
 * error CommonRuntimeError with event id 100 shows
 *
 * @author VTT
 */
@WebServlet(name = "IssueTokenThrowsError", urlPatterns = {"/IssueTokenThrowsError"})
public class IssueTokenThrowsError extends HttpServlet {
    String errorResponse = "";
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs *
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Util.configMessageLogFile();

        try {
            TrustClient client = Util.getTrustClient(Constant.ClientCertificateAlias);
            
            client.addFaultHander("https://sts.kombit.dk/fault", "StsFaultDetail", new FaultHandler() {
                @Override
                public void handleFault(QName faultCode, String faultReason, XMLObject detail) {
                    //StsFaultDetail detailObj = Util.generateStsFaultDetail(detail);
                    errorResponse += "Error thrown: "
                            + "EventId = " + faultCode.toString()
                            + ". Reason = " + faultReason+ "<br /><br />";
//                    if ((detailObj != null) && (detailObj.getMessage()!=null))
//                    {
//                        errorResponse += ". Detail = " + detailObj.getMessage().getValue();
//                    }
                }

            });
            try {
                client.setAppliesTo(Constant.NoConnectionFoundError);
                client.getToken(null);
            } catch (TrustException e) {
                errorResponse += "There is an uncaught exception thrown, please check server log for more details: " + e.getMessage() + "<br /><br />";
            }
            
            response.setContentType("text/html;charset=UTF-8");
            try (PrintWriter out = response.getWriter()) {
                /* TODO output your page here. You may use following sample code. */
                out.println("<!DOCTYPE html>");
                out.println("<html>");
                out.println("<head>");
                out.println("<title>Servlet HanndleIssueErrorToken</title>");
                out.println("</head>");
                out.println("<body>");
                out.println("<h3>" + "This request expects to receive error response from STS" + "</h3>");
                out.println(errorResponse);
                out.println("</body>");
                out.println("</html>");
            }
        } catch (KeyStoreException | FileNotFoundException | NoSuchAlgorithmException | CertificateException ex) {
            Logger.getLogger(IssueTokenThrowsError.class.getName()).log(Level.SEVERE, null, ex);
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
            throws ServletException, IOException, FileNotFoundException {
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
            throws ServletException, IOException, FileNotFoundException {
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
