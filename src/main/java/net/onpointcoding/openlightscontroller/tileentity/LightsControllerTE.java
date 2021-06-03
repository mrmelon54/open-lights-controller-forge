package net.onpointcoding.openlightscontroller.tileentity;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import li.cil.oc.common.Tier;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.onpointcoding.openlightscontroller.blocks.LightsControllerBorderBlockBase;
import net.onpointcoding.openlightscontroller.enums.AxisDirection;
import pcl.openlights.tileentity.OpenLightTE;

import javax.annotation.Nullable;
import java.util.Arrays;

@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")
public class LightsControllerTE extends TileEntity implements SimpleComponent {
    private int width = 0;
    private int height = 0;
    private int[] color = new int[0];
    private int[] brightness = new int[0];

    private boolean up = false;
    private boolean down = false;
    private boolean north = false;
    private boolean east = false;
    private boolean south = false;
    private boolean west = false;

    private boolean valid = false;
    private AxisDirection firstDirection = AxisDirection.None;
    private AxisDirection secondDirection = AxisDirection.None;

    private int MAX_BORDER_LENGTH = 0;
    private String firstSideName = "NONE";
    private String secondSideName = "NONE";
    private int tier = 0;

    public LightsControllerTE() {
        // Do nothing...
    }

    public LightsControllerTE(int tier) {
        // Load starting tier
        this.tier = tier;
        setupMaxBorderLength();
    }

    public void setupMaxBorderLength() {
        if (tier == Tier.One()) MAX_BORDER_LENGTH = 4;
        else if (tier == Tier.Two()) MAX_BORDER_LENGTH = 8;
        else if (tier == Tier.Three()) MAX_BORDER_LENGTH = 16;

            // Creative tier is much higher
        else if (tier == Tier.Four()) MAX_BORDER_LENGTH = 64;
    }

    @Override
    public String getComponentName() {
        return "openlightscontroller";
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        valid = nbt.getBoolean("is-valid");

        width = nbt.getInteger("grid-width");
        height = nbt.getInteger("grid-height");
        if (width < 1 || width > MAX_BORDER_LENGTH) valid = false;
        if (height < 1 || height > MAX_BORDER_LENGTH) valid = false;

        firstDirection = AxisDirection.getEnum(nbt.getString("first-direction"));
        secondDirection = AxisDirection.getEnum(nbt.getString("second-direction"));
        firstSideName = nbt.getString("first-side-name");
        secondSideName = nbt.getString("second-side-name");
        if (firstDirection == AxisDirection.None) valid = false;
        if (secondDirection == AxisDirection.None) valid = false;
        String[] allowedDirections = {"UP", "DOWN", "NORTH", "EAST", "SOUTH", "WEST"};
        if (!Arrays.asList(allowedDirections).contains(firstSideName)) valid = false;
        if (!Arrays.asList(allowedDirections).contains(secondSideName)) valid = false;

        up = nbt.getBoolean("direction-up");
        down = nbt.getBoolean("direction-down");
        north = nbt.getBoolean("direction-north");
        east = nbt.getBoolean("direction-east");
        south = nbt.getBoolean("direction-south");
        west = nbt.getBoolean("direction-west");
        if ((up ? 1 : 0) + (down ? 1 : 0) + (north ? 1 : 0) + (east ? 1 : 0) + (south ? 1 : 0) + (west ? 1 : 0) != 2)
            valid = false;

        color = nbt.getIntArray("color-flat");
        brightness = nbt.getIntArray("brightness-flat");
        if (color.length != width * height) valid = false;
        if (brightness.length != width * height) valid = false;

        tier = nbt.getInteger("tier");
        setupMaxBorderLength();
    }

    private int[] getColors() {
        return color;
    }

