package net.onpointcoding.openlightscontroller.blocks;

import li.cil.oc.api.CreativeTab;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.onpointcoding.openlightscontroller.tileentity.LightsControllerTE;

public class LightsControllerBlock extends Block implements ITileEntityProvider {
    public static final String NAME = "openlightscontroller";

    public LightsControllerBlock() {
        super(Material.GLASS);
        setRegistryName(NAME);
        setTranslationKey("openlightscontroller");
        setHardness(.5F);
        setLightLevel(1.0F);
        setCreativeTab(CreativeTab.instance);
    }

    @Override
    public TileEntity createNewTileEntity(World arg0, int arg1) {
        return new LightsControllerTE();
    }
}