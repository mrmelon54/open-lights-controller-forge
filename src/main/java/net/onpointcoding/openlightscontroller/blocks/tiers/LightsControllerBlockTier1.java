package net.onpointcoding.openlightscontroller.blocks.tiers;

import li.cil.oc.common.Tier;
import net.onpointcoding.openlightscontroller.blocks.LightsControllerBlockBase;

public class LightsControllerBlockTier1 extends LightsControllerBlockBase {
    public static final String NAME = "openlightscontroller1";

    public LightsControllerBlockTier1() {
        super(Tier.One());
        setRegistryName(NAME);
    }
}