    private int[] getBrightnesses() {
        return brightness;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        nbt.setInteger("grid-width", width);
        nbt.setInteger("grid-height", height);

        nbt.setString("first-direction", firstDirection.toString());
        nbt.setString("second-direction", secondDirection.toString());
        nbt.setString("first-side-name", firstSideName);
        nbt.setString("second-side-name", secondSideName);

        nbt.setBoolean("direction-up", up);
        nbt.setBoolean("direction-down", down);
        nbt.setBoolean("direction-north", north);
        nbt.setBoolean("direction-east", east);
        nbt.setBoolean("direction-south", south);
        nbt.setBoolean("direction-west", west);

        nbt.setBoolean("is-valid", valid);

        nbt.setIntArray("color-flat", getColors());
        nbt.setIntArray("brightness-flat", getBrightnesses());

        nbt.setInteger("tier", tier);

        return nbt;
    }

    @Callback(doc = "function apply():string; Applies the current cached lighting data to the lights.")
    public Object[] apply(Context context, Arguments args) throws Exception {
        if (!valid)
            throw new Exception("Invalid light controller setup, fix the issue then run `calibrate()`.");

        BlockPos hori, vert;
        TileEntity tileEntity;
        OpenLightTE lightEntity;
        for (int x = 1; x < width + 1; x++) {
            for (int y = 1; y < height + 1; y++) {
                hori = moveRelativeToBlock(getPos(), firstDirection, x);
                vert = moveRelativeToBlock(hori, secondDirection, y);
                tileEntity = getWorld().getTileEntity(vert);
                if (tileEntity instanceof OpenLightTE) {
                    lightEntity = (OpenLightTE) tileEntity;

                    // Update color and brightness data using nbt tags as fields are private
                    NBTTagCompound fakeNbtTag = new NBTTagCompound();
                    lightEntity.writeToNBT(fakeNbtTag);
                    fakeNbtTag.setInteger("color", color[(y - 1) * width + (x - 1)]);
                    fakeNbtTag.setInteger("brightness", brightness[(y - 1) * width + (x - 1)]);
                    lightEntity.readFromNBT(fakeNbtTag);

                    // Implements protected method `lightEntity.doBlockUpdate()`
                    lightEntity.getUpdateTag();
                    lightEntity.getWorld().notifyBlockUpdate(lightEntity.getPos(), lightEntity.getWorld().getBlockState(lightEntity.getPos()), lightEntity.getWorld().getBlockState(lightEntity.getPos()), 2);
                    lightEntity.getWorld().markBlockRangeForRenderUpdate(lightEntity.getPos(), lightEntity.getPos());
                    lightEntity.markDirty();
                }
            }
        }
        return new Object[]{"OK"};
    }

    @Callback(doc = "function isCalibrated():bool; Checks if the light controller is calibrated.")
    public Object[] isCalibrated(Context context, Arguments args) {
        return new Object[]{valid};
    }

