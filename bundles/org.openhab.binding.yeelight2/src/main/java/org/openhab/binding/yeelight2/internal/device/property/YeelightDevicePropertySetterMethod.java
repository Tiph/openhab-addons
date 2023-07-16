package org.openhab.binding.yeelight2.internal.device.property;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;

public interface YeelightDevicePropertySetterMethod {
    String getName();

    String getValue(Command type);

    static final Function<Command, String> ON_OFF_TYPE_TO_API = command -> {
        OnOffType as = ((State) command).as(OnOffType.class);
        if (as == null) {
            throw new ClassCastException();
        }
        return "\"" + as.toFullString().toLowerCase() + "\"";
    };

    static final Function<Command, String> HSB_TYPE_TO_API = command -> {
        HSBType as = ((State) command).as(HSBType.class);
        if (as == null) {
            throw new ClassCastException();
        }
        return Integer.toString(as.getRGB() & 0xFFFFFF);
    };

    static final BiFunction<Command, Class<? extends State>, String> DEFAULT_TYPE_TO_API = (command, stateType) -> {
        Type as = ((State) command).as(stateType);
        if (as == null) {
            throw new ClassCastException();
        }
        return as.toFullString();
    };
}