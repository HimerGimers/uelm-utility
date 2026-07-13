package com.uelm.utility.gui;

import com.uelm.utility.config.ConfigHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

/**
 * In-game configuration screen. Every feature can be toggled here and the
 * delays can be edited. Settings are saved to config/uelmutility.cfg when
 * the screen is closed.
 */
public class GuiUelmConfig extends GuiScreen {

    private static final int BTN_AUTO_REPLANT = 0;
    private static final int BTN_CANCEL_UNGROWN = 1;
    private static final int BTN_AUTO_FISH = 2;
    private static final int BTN_DELAY_UNIT = 3;
    private static final int BTN_DEBUG = 4;
    private static final int BTN_DONE = 5;

    private GuiTextField replantDelayField;
    private GuiTextField recastDelayField;

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        buttonList.clear();

        int centerX = width / 2;
        int top = height / 4 - 26;

        buttonList.add(new GuiButton(BTN_AUTO_REPLANT, centerX - 100, top, 200, 20,
                "Auto Replant: " + onOff(ConfigHandler.autoReplantEnabled)));
        buttonList.add(new GuiButton(BTN_CANCEL_UNGROWN, centerX - 100, top + 24, 200, 20,
                "Cancel Ungrown Wheat Break: " + onOff(ConfigHandler.cancelUngrownBreak)));
        buttonList.add(new GuiButton(BTN_AUTO_FISH, centerX - 100, top + 48, 200, 20,
                "Auto Fishing: " + onOff(ConfigHandler.autoFishEnabled)));
        buttonList.add(new GuiButton(BTN_DELAY_UNIT, centerX - 100, top + 72, 200, 20,
                "Replant Delay Unit: " + unitName()));
        buttonList.add(new GuiButton(BTN_DEBUG, centerX - 100, top + 96, 200, 20,
                "Debug Messages: " + onOff(ConfigHandler.debugMessages)));

        replantDelayField = new GuiTextField(10, fontRendererObj, centerX - 100, top + 134, 95, 20);
        replantDelayField.setMaxStringLength(7);
        replantDelayField.setText(String.valueOf(ConfigHandler.replantDelay));

        recastDelayField = new GuiTextField(11, fontRendererObj, centerX + 5, top + 134, 95, 20);
        recastDelayField.setMaxStringLength(5);
        recastDelayField.setText(String.valueOf(ConfigHandler.recastDelayTicks));

        buttonList.add(new GuiButton(BTN_DONE, centerX - 100, top + 166, 200, 20, "Done"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case BTN_AUTO_REPLANT:
                ConfigHandler.autoReplantEnabled = !ConfigHandler.autoReplantEnabled;
                button.displayString = "Auto Replant: " + onOff(ConfigHandler.autoReplantEnabled);
                break;
            case BTN_CANCEL_UNGROWN:
                ConfigHandler.cancelUngrownBreak = !ConfigHandler.cancelUngrownBreak;
                button.displayString = "Cancel Ungrown Wheat Break: " + onOff(ConfigHandler.cancelUngrownBreak);
                break;
            case BTN_AUTO_FISH:
                ConfigHandler.autoFishEnabled = !ConfigHandler.autoFishEnabled;
                button.displayString = "Auto Fishing: " + onOff(ConfigHandler.autoFishEnabled);
                break;
            case BTN_DELAY_UNIT:
                ConfigHandler.delayInMs = !ConfigHandler.delayInMs;
                button.displayString = "Replant Delay Unit: " + unitName();
                break;
            case BTN_DEBUG:
                ConfigHandler.debugMessages = !ConfigHandler.debugMessages;
                button.displayString = "Debug Messages: " + onOff(ConfigHandler.debugMessages);
                break;
            case BTN_DONE:
                mc.displayGuiScreen(null);
                break;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        int centerX = width / 2;
        int top = height / 4 - 26;

        drawCenteredString(fontRendererObj, "Uelm Utility", centerX, top - 16, 0xFFFFFF);
        fontRendererObj.drawString("Replant Delay (" + unitName() + ")", centerX - 100, top + 123, 0xA0A0A0);
        fontRendererObj.drawString("Recast Delay (Ticks)", centerX + 5, top + 123, 0xA0A0A0);

        replantDelayField.drawTextBox();
        recastDelayField.drawTextBox();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (replantDelayField.isFocused() || recastDelayField.isFocused()) {
            if (Character.isDigit(typedChar) || typedChar < ' ') {
                replantDelayField.textboxKeyTyped(typedChar, keyCode);
                recastDelayField.textboxKeyTyped(typedChar, keyCode);
            }
            if (keyCode == Keyboard.KEY_ESCAPE) {
                mc.displayGuiScreen(null);
            }
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        replantDelayField.mouseClicked(mouseX, mouseY, mouseButton);
        recastDelayField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void updateScreen() {
        replantDelayField.updateCursorCounter();
        recastDelayField.updateCursorCounter();
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        ConfigHandler.replantDelay = parseField(replantDelayField, ConfigHandler.replantDelay, 0, 1000000);
        ConfigHandler.recastDelayTicks = parseField(recastDelayField, ConfigHandler.recastDelayTicks, 1, 1200);
        ConfigHandler.save();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private int parseField(GuiTextField field, int fallback, int min, int max) {
        try {
            int value = Integer.parseInt(field.getText().trim());
            return Math.max(min, Math.min(max, value));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private String onOff(boolean value) {
        return value ? "\u00a7aON" : "\u00a7cOFF";
    }

    private String unitName() {
        return ConfigHandler.delayInMs ? "Milliseconds" : "Ticks";
    }
}
