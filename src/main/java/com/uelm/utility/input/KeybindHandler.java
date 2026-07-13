package com.uelm.utility.input;

import com.uelm.utility.UelmUtility;
import com.uelm.utility.gui.GuiUelmConfig;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

/**
 * Opens the config GUI when the "Open Uelm Utility Config" key is pressed.
 * The key defaults to Right Shift and can be rebound in
 * Options > Controls under the "Uelm Utility" category.
 */
public class KeybindHandler {

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (UelmUtility.openConfigKey != null && UelmUtility.openConfigKey.isPressed()) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.currentScreen == null) {
                mc.displayGuiScreen(new GuiUelmConfig());
            }
        }
    }
}
