
package com.checkmarx.cxviewer.ws.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CxWSProjectIssueTrackingSettings complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CxWSProjectIssueTrackingSettings">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="TrackingSystemID" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="Params" type="{http://Checkmarx.com/v7}ArrayOfCxWSIssueTrackingParam" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CxWSProjectIssueTrackingSettings", propOrder = {
    "trackingSystemID",
    "params"
})
public class CxWSProjectIssueTrackingSettings {

    @XmlElement(name = "TrackingSystemID")
    protected long trackingSystemID;
    @XmlElement(name = "Params")
    protected ArrayOfCxWSIssueTrackingParam params;

    /**
     * Gets the value of the trackingSystemID property.
     * 
     */
    public long getTrackingSystemID() {
        return trackingSystemID;
    }

    /**
     * Sets the value of the trackingSystemID property.
     * 
     */
    public void setTrackingSystemID(long value) {
        this.trackingSystemID = value;
    }

    /**
     * Gets the value of the params property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfCxWSIssueTrackingParam }
     *     
     */
    public ArrayOfCxWSIssueTrackingParam getParams() {
        return params;
    }

    /**
     * Sets the value of the params property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfCxWSIssueTrackingParam }
     *     
     */
    public void setParams(ArrayOfCxWSIssueTrackingParam value) {
        this.params = value;
    }

}
