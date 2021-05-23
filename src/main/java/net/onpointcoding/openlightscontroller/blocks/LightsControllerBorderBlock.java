package net.onpointcoding.openlightscontroller.blocks;

import li.cil.oc.api.CreativeTab;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class LightsControllerBorderBlock extends Block {
    public static final String NAME = "openlightscontrollerborder";

    public LightsControllerBorderBlock() {
        super(Material.IRON);
        setRegistryName(NAME);
        setTranslationKey("openlightscontrollerborder");
        setHardness(.5F);
        setLightLevel(1.0F);
        setCreativeTab(CreativeTab.instance);
    }
}
