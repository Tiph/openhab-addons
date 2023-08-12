package org.openhab.binding.yeelight2.internal.device.property;

import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;

interface IYeelightDeviceProperty {
    String getChannelGroupId();

    String getPropertyName();

    default State getState(String val) {
        Class<? extends State> targetType = getOHType();

        if (targetType == OnOffType.class) {
            return OnOffType.from(val);
        } else if (targetType == PercentType.class) {
            return PercentType.valueOf(val);
        } else if (targetType == DecimalType.class) {
            DecimalType.valueOf(val);
        } else if (targetType == HSBType.class) {
            int intVal = Integer.parseInt(val);
            int r = intVal >>> 16 & 0xFF;
            int g = intVal >>> 8 & 0xFF;
            int b = intVal & 0xFF;
            return HSBType.fromRGB(r, g, b);
        } else if (targetType == StringType.class) {
            return StringType.valueOf(val);
        }

        throw new IllegalStateException("Type: " + targetType + " is not handle");
    }

    Class<? extends State> getOHType();

}
