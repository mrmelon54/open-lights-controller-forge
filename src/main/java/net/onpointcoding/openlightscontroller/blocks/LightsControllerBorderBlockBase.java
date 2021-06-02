package net.onpointcoding.openlightscontroller.blocks;

import li.cil.oc.api.CreativeTab;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.onpointcoding.openlightscontroller.OpenLightsController.Blocks;
import net.onpointcoding.openlightscontroller.enums.AxisDirection;
import net.onpointcoding.openlightscontroller.tileentity.LightsControllerTE;

import java.util.Arrays;

public class LightsControllerBorderBlockBase extends Block {
    public static final String NAME = "openlightscontrollerborder";
    public static final PropertyEnum<AxisDirection> AXIS = PropertyEnum.create("axis", AxisDirection.class);
    public static final PropertyBool FLIPPED = PropertyBool.create("flipped");
    public static final PropertyBool NEAR_CONTROLLER = PropertyBool.create("near_controller");

    public LightsControllerBorderBlockBase() {
        super(Material.IRON);
        setRegistryName(NAME);
        setTranslationKey("openlightscontrollerborder");
        setHardness(.5F);
        setLightLevel(0F);
        setCreativeTab(CreativeTab.instance);

        // By default none of the sides are connected.
        this.setDefaultState(this.blockState.getBaseState().withProperty(AXIS, AxisDirection.None).withProperty(FLIPPED, false).withProperty(NEAR_CONTROLLER, false));
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos position) {
        // Creates the state to use for the block. This is where we check if every side is
        // connectable or not.
        boolean ok = false;
        AxisDirection blockStateAxis = AxisDirection.None;
        boolean stateAxisFlipped = false;
        boolean stateNearController = false;

        for (EnumFacing facingDirection : EnumFacing.values()) {
            if (ok) break;
            for (int i = 0; i < 100; i++) {
                BlockPos offsetPos = position.offset(facingDirection, i + 1);
                IBlockState otherState = world.getBlockState(offsetPos);
                if (isController(otherState)) {
                    stateNearController = i == 0;
                    TileEntity controllerTileEntity = world.getTileEntity(offsetPos);
                    if (controllerTileEntity instanceof LightsControllerTE) {
                        LightsControllerTE lightsControllerTileEntity = (LightsControllerTE) controllerTileEntity;
                        if (i >= lightsControllerTileEntity.getMaximumBorderSize()) break;
                    }
                    switch (facingDirection) {
                        case EAST:
                            stateAxisFlipped = true;
                        case WEST:
                            blockStateAxis = AxisDirection.X;
                            break;
                        case NORTH:
                            stateAxisFlipped = true;
                        case SOUTH:
                            blockStateAxis = AxisDirection.Z;
                            break;
                        case UP:
                            stateAxisFlipped = true;
                        case DOWN:
                            blockStateAxis = AxisDirection.Y;
                            break;
                        default:
                            blockStateAxis = AxisDirection.None;
                    }
                    BlockPos oppositeOffsetPos = position.offset(facingDirection.getOpposite());
                    if (isController(world.getBlockState(oppositeOffsetPos))) {
                        stateAxisFlipped = !stateAxisFlipped;
                        stateNearController = true;
                    }
                    ok = true;
                    break;
                } else if (otherState.getBlock() == this) {
                    continue;
                }
                break;
            }
        }

        return state.withProperty(AXIS, blockStateAxis).withProperty(FLIPPED, stateAxisFlipped).withProperty(NEAR_CONTROLLER, stateNearController);
    }

    private boolean isController(IBlockState blockState) {
        if (blockState == null) return false;
        Block block = blockState.getBlock();
        return Arrays.asList(Blocks.openlightscontroller1, Blocks.openlightscontroller2, Blocks.openlightscontroller3, Blocks.openlightscontroller4).contains(block);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        // This returns the connected properties as part of the BlockStateContainer. This is
        // used all over the place in vanilla to represent properties without having them take
        // up metadata values.
        return new BlockStateContainer(this, AXIS, FLIPPED, NEAR_CONTROLLER);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }
}
