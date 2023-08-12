package org.openhab.binding.yeelight2.internal.device.property;

import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.State;

/**
 *
 * @author Tiph
 *
 */
class YeelightColorTemperatureProperty extends AbstractYeelightMainProperty implements IYeelightDeviceWritableProperty {

    @Override
    public String getPropertyName() {
        return "ct";
    }

    @Override
    public String getSetterName() {
        return "set_ct_abx";
    }

    @Override
    public Class<? extends State> getOHType() {
        return DecimalType.class;
    }

}
