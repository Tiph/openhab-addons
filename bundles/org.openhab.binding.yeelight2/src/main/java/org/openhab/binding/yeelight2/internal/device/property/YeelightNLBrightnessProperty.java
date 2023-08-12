package org.openhab.binding.yeelight2.internal.device.property;

import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.State;

/**
 *
 * @author Tiph
 *
 */
class YeelightNLBrightnessProperty extends AbstractYeelightMainProperty implements IYeelightDeviceWritableProperty {

    @Override
    public String getPropertyName() {
        return "nl_br";
    }

    @Override
    public String getSetterName() {
        return "set_nl_br";
    }

    @Override
    public Class<? extends State> getOHType() {
        return PercentType.class;
    }

}
