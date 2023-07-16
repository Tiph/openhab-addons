package org.openhab.binding.yeelight2.internal.device.property;

import java.util.Optional;

import org.openhab.core.types.State;

interface IYeelightDeviceProperty {
    String getChannelGroupId();

    String getPropertyName();

    State getState(String val);

    default Optional<YeelightDevicePropertySetterMethod> getSetter() {
        return Optional.empty();
    }

}
