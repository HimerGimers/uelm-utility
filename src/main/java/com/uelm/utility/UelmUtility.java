package com.uelm.utility;

import com.uelm.utility.config.ConfigHandler;
import com.uelm.utility.features.AutoFish;
import com.uelm.utility.features.AutoReplant;
import com.uelm.utility.input.KeybindHandler;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.lwjgl.input.Keyboard;

@Mod(
        modid = UelmUtility.MODID,
        name = UelmUtility.NAME,
        version = UelmUtility.VERSION,
        clientSideOnly = true,
        acceptedMinecraftVersions = "[1.8.9]"
)
public class UelmUtility {

    public static final String MODID = "uelmutility";
    public static final String NAME = "Uelm Utility";
    public static final String VERSION = "1.0.0";

    /** Keybinding that opens the config GUI. Rebindable in Options > Controls under "Uelm Utility". */
    public static KeyBinding openConfigKey;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ConfigHandler.init(event.getSuggestedConfigurationFile());
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        openConfigKey = new KeyBinding("Open Uelm Utility Config", Keyboard.KEY_RSHIFT, "Uelm Utility");
        ClientRegistry.registerKeyBinding(openConfigKey);

        MinecraftForge.EVENT_BUS.register(new AutoReplant());
        MinecraftForge.EVENT_BUS.register(new AutoFish());
        MinecraftForge.EVENT_BUS.register(new KeybindHandler());
    }
}
