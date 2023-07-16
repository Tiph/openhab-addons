package org.openhab.binding.yeelight2.internal.device.property;

import static org.openhab.binding.yeelight2.internal.Yeelight2BindingConstants.CHANNEL_GROUP_DEFAULT;

/**
 *
 * @author Tiph
 *
 */
abstract class AbstractYeelightMainProperty implements IYeelightDeviceProperty {

    @Override
    public String getChannelGroupId() {
        return CHANNEL_GROUP_DEFAULT;
    }

}
