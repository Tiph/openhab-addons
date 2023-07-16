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
        // TODO Auto-generated method stub
        return "music_on";
    }

    @Override
    public State getState(String val) {
        return OnOffType.from(val);
    }

}
