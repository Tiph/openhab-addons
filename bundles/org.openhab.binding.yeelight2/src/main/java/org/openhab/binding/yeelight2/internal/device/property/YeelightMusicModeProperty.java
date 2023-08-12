package org.openhab.binding.yeelight2.internal.device.property;

import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.State;

/**
 *
 * @author Tiph
 *
 */
class YeelightMusicModeProperty extends AbstractYeelightMainProperty {

    @Override
    public String getPropertyName() {
        return "music_on";
    }

    @Override
    public Class<? extends State> getOHType() {
        return OnOffType.class;
    }

}
