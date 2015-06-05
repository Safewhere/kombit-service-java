/*
 * The contents of this file are subject to the Mozilla Public 
 * License Version 1.1 (the "License"); you may not use this 
 * file except in compliance with the License. You may obtain 
 * a copy of the License at http://www.mozilla.org/MPL/
 * 
 * Software distributed under the License is distributed on an 
 * "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express 
 * or implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 *
 * The Original Code is OIOSAML Trust Client.
 * 
 * The Initial Developer of the Original Code is Trifork A/S. Portions 
 * created by Trifork A/S are Copyright (C) 2008 Danish National IT 
 * and Telecom Agency (http://www.itst.dk). All Rights Reserved.
 * 
 * Contributor(s):
 *   Joakim Recht <jre@trifork.com>
 *
 */
package dk.itst.oiosaml.trust.internal;

import java.security.Provider;
import java.util.logging.Level;

import javax.xml.crypto.dsig.XMLSignatureFactory;

import org.apache.log4j.Logger;
import org.apache.xml.security.exceptions.AlgorithmAlreadyRegisteredException;

import org.apache.jcp.xml.dsig.internal.dom.XMLDSigRI;
import org.apache.xml.security.transforms.InvalidTransformException;

public class SignatureFactory {

    private static final Logger log = Logger.getLogger(SignatureFactory.class);

    private static XMLSignatureFactory instance;

    @SuppressWarnings("unchecked")
    public static XMLSignatureFactory getInstance() throws ClassNotFoundException, InvalidTransformException {
        if (instance == null) {
            registerTransform();

            Provider p = new XMLDSigRI();
            p.put("TransformService." + STRTransform.implementedTransformURI, DOMSTRTransform.class.getName());
            p.put("Alg.Alias.TransformService.STRTRANSFORM", STRTransform.implementedTransformURI);
            p.put("TransformService." + STRTransform.implementedTransformURI + " MechanismType", "DOM");
            try {
                instance = XMLSignatureFactory.getInstance("DOM", p);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return instance;
    }

    private static void registerTransform() throws ClassNotFoundException, InvalidTransformException {
        try {
            org.apache.xml.security.transforms.Transform.register(STRTransform.implementedTransformURI, STRTransform.class.getName());
            log.debug("STR-Transform registered");
        } catch (AlgorithmAlreadyRegisteredException e) {
            log.info("STR-Transform already registered", e);
        }
    }

}
