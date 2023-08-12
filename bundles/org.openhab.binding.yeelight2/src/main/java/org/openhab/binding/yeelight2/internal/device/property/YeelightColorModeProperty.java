package org.openhab.binding.yeelight2.internal.device.property;

import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;

/**
 *
 * @author Tiph
 *
 */
class YeelightColorModeProperty extends AbstractYeelightMainProperty {

    @Override
    public String getPropertyName() {
        return "color_mode";
    }

    @Override
    public State getState(String val) {
        return super.getState(getColorModeStateValue(val));
    }

    @Override
    public Class<? extends State> getOHType() {
        return StringType.class;
    }

    static final String getColorModeStateValue(String val) {
        switch (val) {
            case "1":
                return "RGB";
            case "2":
                return "CT";
            case "3":
                return "HSV";
            default:
                return val;
        }
    }

}
