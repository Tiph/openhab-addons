package org.openhab.binding.yeelight2.internal.device.property;

import java.util.Optional;

import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 *
 * @author Tiph
 *
 */
class YeelightColorTemperatureProperty extends AbstractYeelightMainProperty {

    @Override
    public String getPropertyName() {
        return "ct";
    }

    @Override
    public State getState(String val) {
        return DecimalType.valueOf(val);
    }

    @SuppressWarnings("null")
    @Override
    public Optional<YeelightDevicePropertySetterMethod> getSetter() {
        return Optional.of(new YeelightDevicePropertySetterMethod() {

            @Override
            public String getName() {
                return "set_ct_abx";
            }

            @Override
            public String getValue(Command command) {
                return DEFAULT_TYPE_TO_API.apply(command, DecimalType.class);
            }
        });

    }

}
