
package consumer.provider;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for pingResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="pingResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="KombitProfile" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0" form="qualified"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "pingResponse", propOrder = {
    "kombitProfile"
})
public class PingResponse {

    @XmlElement(name = "KombitProfile")
    protected String kombitProfile;

    /**
     * Gets the value of the kombitProfile property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKombitProfile() {
        return kombitProfile;
    }

    /**
     * Sets the value of the kombitProfile property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKombitProfile(String value) {
        this.kombitProfile = value;
    }

}
