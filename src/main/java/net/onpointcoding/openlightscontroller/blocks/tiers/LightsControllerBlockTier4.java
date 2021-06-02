package net.onpointcoding.openlightscontroller.blocks.tiers;

import li.cil.oc.common.Tier;
import net.onpointcoding.openlightscontroller.blocks.LightsControllerBlockBase;

public class LightsControllerBlockTier4 extends LightsControllerBlockBase {
    public static final String NAME = "openlightscontroller4";

    public LightsControllerBlockTier4() {
        super(Tier.Four());
        setRegistryName(NAME);
    }
}