    @Callback(doc = "function calibrate(direction:string):string; Calibrate light controller.")
    public Object[] calibrate(Context context, Arguments args) throws Exception {
        valid = false;

        if (args.count() != 1)
            throw new Exception("Invalid number of arguments, expected axis directions");

        String direction = args.checkString(0);
        if (direction.length() != 2)
            throw new Exception("Invalid direction argument, expected two letters");

        String PFirstDirection = direction.substring(0, 1).toLowerCase();
        String PSecondDirection = direction.substring(1, 2).toLowerCase();
        if (!(PFirstDirection.equals("x") || PFirstDirection.equals("y") || PFirstDirection.equals("z")))
            throw new Exception("Invalid direction argument, expected axis letter (x, y, z)");
        if (!(PSecondDirection.equals("x") || PSecondDirection.equals("y") || PSecondDirection.equals("z")))
            throw new Exception("Invalid direction argument, expected axis letter (x, y, z)");

        firstDirection = AxisDirection.valueOf(PFirstDirection.toUpperCase());
        secondDirection = AxisDirection.valueOf(PSecondDirection.toUpperCase());

        if (firstDirection == secondDirection)
            throw new Exception("Invalid direction argument, both directions must be different");

        // Get border axes directions
        World world = getWorld();
        up = world.getBlockState(getPos().up()).getBlock() instanceof LightsControllerBorderBlockBase;
        down = world.getBlockState(getPos().down()).getBlock() instanceof LightsControllerBorderBlockBase;
        north = world.getBlockState(getPos().north()).getBlock() instanceof LightsControllerBorderBlockBase;
        east = world.getBlockState(getPos().east()).getBlock() instanceof LightsControllerBorderBlockBase;
        south = world.getBlockState(getPos().south()).getBlock() instanceof LightsControllerBorderBlockBase;
        west = world.getBlockState(getPos().west()).getBlock() instanceof LightsControllerBorderBlockBase;

        if ((up && down) || (north && south) || (east && west))
            throw new Exception("Invalid light controller setup, expected two different axes for the borders");

        // Check for invalid border configuration
        int a = (up ? 1 : 0) + (down ? 1 : 0) + (north ? 1 : 0) + (east ? 1 : 0) + (south ? 1 : 0) + (west ? 1 : 0);
        if (a != 2)
            throw new Exception("Invalid light controller setup, expected only two border axes");

        // Check if direction argument matches border axes
        if ((up || down) && !(firstDirection == AxisDirection.Y || secondDirection == AxisDirection.Y))
            throw new Exception("Invalid light controller setup, expected the direction to match the border axes");
        if ((north || south) && !(firstDirection == AxisDirection.Z || secondDirection == AxisDirection.Z))
            throw new Exception("Invalid light controller setup, expected the direction to match the border axes");
        if ((east || west) && !(firstDirection == AxisDirection.X || secondDirection == AxisDirection.X))
            throw new Exception("Invalid light controller setup, expected the direction to match the border axes");

        width = detectSideLength(world, firstDirection);
        height = detectSideLength(world, secondDirection);

        color = new int[width * height];
        brightness = new int[width * height];

        firstSideName = (firstDirection == AxisDirection.X ? (east ? "EAST" : "WEST") : (firstDirection == AxisDirection.Y ? (up ? "UP" : "DOWN") : (north ? "NORTH" : "SOUTH")));
        secondSideName = (secondDirection == AxisDirection.X ? (east ? "EAST" : "WEST") : (secondDirection == AxisDirection.Y ? (up ? "UP" : "DOWN") : (north ? "NORTH" : "SOUTH")));

        valid = true;

        return new Object[]{"OK"};
    }

    private int detectSideLength(World world, AxisDirection direction) {
        int i;

        for (i = 1; i < MAX_BORDER_LENGTH + 1; i++) {
            if (!(getBlockOnAxis(world, direction, i) instanceof LightsControllerBorderBlockBase)) break;
        }

        return i - 1;
    }

    private Block getBlockOnAxis(World world, AxisDirection direction, int i) {
        return world.getBlockState(moveRelativeToBlock(getPos(), direction, i)).getBlock();
    }

    private BlockPos moveRelativeToBlock(BlockPos position, AxisDirection direction, int i) {
        if (direction == AxisDirection.Y) {
            if (up) return position.up(i);
            if (down) return position.down(i);
        } else if (direction == AxisDirection.Z) {
            if (north) return position.north(i);
            if (south) return position.south(i);
        } else if (direction == AxisDirection.X) {
            if (east) return position.east(i);
            if (west) return position.west(i);
        }

        return position;
    }

    @Callback(doc = "function getBorderAxes():string, string; Get border axes.")
    public Object[] getBorderAxes(Context context, Arguments args) throws Exception {
        if (!valid)
            throw new Exception("Invalid light controller setup, fix the issue then run `calibrate()`.");

        return new Object[]{firstDirection.toString(), secondDirection.toString()};
    }

    @Callback(doc = "function getBorderDirections():string, string; Get active border directions.")
    public Object[] getBorderDirections(Context context, Arguments args) throws Exception {
        if (!valid)
            throw new Exception("Invalid light controller setup, fix the issue then run `calibrate()`.");

        return new Object[]{firstSideName, secondSideName};
    }

    @Callback(doc = "function getSize():number, number; Get size of light grid. ")
    public Object[] getSize(Context context, Arguments args) throws Exception {
        if (!valid)
            throw new Exception("Invalid light controller setup, fix the issue then run `calibrate()`.");

        return new Object[]{width, height};
    }

