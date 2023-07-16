package org.openhab.binding.yeelight2.internal.device.property;

import java.util.Optional;

import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 *
 * @author Tiph
 *
 */
class YeelightBGRGBProperty extends AbstractYeelightBGProperty {

    @Override
    public String getPropertyName() {
        return "bg_rgb";
    }

    @Override
    public State getState(String val) {
        return YeelightRGBProperty.RGB_STATE_MAPPING.apply(val);
    }

    @SuppressWarnings("null")
    @Override
    public Optional<YeelightDevicePropertySetterMethod> getSetter() {
        return Optional.of(new YeelightDevicePropertySetterMethod() {

            @Override
            public String getName() {
                return "bg_set_rgb";
            }

            @Override
            public String getValue(Command command) {
                return HSB_TYPE_TO_API.apply(command);
            }
        });

    }

}
