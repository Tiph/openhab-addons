package org.openhab.binding.yeelight2.internal.device.property;

import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.State;

/**
 *
 * @author Tiph
 *
 */
class YeelightBGColorTemperatureProperty extends AbstractYeelightBGProperty implements IYeelightDeviceWritableProperty {

    @Override
    public String getPropertyName() {
        return "bg_ct";
    }

    @Override
    public String getSetterName() {
        return "bg_set_ct_abx";
    }

    @Override
    public Class<? extends State> getOHType() {
        return DecimalType.class;
    }

}
