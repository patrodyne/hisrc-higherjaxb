package org.example.trade;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

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
