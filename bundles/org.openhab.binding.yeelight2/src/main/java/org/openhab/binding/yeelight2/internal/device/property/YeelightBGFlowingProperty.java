package org.openhab.binding.yeelight2.internal.device.property;

import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.State;

/**
 *
 * @author Tiph
 *
 */
class YeelightBGFlowingProperty extends AbstractYeelightBGProperty {

    @Override
    public String getPropertyName() {
        return "bg_flowing";
    }

    @Override
    public State getState(String val) {
        return OnOffType.from(val);
    }

}
