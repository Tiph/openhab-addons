package org.openhab.binding.yeelight2.internal.device.property;

import java.util.Optional;

import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 *
 * @author Tiph
 *
 */
class YeelightBGBrightnessProperty extends AbstractYeelightBGProperty {

    @Override
    public String getPropertyName() {
        return "bg_bright";
    }

    @Override
    public State getState(String val) {
        return PercentType.valueOf(val);
    }

    @SuppressWarnings("null")
    @Override
    public Optional<YeelightDevicePropertySetterMethod> getSetter() {
        return Optional.of(new YeelightDevicePropertySetterMethod() {

            @Override
            public String getName() {
                return "bg_set_bright";
            }

            @Override
            public String getValue(Command command) {
                return DEFAULT_TYPE_TO_API.apply(command, PercentType.class);
            }
        });

    }

}
