/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.itst.oiosaml.liberty;

import javax.xml.namespace.QName;
import org.opensaml.xml.AbstractExtensibleXMLObject;

/**
 *
 * @author VTT
 */
public class ClaimValue extends AbstractExtensibleXMLObject{

            public static final String LOCAL_NAME = "Value";
            public static final QName ELEMENT_NAME= new QName("http://docs.oasis-open.org/wsfed/authorization/200706", "Value", "auth");
	    
	    private String value;

            public ClaimValue(String namespaceURI, String elementLocalName, String namespacePrefix) {
                super(namespaceURI, elementLocalName, namespacePrefix);
            }
            
            public ClaimValue() 
            {
                super(ELEMENT_NAME.getNamespaceURI(), ELEMENT_NAME.getLocalPart(), ELEMENT_NAME.getPrefix());
            }

	    public String getValue() {
			return value;
		}
	    
	    public void setValue(String value) {
			this.value = prepareForAssignment(this.value, value);
		}
}