    @Callback(doc = "function setColor(color:number, {x:number, y:number}):string; Set the light color as an RGB value. Returns the new color as an RGB hex string. Use `tonumber(value, 16)` to convert return value to a usable numeric value. The controller caches this until `apply()` is called.")
    public Object[] setColor(Context context, Arguments args) throws Exception {
        // Must be an odd number of arguments but can't be fewer than 3
        if (args.count() < 3 || args.count() % 2 == 0)
            throw new Exception("Invalid number of arguments, expected a color then (x, y) coordinates");

        if (!valid)
            throw new Exception("Invalid light controller setup, fix the issue then run `calibrate()`.");

        int buf = args.checkInteger(0);

        if ((buf > 0xFFFFFF) || (buf < 0x000000))
            throw new Exception("Valid RGB range is 0x000000 to 0xFFFFFF");

        int l = args.count() / 2;
        int x, y;
        for (int i = 0; i < l; i++) {
            x = args.checkInteger(i + 1);
            y = args.checkInteger(i + 2);
            if ((x < 1 || x > width) || (y < 1 || y > height))
                throw new Exception("Invalid coordinate (" + x + ", " + y + ")");
            color[(y - 1) * width + (x - 1)] = buf;
        }

        return new Object[]{getColorString(buf)};
    }

    @Callback(doc = "function fillColor(color:number, [x1:number, y1:number, x2:number, y2:number]):string; Fills a specific region or the whole grid to a specific RGB value. Returns the new color as an RGB hex string. Use `tonumber(value, 16)` to convert return value to a usable numeric value. The controller caches this until `apply()` is called.")
    public Object[] fillColor(Context context, Arguments args) throws Exception {
        // Must be 1 or 5 arguments
        if (args.count() != 1 && args.count() != 5)
            throw new Exception("Invalid number of arguments, expected a color and either a pair of coordinates or nothing");

        if (!valid)
            throw new Exception("Invalid light controller setup, fix the issue then run `calibrate()`.");

        int buf = args.checkInteger(0);

        if ((buf > 0xFFFFFF) || (buf < 0x000000))
            throw new Exception("Valid RGB range is 0x000000 to 0xFFFFFF");

        if (args.count() == 5) {
            int x1 = args.checkInteger(1);
            int y1 = args.checkInteger(2);
            int x2 = args.checkInteger(3);
            int y2 = args.checkInteger(4);

            if ((x1 < 1 || x1 > width) || (y1 < 1 || y1 > height))
                throw new Exception("Invalid coordinate (" + x1 + ", " + y1 + ")");
            if ((x2 < 1 || x2 > width) || (y2 < 1 || y2 > height))
                throw new Exception("Invalid coordinate (" + x2 + ", " + y2 + ")");

            fillRegionColor(buf, x1 - 1, y1 - 1, x2 - 1, y2 - 1);
        } else {
            fillRegionColor(buf, 0, 0, width - 1, height - 1);
        }

        return new Object[]{buf};
    }

    @Callback(doc = "function setBrightness(brightness:number, {x:number, y:number}):number; Set the brightness of the light. Returns the new brightness. The controller caches this until `apply()` is called.")
    public Object[] setBrightness(Context context, Arguments args) throws Exception {
        // Must be an odd number of arguments but can't be fewer than 3
        if (args.count() < 3 || args.count() % 2 == 0)
            throw new Exception("Invalid number of arguments, expected a brightness then (x, y) coordinates");

        if (!valid)
            throw new Exception("Invalid light controller setup, fix the issue then run `calibrate()`.");

        int buf = args.checkInteger(0);

        if ((buf > 15) || (buf < 0))
            throw new Exception("Valid brightness range is 0 to 15");

        int l = args.count() / 2;
        int x, y;
        for (int i = 0; i < l; i++) {
            x = args.checkInteger(i * 2 + 1);
            y = args.checkInteger(i * 2 + 2);
            if ((x < 1 || x > width) || (y < 1 || y > height))
                throw new Exception("Invalid coordinate (" + x + ", " + y + ")");
            brightness[(y - 1) * width + (x - 1)] = buf;
        }

        return new Object[]{buf};
    }

