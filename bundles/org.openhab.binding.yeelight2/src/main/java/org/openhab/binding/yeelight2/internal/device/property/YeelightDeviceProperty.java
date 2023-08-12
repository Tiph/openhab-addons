package org.openhab.binding.yeelight2.internal.device.property;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 *
 * @author Tiph
 *
 */
public enum YeelightDeviceProperty {

    // Global
    POWER(new YeelightPowerProperty()),
    DELAYOFF(new YeelightDelayOffProperty()),
    MUSIC_MODE(new YeelightMusicModeProperty()),

    // Main
    MAIN_POWER(new YeelightMainPowerProperty()),
    ACTIVE_MODE(new YeelightActiveModeProperty()),
    BRIGHTNESS(new YeelightBrightnessProperty()),
    NL_BRIGHTNESS(new YeelightNLBrightnessProperty()),
    COLOR_TEMPERATURE(new YeelightColorTemperatureProperty()),
    RGB(new YeelightRGBProperty()),
    COLOR_MODE(new YeelightColorModeProperty()),
    FLOWING(new YeelightFlowingProperty()),
    FLOW_PARAMS(new YeelightFlowParamsProperty(), FLOWING),

    // Background
    BG_POWER(new YeelightBGPowerProperty()),
    BG_COLOR_TEMPERATURE(new YeelightBGColorTemperatureProperty()),
    BG_BRIGHTNESS(new YeelightBGBrightnessProperty()),
    BG_RGB(new YeelightBGRGBProperty()),
    BG_COLOR_MODE(new YeelightBGColorModeProperty()),
    BG_FLOWING(new YeelightBGFlowingProperty()),
    BG_FLOW_PARAMS(new YeelightBGFlowParamsProperty(), BG_FLOWING),
    //
    ;

    private final IYeelightDeviceProperty deviceProp;

    /**
     * The property to check for a value to determine if channel (property) is supported by the device
     */
    private final YeelightDeviceProperty propAvailibility;

    private static final Map<String, YeelightDeviceProperty> THIS_BY_PROPERTY_NAME;

    private YeelightDeviceProperty(IYeelightDeviceProperty deviceProperty) {
        this(deviceProperty, null);
    }

    private YeelightDeviceProperty(IYeelightDeviceProperty deviceProperty, YeelightDeviceProperty propAvailibility) {
        this.deviceProp = deviceProperty;
        this.propAvailibility = propAvailibility;
    }

    static {
        final Map<String, YeelightDeviceProperty> tempMap = new HashMap<>();
        for (YeelightDeviceProperty property : YeelightDeviceProperty.values()) {
            tempMap.put(property.getPropertyName(), property);
        }
        THIS_BY_PROPERTY_NAME = Collections.unmodifiableMap(tempMap);
    }

    public static YeelightDeviceProperty fromPropertyName(String value) {
        return THIS_BY_PROPERTY_NAME.get(value);
    }

    public String getChannelGroupId() {
        return deviceProp.getChannelGroupId();
    }

    public String getPropertyName() {
        return deviceProp.getPropertyName();
    }

    public State getState(String val) {
        return deviceProp.getState(val);
    }

    public YeelightDeviceProperty getChannelProperty() {
        return propAvailibility != null ? propAvailibility : this;
    }

    public boolean isWrittable() {
        return deviceProp instanceof IYeelightDeviceWritableProperty;
    }

    public String getSetterName() {
        try {
            return ((IYeelightDeviceWritableProperty) deviceProp).getSetterName();
        } catch (ClassCastException e) {
            throw new IllegalCallerException("Property not writtable");
        }
    }

    public String getSetterValue(Command command) {
        try {
            return ((IYeelightDeviceWritableProperty) deviceProp).getSetterValue(command);
        } catch (ClassCastException e) {
            throw new IllegalCallerException("Property not writtable");
        }
    }

}
