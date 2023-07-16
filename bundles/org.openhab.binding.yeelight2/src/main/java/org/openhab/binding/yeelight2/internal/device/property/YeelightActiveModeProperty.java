package org.openhab.binding.yeelight2.internal.device.property;

import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;

/**
 *
 * @author Tiph
 *
 */
class YeelightActiveModeProperty extends AbstractYeelightMainProperty {

    @Override
    public String getPropertyName() {
        return "active_mode";
    }

    @Override
    public State getState(String val) {
        if ("0".equals(val)) {
            return StringType.valueOf("SUN");
        } else if ("1".equals(val)) {
            return StringType.valueOf("MOON");
        }
        return StringType.valueOf("NULL");
    }

}
