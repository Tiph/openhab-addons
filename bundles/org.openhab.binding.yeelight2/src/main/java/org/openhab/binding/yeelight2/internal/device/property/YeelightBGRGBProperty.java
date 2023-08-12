package org.openhab.binding.yeelight2.internal.device.property;

import org.openhab.core.library.types.HSBType;
import org.openhab.core.types.State;

/**
 *
 * @author Tiph
 *
 */
class YeelightBGRGBProperty extends AbstractYeelightBGProperty implements IYeelightDeviceWritableProperty {

    @Override
    public String getPropertyName() {
        return "bg_rgb";
    }

    @Override
    public String getSetterName() {
        return "bg_set_rgb";
    }

    @Override
    public Class<? extends State> getOHType() {
        return HSBType.class;
    }

}
