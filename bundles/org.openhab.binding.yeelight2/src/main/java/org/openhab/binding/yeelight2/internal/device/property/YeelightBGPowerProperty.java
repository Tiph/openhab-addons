package org.openhab.binding.yeelight2.internal.device.property;

import java.util.Optional;

import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 *
 * @author Tiph
 *
 */
class YeelightBGPowerProperty extends AbstractYeelightBGProperty {

    @Override
    public String getPropertyName() {
        return "bg_power";
    }

    @Override
    public State getState(String val) {
        return OnOffType.from(val);
    }

    @SuppressWarnings("null")
    @Override
    public Optional<YeelightDevicePropertySetterMethod> getSetter() {
        return Optional.of(new YeelightDevicePropertySetterMethod() {

            @Override
            public String getName() {
                return "bg_set_power";
            }

            @Override
            public String getValue(Command command) {
                return ON_OFF_TYPE_TO_API.apply(command);
            }
        });

    }

}