    @Callback(doc = "function fillBrightness(brightness:number, [x1:number, y1:number, x2:number, y2:number]):number; Fills a specific region or the whole grid to a specific brightness. Returns the new brightness. The controller caches this until `apply()` is called.")
    public Object[] fillBrightness(Context context, Arguments args) throws Exception {
        // Must be 1 or 5 arguments
        if (args.count() != 1 && args.count() != 5)
            throw new Exception("Invalid number of arguments, expected a brightness and either a pair of coordinates or nothing");

        if (!valid)
            throw new Exception("Invalid light controller setup, fix the issue then run `calibrate()`.");

        int buf = args.checkInteger(0);

        if ((buf > 15) || (buf < 0))
            throw new Exception("Valid brightness range is 0 to 15");

        if (args.count() == 5) {
            int x1 = args.checkInteger(1);
            int y1 = args.checkInteger(2);
            int x2 = args.checkInteger(3);
            int y2 = args.checkInteger(4);

            if ((x1 < 1 || x1 > width) || (y1 < 1 || y1 > height))
                throw new Exception("Invalid coordinate (" + x1 + ", " + y1 + ")");
            if ((x2 < 1 || x2 > width) || (y2 < 1 || y2 > height))
                throw new Exception("Invalid coordinate (" + x2 + ", " + y2 + ")");

            fillRegionBrightness(buf, x1 - 1, y1 - 1, x2 - 1, y2 - 1);
        } else {
            fillRegionBrightness(buf, 0, 0, width - 1, height - 1);
        }

        return new Object[]{buf};
    }

    @Callback(doc = "function getColor({x:number, y:number}):{string}; Get the light color as an RGB hex string. Use `tonumber(value, 16)` to convert return value to a usable numeric value. The controller returns the current color use `getCachedColor()` to get the currently cached color.")
    public Object[] getColor(Context context, Arguments args) throws Exception {
        // This can't have an odd number of arguments or less than 2 arguments
        if (args.count() % 2 == 1 || args.count() < 2)
            throw new Exception("Invalid number of arguments, expected (x, y) coordinates");

        if (!valid)
            throw new Exception("Invalid light controller setup, fix the issue then run `calibrate()`.");

        int l = args.count() / 2;
        Object[] o = new Object[l];

        int x, y;
        BlockPos hori, vert;
        TileEntity tileEntity;
        OpenLightTE lightEntity;

        for (int i = 0; i < l; i++) {
            x = args.checkInteger(i * 2);
            y = args.checkInteger(i * 2 + 1);
            if ((x < 1 || x > width) || (y < 1 || y > height))
                throw new Exception("Invalid coordinate (" + x + ", " + y + ")");

            hori = moveRelativeToBlock(getPos(), firstDirection, x);
            vert = moveRelativeToBlock(hori, secondDirection, y);

            tileEntity = getWorld().getTileEntity(vert);
            if (tileEntity instanceof OpenLightTE) {
                lightEntity = (OpenLightTE) tileEntity;
                o[i] = lightEntity.getColorString();
            }
        }

        return o;
    }

    @Callback(doc = "function getBrightness({x:number, y:number}):{number}; Get brightness of the light. The controller returns the current brightness use `getCachedBrightness()` to get the currently cached brightness.")
    public Object[] getBrightness(Context context, Arguments args) throws Exception {
        // This can't have an odd number of arguments or less than 2 arguments
        if (args.count() % 2 == 1 || args.count() < 2)
            throw new Exception("Invalid number of arguments, expected (x, y) coordinates");

        if (!valid)
            throw new Exception("Invalid light controller setup, fix the issue then run `calibrate()`.");

        int l = args.count() / 2;
        Object[] o = new Object[l];

        int x, y;
        BlockPos hori, vert;
        TileEntity tileEntity;
        OpenLightTE lightEntity;

        for (int i = 0; i < l; i++) {
            x = args.checkInteger(i * 2);
            y = args.checkInteger(i * 2 + 1);
            if ((x < 1 || x > width) || (y < 1 || y > height))
                throw new Exception("Invalid coordinate (" + x + ", " + y + ")");

            hori = moveRelativeToBlock(getPos(), firstDirection, x - 1);
            vert = moveRelativeToBlock(hori, secondDirection, y - 1);

            tileEntity = getWorld().getTileEntity(vert);
            if (tileEntity instanceof OpenLightTE) {
                lightEntity = (OpenLightTE) tileEntity;
                o[i] = lightEntity.getBrightness();
            }
        }

        return o;
    }

