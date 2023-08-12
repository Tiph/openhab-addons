package org.openhab.binding.yeelight2.internal.device.property;

import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.State;

/**
 *
 * @author Tiph
 *
 */
class YeelightPowerProperty extends AbstractYeelightMainProperty implements IYeelightDeviceWritableProperty {

    @Override
    public String getPropertyName() {
        return "power";
    }

    @Override
    public String getSetterName() {
        return "set_power";
    }

    @Override
    public Class<? extends State> getOHType() {
        return OnOffType.class;
    }

}
