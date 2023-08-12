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
        switch (val) {
            case "0":
                return StringType.valueOf("SUN");
            case "1":
                return StringType.valueOf("MOON");
            default:
                return StringType.valueOf(val);
        }
    }

    @Override
    public Class<? extends State> getOHType() {
        return StringType.class;
    }

}