    @Callback(doc = "function getCachedColor({x:number, y:number}):{string}; Get the light color as an RGB hex string. Use `tonumber(value, 16)` to convert return value to a usable numeric value. Use `getColor()` to get the current color of the light.")
    public Object[] getCachedColor(Context context, Arguments args) throws Exception {
        // This can't have an odd number of arguments or less than 2 arguments
        if (args.count() % 2 == 1 || args.count() < 2)
            throw new Exception("Invalid number of arguments, expected (x, y) coordinates");

        if (!valid)
            throw new Exception("Invalid light controller setup, fix the issue then run `calibrate()`.");

        int l = args.count() / 2;
        Object[] o = new Object[l];

        int x, y;
        for (int i = 0; i < l; i++) {
            x = args.checkInteger(i * 2);
            y = args.checkInteger(i * 2 + 1);
            if ((x < 1 || x > width) || (y < 1 || y > height))
                throw new Exception("Invalid coordinate (" + x + ", " + y + ")");
            o[i] = getColorString(color[(y - 1) * width + (x - 1)]);
        }

        return o;
    }

    @Callback(doc = "function getCachedBrightness({x:number, y:number}):{number}; Get brightness of the light. Use `getBrightness()` to get the current brightness of the light.")
    public Object[] getCachedBrightness(Context context, Arguments args) throws Exception {
        // This can't have an odd number of arguments or less than 2 arguments
        if (args.count() % 2 == 1 || args.count() < 2)
            throw new Exception("Invalid number of arguments, expected (x, y) coordinates");

        if (!valid)
            throw new Exception("Invalid light controller setup, fix the issue then run `calibrate()`.");

        int l = args.count() / 2;
        Object[] o = new Object[l];

        int x, y;
        for (int i = 0; i < l; i++) {
            x = args.checkInteger(i * 2);
            y = args.checkInteger(i * 2 + 1);
            if ((x < 1 || x > width) || (y < 1 || y > height))
                throw new Exception("Invalid coordinate (" + x + ", " + y + ")");
            o[i] = brightness[(y - 1) * width + (x - 1)];
        }

        return o;
    }

    @Callback(doc = "function getMaximumBorderSize():number; Get the maximum border size. This changes depending on the tier of the light controller.")
    public Object[] getMaximumBorderSize(Context context, Arguments args) {
        return new Object[]{MAX_BORDER_LENGTH};
    }

    public int getMaximumBorderSize() {
        return MAX_BORDER_LENGTH;
    }

    private void fillRegionColor(int buf, int x1, int y1, int x2, int y2) {
        if (x1 > x2) {
            int tmp = x2;
            x2 = x1;
            x1 = tmp;
        }
        if (y1 > y2) {
            int tmp = y2;
            y2 = y1;
            y1 = tmp;
        }

        for (int x = x1; x < x2 + 1; x++)
            for (int y = y1; y < y2 + 1; y++)
                color[y * width + x] = buf;
    }

    private void fillRegionBrightness(int buf, int x1, int y1, int x2, int y2) {
        if (x1 > x2) {
            int tmp = x2;
            x2 = x1;
            x1 = tmp;
        }
        if (y1 > y2) {
            int tmp = y2;
            y2 = y1;
            y1 = tmp;
        }

        for (int x = x1; x < x2 + 1; x++)
            for (int y = y1; y < y2 + 1; y++)
                brightness[y * width + x] = buf;
    }

    @Override
    @Nullable
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(getPos(), 0, getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound tagCom = super.getUpdateTag();
        writeToNBT(tagCom);
        return tagCom;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        this.readFromNBT(tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        readFromNBT(packet.getNbtCompound());
        world.notifyBlockUpdate(getPos(), world.getBlockState(getPos()), world.getBlockState(getPos()), 3);
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return (oldState.getBlock() != newState.getBlock());
    }

    public String getColorString(int v) {
        return String.format("%06X", (0xFFFFFF & v));
    }

    @Override
    public void validate() {
        super.validate();
    }

    @Override
    public void invalidate() {
        super.invalidate();
    }
}