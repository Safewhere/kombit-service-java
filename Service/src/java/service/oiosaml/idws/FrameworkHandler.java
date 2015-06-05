/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service.oiosaml.idws;

import java.util.Collections;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.apache.log4j.Logger;

/**
 *
 * @author LXP
 */
public class FrameworkHandler implements SOAPHandler<SOAPMessageContext> {

    private static final Logger log = Logger.getLogger(FrameworkHandler.class);

    private static final QName FRAMEWORK = new QName("urn:liberty:sb:2006-08", "Framework", "sbf");
    private JAXBContext jc;

    @PostConstruct
    public void init() {
        log.info("Starting framework handler");
        try {
            jc = JAXBContext.newInstance("service.oiosaml.idws");
        } catch (JAXBException e) {
            log.error(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Set<QName> getHeaders() {
        return Collections.singleton(FRAMEWORK);
    }

    @Override
    public void close(MessageContext context) {

    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        boolean outbound = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        log.debug("Outbound message: " + outbound);
        try {
            if (outbound) {
                SOAPHeader h = context.getMessage().getSOAPHeader();
                SOAPHeaderElement framework = h.addHeaderElement(FRAMEWORK);
                framework.addAttribute(new QName("version"), "2.0");
                framework.addAttribute(new QName("urn:liberty:sb:profile", "profile", "sbfprofile"), "urn:liberty:sb:profile:basic");
            } else {
                Framework f = null;
                Object[] headers = context.getHeaders(FRAMEWORK, jc, true);
                if (headers.length == 1) {
                    JAXBElement<Framework> e = (JAXBElement<Framework>) headers[0];
                    f = e.getValue();
                }
                log.debug("Found header: " + f);
                FrameworkMismatchFault.throwIfNecessary(f, context);
            }
        } catch (SOAPException e) {
            log.debug("SoapException is thrown: " + e);
            throw new RuntimeException(e);
        }

        return true;
    }

}
