package com.uelm.utility.features;

import com.uelm.utility.config.ConfigHandler;
import com.uelm.utility.util.ChatUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * Automatic fishing. While enabled and holding a fishing rod:
 * - casts the rod if the bobber is not out,
 * - detects a bite two ways: the splash sound played at the bobber
 *   (primary, most reliable in 1.8.9) and the bobber suddenly being
 *   yanked downward (fallback),
 * - reels in, waits the configured recast delay, and casts again.
 *
 * Enable Debug Messages in the config GUI to watch every step in chat.
 */
public class AutoFish {

    /** Ticks to ignore after casting so the bobber landing doesn't count as a bite. */
    private static final int SETTLE_TICKS = 15;

    private int recastTimer = 0;
    private int settleTicks = 0;
    private boolean reelQueued = false;

    @SubscribeEvent
    public void onSound(PlaySoundEvent event) {
        if (!ConfigHandler.autoFishEnabled) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.thePlayer;
        if (player == null || player.fishEntity == null || settleTicks > 0) {
            return;
        }
        if (event.name == null || !event.name.contains("random.splash")) {
            return;
        }
        ISound sound = event.sound;
        if (sound == null) {
            return;
        }
        EntityFishHook hook = player.fishEntity;
        double dx = sound.getXPosF() - hook.posX;
        double dy = sound.getYPosF() - hook.posY;
        double dz = sound.getZPosF() - hook.posZ;
        if (dx * dx + dy * dy + dz * dz <= 25.0D) { // within 5 blocks of the bobber
            ChatUtil.debug("Splash at bobber detected - reeling in");
            reelQueued = true;
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.thePlayer;
        if (player == null || mc.theWorld == null || !ConfigHandler.autoFishEnabled) {
            recastTimer = 0;
            settleTicks = 0;
            reelQueued = false;
            return;
        }
        if (mc.currentScreen != null) {
            return; // don't fish while a GUI is open
        }

        ItemStack held = player.getHeldItem();
        if (held == null || held.getItem() != Items.fishing_rod) {
            recastTimer = 0;
            settleTicks = 0;
            reelQueued = false;
            return;
        }

        EntityFishHook hook = player.fishEntity;

        if (hook == null) {
            reelQueued = false;
            settleTicks = 0;
            if (recastTimer > 0) {
                recastTimer--;
                if (recastTimer == 0) {
                    ChatUtil.debug("Casting rod");
                    useRod(mc, player);
                    settleTicks = SETTLE_TICKS;
                }
            } else {
                recastTimer = Math.max(1, ConfigHandler.recastDelayTicks);
                ChatUtil.debug("Rod in hand, casting in " + recastTimer + " ticks");
            }
            return;
        }

        // A bobber is out, so any pending recast is irrelevant.
        recastTimer = 0;

        if (settleTicks > 0) {
            settleTicks--;
            return;
        }

        boolean motionBite = hook.ticksExisted > 20
                && hook.motionY < -0.03D
                && Math.abs(hook.motionX) < 0.05D
                && Math.abs(hook.motionZ) < 0.05D;

        if (reelQueued || motionBite) {
            if (!reelQueued) {
                ChatUtil.debug("Bobber yanked down - reeling in");
            }
            reelQueued = false;
            useRod(mc, player); // reel in
            recastTimer = Math.max(1, ConfigHandler.recastDelayTicks);
            settleTicks = SETTLE_TICKS; // guard against double-triggering
            ChatUtil.debug("Recasting in " + recastTimer + " ticks");
        }
    }

    private void useRod(Minecraft mc, EntityPlayerSP player) {
        ItemStack held = player.getHeldItem();
        if (held != null && held.getItem() == Items.fishing_rod) {
            mc.playerController.sendUseItem(player, mc.theWorld, held);
            player.swingItem();
        }
    }
}
