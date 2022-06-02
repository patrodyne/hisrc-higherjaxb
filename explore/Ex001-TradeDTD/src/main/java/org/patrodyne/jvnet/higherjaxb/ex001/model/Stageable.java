package org.patrodyne.jvnet.higherjaxb.ex001.model;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * Java class for Stageable complex type.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Stageable")
public class Stageable implements Serializable
{
    private final static long serialVersionUID = 20220501L;

    @XmlAttribute(name = "Stage", required = true)
    protected Stage stage;

    /**
     * Gets the value of the stage property.
     * 
     * @return
     *     possible object is
     *     {@link Stage }
     *     
     */
    public Stage getStage() {
        return stage;
    }

    /**
     * Sets the value of the stage property.
     * 
     * @param value
     *     allowed object is
     *     {@link Stage }
     *     
     */
    public void setStage(Stage value) {
        this.stage = value;
    }
}
