package org.openhab.binding.yeelight2.internal.device.property;

import java.util.function.Function;

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
        return COLOR_MODE_STATE_MAPPING.apply(val);
    }

    static final Function<String, State> COLOR_MODE_STATE_MAPPING = v -> {
        switch (v) {
            case "1":
                return StringType.valueOf("RGB");
            case "2":
                return StringType.valueOf("CT");
            case "3":
                return StringType.valueOf("HSV");
            default:
                return StringType.valueOf("NULL");
        }
    };

}
