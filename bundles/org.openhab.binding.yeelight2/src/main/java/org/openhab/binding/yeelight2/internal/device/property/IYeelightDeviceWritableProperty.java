package org.openhab.binding.yeelight2.internal.device.property;

import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

interface IYeelightDeviceWritableProperty extends IYeelightDeviceProperty {

    String getSetterName();

    default String getSetterValue(Command command) {
        Class<? extends State> targetType = getOHType();
        State as = ((State) command).as(targetType);
        if (as == null) {
            throw new ClassCastException("Cannot transform " + command + " as " + targetType);
        }

        if (targetType == OnOffType.class) {
            return "\"" + as.toFullString().toLowerCase() + "\"";
        } else if (targetType == HSBType.class) {
            return Integer.toString(((HSBType) as).getRGB() & 0xFFFFFF);
        } else {
            return as.toFullString();
        }
    }

}
