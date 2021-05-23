package net.onpointcoding.openlightscontroller;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.onpointcoding.openlightscontroller.blocks.LightsControllerBlock;
import net.onpointcoding.openlightscontroller.blocks.LightsControllerBorderBlock;
import net.onpointcoding.openlightscontroller.tileentity.LightsControllerTE;

@Mod(
        modid = OpenLightsController.MOD_ID,
        name = OpenLightsController.MOD_NAME,
        version = OpenLightsController.VERSION,
        useMetadata = true,
        dependencies = "after:opencomputers"
)
public class OpenLightsController {

    public static final String MOD_ID = "openlightscontroller";
    public static final String MOD_NAME = "OpenLightsController";
    public static final String VERSION = "1.0.0";

    /**
     * This is the instance of your mod as created by Forge. It will never be null.
     */
    @Mod.Instance(MOD_ID)
    public static OpenLightsController INSTANCE;

    /**
     * This is the first initialization event. Register tile entities here.
     * The registry events below will have fired prior to entry to this method.
     */
    @Mod.EventHandler
    public void preinit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(ObjectRegistryHandler.class);
        GameRegistry.registerTileEntity(LightsControllerTE.class, new ResourceLocation(OpenLightsController.MOD_ID, "OpenLightsControllerTE"));
    }

    /**
     * This is the second initialization event. Register custom recipes
     */
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
    }

    /**
     * This is the final initialization event. Register actions from other mods here
     */
    @Mod.EventHandler
    public void postinit(FMLPostInitializationEvent event) {
        ModelLoader.setCustomModelResourceLocation(Items.openlightscontroller, 0, new ModelResourceLocation(Items.openlightscontroller.getRegistryName(), "normal"));
        ModelLoader.setCustomModelResourceLocation(Items.openlightscontrollerborder, 0, new ModelResourceLocation(Items.openlightscontrollerborder.getRegistryName(), "normal"));
    }

    /**
     * Forge will automatically look up and bind blocks to the fields in this class
     * based on their registry name.
     */
    @GameRegistry.ObjectHolder(MOD_ID)
    public static class Blocks {
        public static final LightsControllerBlock openlightscontroller = null;
        public static final LightsControllerBorderBlock openlightscontrollerborder = null;
    }

    /**
     * Forge will automatically look up and bind items to the fields in this class
     * based on their registry name.
     */
    @GameRegistry.ObjectHolder(MOD_ID)
    public static class Items {
        public static final ItemBlock openlightscontroller = null;
        public static final ItemBlock openlightscontrollerborder = null;
    }

    /**
     * This is a special class that listens to registry events, to allow creation of mod blocks and items at the proper time.
     */
    @Mod.EventBusSubscriber
    public static class ObjectRegistryHandler {
        /**
         * Listen for the register event for creating custom items
         */
        @SubscribeEvent
        public static void addItems(RegistryEvent.Register<Item> event) {
            event.getRegistry().register(new ItemBlock(Blocks.openlightscontroller).setRegistryName(Blocks.openlightscontroller.getRegistryName().toString()));
            event.getRegistry().register(new ItemBlock(Blocks.openlightscontrollerborder).setRegistryName(Blocks.openlightscontrollerborder.getRegistryName().toString()));
        }

        /**
         * Listen for the register event for creating custom blocks
         */
        @SubscribeEvent
        public static void addBlocks(RegistryEvent.Register<Block> event) {
            event.getRegistry().register(new LightsControllerBlock());
            event.getRegistry().register(new LightsControllerBorderBlock());
        }
    }
}
