package net.onpointcoding.openlightscontroller.blocks.tiers;

import li.cil.oc.common.Tier;
import net.onpointcoding.openlightscontroller.blocks.LightsControllerBlockBase;

public class LightsControllerBlockTier3 extends LightsControllerBlockBase {
    public static final String NAME = "openlightscontroller3";

    public LightsControllerBlockTier3() {
        super(Tier.Three());
        setRegistryName(NAME);
    }
}
