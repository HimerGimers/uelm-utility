package com.uelm.utility.util;

import com.uelm.utility.config.ConfigHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;

public class ChatUtil {

    /** Prints a debug line to chat when Debug Messages are enabled in the config. */
    public static void debug(String message) {
        if (!ConfigHandler.debugMessages) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new ChatComponentText("\u00a77[Uelm] \u00a7f" + message));
        }
    }
}
