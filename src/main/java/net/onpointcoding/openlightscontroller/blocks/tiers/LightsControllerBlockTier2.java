package net.onpointcoding.openlightscontroller.blocks.tiers;

import li.cil.oc.common.Tier;
import net.onpointcoding.openlightscontroller.blocks.LightsControllerBlockBase;

public class LightsControllerBlockTier2 extends LightsControllerBlockBase {
    public static final String NAME = "openlightscontroller2";

    public LightsControllerBlockTier2() {
        super(Tier.Two());
        setRegistryName(NAME);
    }
}
