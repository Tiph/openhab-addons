package org.openhab.binding.yeelight2.internal.device.property;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.openhab.core.types.State;

/**
 *
 * @author Tiph
 *
 */
public enum YeelightDeviceProperty implements IYeelightDeviceProperty {

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
     * The property to check for a value to determine if channel is supported
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

    @Override
    public String getChannelGroupId() {
        return deviceProp.getChannelGroupId();
    }

    @Override
    public String getPropertyName() {
        return deviceProp.getPropertyName();
    }

    @Override
    public State getState(String val) {
        return deviceProp.getState(val);
    }

    @Override
    public Optional<YeelightDevicePropertySetterMethod> getSetter() {
        return deviceProp.getSetter();
    }

    public YeelightDeviceProperty getChannelProperty() {
        return propAvailibility != null ? propAvailibility : this;
    }

}
