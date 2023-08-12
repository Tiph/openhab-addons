package org.openhab.binding.yeelight2.internal.device.property;

import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.State;

/**
 *
 * @author Tiph
 *
 */
class YeelightBGBrightnessProperty extends AbstractYeelightBGProperty implements IYeelightDeviceWritableProperty {

    @Override
    public String getPropertyName() {
        return "bg_bright";
    }

    @Override
    public String getSetterName() {
        return "bg_set_bright";
    }

    @Override
    public Class<? extends State> getOHType() {
        return PercentType.class;
    }

}
