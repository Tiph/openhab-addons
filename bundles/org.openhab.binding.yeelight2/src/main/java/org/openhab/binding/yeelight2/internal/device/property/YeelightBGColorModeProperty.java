package org.openhab.binding.yeelight2.internal.device.property;

import org.openhab.core.types.State;

/**
 *
 * @author Tiph
 *
 */
class YeelightBGColorModeProperty extends AbstractYeelightBGProperty {

    @Override
    public String getPropertyName() {
        return "bg_lmode";
    }

    @Override
    public State getState(String val) {
        return YeelightColorModeProperty.COLOR_MODE_STATE_MAPPING.apply(val);
    }

}
