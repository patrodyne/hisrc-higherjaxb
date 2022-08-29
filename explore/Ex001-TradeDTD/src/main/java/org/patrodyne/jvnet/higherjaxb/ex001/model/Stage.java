package org.patrodyne.jvnet.higherjaxb.ex001.model;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Enumeration of entity life-cycle stages.</p>
 *
 * <p>As entities are processed, they goes through several life-cycle stages.
 * When an entity is instantiated, it is in an open stage until it can be
 * validated. When it is ready then it can move to the active stage; otherwise,
 * it must be put on hold and escalated for review. When active, it is available
 * for general processing and when all processing is complete and actions have
 * been satisfied the the entity can be closed. An entity on hold can be canceled
 * or fixed and re-opened. An open entity can be canceled before it becomes active.
 * An active entity may return to hold for review based on processing issues. A
 * closed entity should be deleted, after a time, based on the disposal policy.</p>
 *
 * <ul>
 *     <li>HOLD - Esclated for review to fix issues.</li>
 *     <li>OPEN - Being prepared for activation.</li>
 *     <li>ACTIVE - Ready for processing, in-progress.</li>
 *     <li>CLOSED - Actions complete normally. Immutable. Disposable.</li>
 *     <li>CANCELED - Can/should not be processed. Immutable. Disposable.</li>
 * </ul>
 *
 * <p>Best practice is to limit stages to these five canonical phases and use
 * the entity itself as the context for interpretaion of each stage. This provides
 * for context flexibility while preserving summarized stages during transaction
 * processing.</p>
 *
 * <p>Best practices:</p>
 *
 * <ul>
 *     <li>Hold a parent entity when any of its children are on hold.</li>
 *     <li>Open a parent entity when any of its children are open and:</li>
 *     <ul>
 *         <li>No other children are on hold.</li>
 *         <li>Other children are open, canceled or closed.</li>
 *     </ul>
 *     <li>Activate a parent entity when any children are active and:</li>
 *     <ul>
 *         <li>No other children are open or on hold.</li>
 *         <li>Other children are active, canceled or closed.</li>
 *     </ul>
 *     <li>Close a parent entity after all it children are closed/canceled.</li>
 *     <li>Cancel a parent entity when all it children are canceled.</li>
 *     <li>Delete parent/children when parent has been closed/canceled, per disposal policy.</li>
 * </ul>
 */
@XmlType(name = "stage")
@XmlEnum
public enum Stage {

    @XmlEnumValue("Hold")
    HOLD("Hold"),
    @XmlEnumValue("Open")
    OPEN("Open"),
    @XmlEnumValue("Active")
    ACTIVE("Active"),
    @XmlEnumValue("Closed")
    CLOSED("Closed"),
    @XmlEnumValue("Canceled")
    CANCELED("Canceled");
    private final String value;

    Stage(String v)
	{
        value = v;
    }

    public String value()
	{
        return value;
    }

    public static Stage fromValue(String v)
	{
        for (Stage c: Stage.values())
		{
            if (c.value.equals(v))
                return c;
        }
        throw new IllegalArgumentException(v);
    }
}
