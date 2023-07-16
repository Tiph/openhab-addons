package org.openhab.binding.yeelight2.internal.device.property;

import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;

/**
 *
 * @author Tiph
 *
 */
class YeelightDelayOffProperty extends AbstractYeelightMainProperty {

    @Override
    public String getPropertyName() {
        return "delayoff";
    }

    @Override
    public State getState(String val) {
        return StringType.valueOf(val);
    }

}
