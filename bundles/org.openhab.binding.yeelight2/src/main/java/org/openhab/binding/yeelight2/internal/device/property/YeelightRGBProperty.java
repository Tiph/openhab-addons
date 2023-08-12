package org.openhab.binding.yeelight2.internal.device.property;

import org.openhab.core.library.types.HSBType;
import org.openhab.core.types.State;

/**
 *
 * @author Tiph
 *
 */
class YeelightRGBProperty extends AbstractYeelightMainProperty implements IYeelightDeviceWritableProperty {

    @Override
    public String getPropertyName() {
        return "rgb";
    }

    @Override
    public String getSetterName() {
        return "set_rgb";
    }

    @Override
    public Class<? extends State> getOHType() {
        return HSBType.class;
    }

}
