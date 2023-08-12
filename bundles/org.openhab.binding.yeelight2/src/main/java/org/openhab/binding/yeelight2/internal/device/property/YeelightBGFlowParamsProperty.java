package org.openhab.binding.yeelight2.internal.device.property;

import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;

/**
 *
 * @author Tiph
 *
 */
class YeelightBGFlowParamsProperty extends AbstractYeelightBGProperty {

    @Override
    public String getPropertyName() {
        return "bg_flow_params";
    }

    @Override
    public Class<? extends State> getOHType() {
        return StringType.class;
    }

}
