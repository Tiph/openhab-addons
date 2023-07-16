package org.openhab.binding.yeelight2.internal.device.property;

import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;

/**
 *
 * @author Tiph
 *
 */
class YeelightFlowParamsProperty extends AbstractYeelightMainProperty {

    @Override
    public String getPropertyName() {
        return "flow_params";
    }

    @Override
    public State getState(String val) {
        return StringType.valueOf(val);
    }

}
