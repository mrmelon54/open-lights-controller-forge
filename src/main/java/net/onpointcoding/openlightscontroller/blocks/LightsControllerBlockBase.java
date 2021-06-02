package net.onpointcoding.openlightscontroller.blocks;

import li.cil.oc.api.CreativeTab;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.onpointcoding.openlightscontroller.tileentity.LightsControllerTE;

public class LightsControllerBlockBase extends Block implements ITileEntityProvider {
    private final int tier;

    public LightsControllerBlockBase(int tier) {
        super(Material.GLASS);
        setTranslationKey("openlightscontroller" + (tier + 1));
        setHardness(.5F);
        setLightLevel(0F);
        setCreativeTab(CreativeTab.instance);
        this.tier = tier;
    }

    @Override
    public TileEntity createNewTileEntity(World arg0, int arg1) {
        return new LightsControllerTE(tier);
    }
}