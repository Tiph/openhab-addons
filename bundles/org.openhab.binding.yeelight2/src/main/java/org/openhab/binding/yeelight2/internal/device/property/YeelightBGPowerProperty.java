package org.openhab.binding.yeelight2.internal.device.property;

import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.State;

/**
 *
 * @author Tiph
 *
 */
class YeelightBGPowerProperty extends AbstractYeelightBGProperty implements IYeelightDeviceWritableProperty {

    @Override
    public String getPropertyName() {
        return "bg_power";
    }

    @Override
    public String getSetterName() {
        return "bg_set_power";
    }

    @Override
    public Class<? extends State> getOHType() {
        return OnOffType.class;
    }

}
