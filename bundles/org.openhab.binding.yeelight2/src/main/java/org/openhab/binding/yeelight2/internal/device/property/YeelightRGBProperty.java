package org.openhab.binding.yeelight2.internal.device.property;

import java.util.Optional;
import java.util.function.Function;

import org.openhab.core.library.types.HSBType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 *
 * @author Tiph
 *
 */
class YeelightRGBProperty extends AbstractYeelightMainProperty {

    @Override
    public String getPropertyName() {
        return "rgb";
    }

    @Override
    public State getState(String val) {
        return RGB_STATE_MAPPING.apply(val);
    }

    @SuppressWarnings("null")
    @Override
    public Optional<YeelightDevicePropertySetterMethod> getSetter() {
        return Optional.of(new YeelightDevicePropertySetterMethod() {

            @Override
            public String getName() {
                return "set_rgb";
            }

            @Override
            public String getValue(Command command) {
                return HSB_TYPE_TO_API.apply(command);
            }
        });

    }

    static final Function<String, State> RGB_STATE_MAPPING = v -> {
        int val = Integer.parseInt(v);
        int r = val >>> 16 & 0xFF;
        int g = val >>> 8 & 0xFF;
        int b = val & 0xFF;
        return HSBType.fromRGB(r, g, b);
    };

}
