package org.openhab.binding.yeelight2.internal.device.property;

import static org.openhab.binding.yeelight2.internal.Yeelight2BindingConstants.CHANNEL_GROUP_BACKGROUND;

/**
 *
 * @author Tiph
 *
 */
abstract class AbstractYeelightBGProperty implements IYeelightDeviceProperty {

    @Override
    public final String getChannelGroupId() {
        return CHANNEL_GROUP_BACKGROUND;
    }

}
