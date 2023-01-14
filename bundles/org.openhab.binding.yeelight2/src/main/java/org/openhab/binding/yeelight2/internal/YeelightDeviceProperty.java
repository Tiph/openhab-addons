package org.openhab.binding.yeelight2.internal;

import static org.openhab.binding.yeelight2.internal.Yeelight2BindingConstants.*;

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
    POWER("power", "set_power", OnOffType.class, OnOffType::from, null, CHANNEL_GROUP_DEFAULT, Constant.onOffToApi),
    DELAYOFF("delayoff", null, StringType.class, StringType::valueOf, null, CHANNEL_GROUP_DEFAULT, null),
    MUSIC_MODE("music_on", null, OnOffType.class, OnOffType::from, null, CHANNEL_GROUP_DEFAULT, null),

    // Main
    MAIN_POWER("main_power", "set_power", OnOffType.class, OnOffType::from, null, CHANNEL_GROUP_DEFAULT,
            Constant.onOffToApi),
    ACTIVE_MODE("active_mode", null, StringType.class, Constant.activeModeFunction, null, CHANNEL_GROUP_DEFAULT, null),
    BRIGHTNESS("bright", "set_bright", PercentType.class, PercentType::valueOf, null, CHANNEL_GROUP_DEFAULT,
            Constant.typeToApi),
    NL_BRIGHTNESS("nl_br", "set_nl_br", PercentType.class, PercentType::valueOf, null, CHANNEL_GROUP_DEFAULT,
            Constant.typeToApi),
    COLOR_TEMPERATURE("ct", "set_ct_abx", DecimalType.class, DecimalType::valueOf, null, CHANNEL_GROUP_DEFAULT,
            Constant.typeToApi),
    RGB("rgb", "set_rgb", HSBType.class, Constant.rgbFunction, null, CHANNEL_GROUP_DEFAULT, Constant.hsbTypeToApi),
    COLOR_MODE("color_mode", null, StringType.class, Constant.colorModeFunction, null, CHANNEL_GROUP_DEFAULT, null),
    FLOWING("flowing", null, OnOffType.class, OnOffType::from, null, CHANNEL_GROUP_DEFAULT, null),
    FLOW_PARAMS("flow_params", null, StringType.class, StringType::valueOf, FLOWING, CHANNEL_GROUP_DEFAULT, null),

    // Background
    BG_POWER("bg_power", "bg_set_power", OnOffType.class, OnOffType::from, null, CHANNEL_GROUP_BACKGROUND,
            Constant.onOffToApi),
    BG_COLOR_TEMPERATURE("bg_ct", "bg_set_ct_abx", DecimalType.class, DecimalType::valueOf, null,
            CHANNEL_GROUP_BACKGROUND, Constant.typeToApi),
    BG_BRIGHTNESS("bg_bright", "bg_set_bright", PercentType.class, PercentType::valueOf, null, CHANNEL_GROUP_BACKGROUND,
            Constant.typeToApi),
    BG_RGB("bg_rgb", "bg_set_rgb", HSBType.class, Constant.rgbFunction, null, CHANNEL_GROUP_BACKGROUND,
            Constant.hsbTypeToApi),
    BG_COLOR_MODE("bg_lmode", null, StringType.class, Constant.colorModeFunction, null, CHANNEL_GROUP_BACKGROUND, null),
    BG_FLOWING("bg_flowing", null, OnOffType.class, OnOffType::from, null, CHANNEL_GROUP_BACKGROUND, null),
    BG_FLOW_PARAMS("bg_flow_params", null, StringType.class, StringType::valueOf, BG_FLOWING, CHANNEL_GROUP_BACKGROUND,
            null),
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

    /**
     * The channel group Id of the property
     */
    private String channelGroupId;

    /**
     * The function to map OpenHab Type to API Yeelight parameter
     */
    private Function<Type, String> typeToApiMappingFunction;

    private static final Map<String, YeelightDeviceProperty> ENUM_MAP;

    static {
        final Map<String, YeelightDeviceProperty> tempMap = new HashMap<>();
        for (YeelightDeviceProperty property : YeelightDeviceProperty.values()) {
            tempMap.put(property.propertyName, property);
        }
        ENUM_MAP = Collections.unmodifiableMap(tempMap);
    }

    YeelightDeviceProperty(String propName, String setterMethodName, Class<? extends Type> associatedType,
            Function<String, Type> mappingFunction, YeelightDeviceProperty channelProperty, String channelGroupId,
            Function<Type, String> typeToApiMappingFunction) {
        this.propertyName = propName;
        this.setterName = setterMethodName;
        this.associatedType = associatedType;
        this.mappingFunction = mappingFunction;
        if (channelProperty == null) {
            this.channelProperty = this;
        } else {
            this.channelProperty = channelProperty;
        }
        this.channelGroupId = channelGroupId;
        this.typeToApiMappingFunction = typeToApiMappingFunction;
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

    public String getChannelGroupId() {
        return channelGroupId;
    }

    public Function<Type, String> getTypeToApiMappingFunction() {
        return typeToApiMappingFunction;
    }

    public static YeelightDeviceProperty fromPropertyName(String value) {
        return ENUM_MAP.get(value);
    }

    private static class Constant {
        static final Function<String, Type> colorModeFunction = v -> {
            switch (v) {
                case "1":
                    return StringType.valueOf("RGB");
                case "2":
                    return StringType.valueOf("CT");
                case "3":
                    return StringType.valueOf("HSV");
                default:
                    return StringType.valueOf("NULL");
            }
        };

        static final Function<String, Type> rgbFunction = v -> {
            int val = Integer.parseInt(v);
            int r = val >>> 16 & 0xFF;
            int g = val >>> 8 & 0xFF;
            int b = val & 0xFF;
            return HSBType.fromRGB(r, g, b);
        };

        static final Function<String, Type> activeModeFunction = v -> {
            if ("0".equals(v)) {
                return StringType.valueOf("SUN");
            } else if ("1".equals(v)) {
                return StringType.valueOf("MOON");
            }
            return StringType.valueOf("NULL");
        };

        static final Function<Type, String> onOffToApi = t -> "\"" + t.toFullString().toLowerCase() + "\"";
        static final Function<Type, String> hsbTypeToApi = t -> Integer.toString(((HSBType) t).getRGB() & 0xFFFFFF);
        static final Function<Type, String> typeToApi = Type::toFullString;

    }
}
