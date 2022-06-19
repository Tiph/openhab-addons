package org.openhab.binding.yeelight2.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Type;

/**
 *
 * @author Leo
 *
 */
public enum YeelightDeviceProperty {

    // Global
    POWER("power", "set_power", OnOffType.class, OnOffType::from),
    DELAYOFF("delayoff", null, StringType.class, StringType::valueOf),
    MUSIC_MODE("music_on", null, OnOffType.class, OnOffType::from),

    // Main
    MAIN_POWER("main_power", "set_power", OnOffType.class, OnOffType::from),
    ACTIVE_MODE("active_mode", null, StringType.class, Constant.activeModeFunction),
    BRIGHTNESS("bright", "set_bright", PercentType.class, PercentType::valueOf),
    NL_BRIGHTNESS("nl_br", "set_nl_br", PercentType.class, PercentType::valueOf),
    COLOR_TEMPERATURE("ct", "set_ct_abx", DecimalType.class, DecimalType::valueOf),
    RGB("rgb", "set_rgb", HSBType.class, Constant.rgbFunction),
    COLOR_MODE("color_mode", null, StringType.class, Constant.colorModeFunction),
    FLOWING("flowing", null, OnOffType.class, OnOffType::from),
    FLOW_PARAMS("flow_params", null, StringType.class, StringType::valueOf, FLOWING),

    // Background
    BG_POWER("bg_power", "bg_set_power", OnOffType.class, OnOffType::from),
    BG_COLOR_TEMPERATURE("bg_ct", "bg_set_ct_abx", DecimalType.class, DecimalType::valueOf),
    BG_BRIGHTNESS("bg_bright", "bg_set_bright", PercentType.class, PercentType::valueOf),
    BG_RGB("bg_rgb", "bg_set_rgb", HSBType.class, Constant.rgbFunction),
    BG_COLOR_MODE("bg_lmode", null, StringType.class, Constant.colorModeFunction),
    BG_FLOWING("bg_flowing", null, OnOffType.class, OnOffType::from),
    BG_FLOW_PARAMS("bg_flow_params", null, StringType.class, StringType::valueOf, BG_FLOWING),
    //
    ;

    /**
     * Name of the property in yeelight (must match the channel id)
     */
    private String propertyName;
    /**
     * Method to call for setting the property
     */
    private String setterName;

    /**
     * The type associated with the property
     */
    private Class<? extends Type> associatedType;

    /**
     * The function to retrieve a type from a string
     */
    private Function<String, Type> mappingFunction;

    /**
     * The property to check for a value to determine if channel is supported
     */
    private YeelightDeviceProperty channelProperty;

    private static final Map<String, YeelightDeviceProperty> ENUM_MAP;

    static {
        final Map<String, YeelightDeviceProperty> tempMap = new HashMap<>();
        for (YeelightDeviceProperty property : YeelightDeviceProperty.values()) {
            tempMap.put(property.propertyName, property);
        }
        ENUM_MAP = Collections.unmodifiableMap(tempMap);
    }

    YeelightDeviceProperty(String propName, String setterMethodName, Class<? extends Type> associatedType,
            Function<String, Type> mappingFunction) {
        this(propName, setterMethodName, associatedType, mappingFunction, null);
    }

    YeelightDeviceProperty(String propName, String setterMethodName, Class<? extends Type> associatedType,
            Function<String, Type> mappingFunction, YeelightDeviceProperty channelProperty) {
        this.propertyName = propName;
        this.setterName = setterMethodName;
        this.associatedType = associatedType;
        this.mappingFunction = mappingFunction;
        if (channelProperty == null) {
            this.channelProperty = this;
        } else {
            this.channelProperty = channelProperty;
        }
    }

    public String getPropertyName() {
        return propertyName;
    }

    public String getSetterName() {
        return setterName;
    }

    public Class<? extends Type> getAssociatedType() {
        return associatedType;
    }

    public Type getType(String val) {
        return mappingFunction.apply(val);
    }

    public YeelightDeviceProperty getChannelProperty() {
        return channelProperty;
    }

    public static YeelightDeviceProperty fromPropertyName(String value) {
        return ENUM_MAP.get(value);
    }

    private static class Constant {
        static final Function<String, Type> colorModeFunction = new Function<String, Type>() {
            @Override
            public Type apply(String s) {
                switch (s) {
                    case "1":
                        return StringType.valueOf("RGB");
                    case "2":
                        return StringType.valueOf("CT");
                    case "3":
                        return StringType.valueOf("HSV");

                }
                return StringType.valueOf("NULL");
            }
        };

        static final Function<String, Type> rgbFunction = new Function<String, Type>() {
            @Override
            public Type apply(String s) {
                int v = Integer.valueOf(s);
                int r = v >>> 16 & 0xFF;
                int g = v >>> 8 & 0xFF;
                int b = v & 0xFF;
                return HSBType.fromRGB(r, g, b);
            }
        };
        static final Function<String, Type> activeModeFunction = new Function<String, Type>() {
            @Override
            public Type apply(String s) {
                switch (s) {
                    case "0":
                        return StringType.valueOf("SUN");
                    case "1":
                        return StringType.valueOf("MOON");

                }
                return StringType.valueOf("NULL");
            }
        };

    }
}
