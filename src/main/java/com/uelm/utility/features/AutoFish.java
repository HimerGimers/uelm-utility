package com.uelm.utility.features;

import com.uelm.utility.config.ConfigHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * Automatic fishing. While the feature is enabled and you are holding a
 * fishing rod, the mod will:
 * - cast the rod if the bobber is not in the water,
 * - watch the bobber and reel in the moment a fish bites (the bobber gets
 *   yanked downward while otherwise stationary),
 * - wait the configured recast delay, then cast again.
 */
public class AutoFish {

    /** Ticks to ignore after casting so the bobber landing doesn't count as a bite. */
    private static final int SETTLE_TICKS = 20;

    private int recastTimer = 0;
    private int settleTicks = 0;

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
            return;
        }

        ItemStack held = player.getHeldItem();
        if (held == null || held.getItem() != Items.fishing_rod) {
            recastTimer = 0;
            settleTicks = 0;
            return;
        }

        EntityFishHook hook = player.fishEntity;

        if (hook == null) {
            settleTicks = 0;
            if (recastTimer > 0) {
                recastTimer--;
                if (recastTimer == 0) {
                    useRod(mc, player); // cast
                    settleTicks = SETTLE_TICKS;
                }
            } else {
                // Rod is out but nothing is cast yet - schedule the first cast.
                recastTimer = Math.max(1, ConfigHandler.recastDelayTicks);
            }
            return;
        }

        // A bobber exists, so any pending recast is irrelevant.
        recastTimer = 0;

        if (settleTicks > 0) {
            settleTicks--;
            return;
        }

        if (isBiting(hook)) {
            useRod(mc, player); // reel in
            recastTimer = Math.max(1, ConfigHandler.recastDelayTicks);
            settleTicks = SETTLE_TICKS; // guard against double-triggering before the hook despawns
        }
    }

    /**
     * A bite in 1.8.9 yanks the bobber straight down: strong negative Y motion
     * while horizontal motion is essentially zero.
     */
    private boolean isBiting(EntityFishHook hook) {
        return hook.motionY < -0.05D
                && Math.abs(hook.motionX) < 0.01D
                && Math.abs(hook.motionZ) < 0.01D;
    }

    private void useRod(Minecraft mc, EntityPlayerSP player) {
        ItemStack held = player.getHeldItem();
        if (held != null && held.getItem() == Items.fishing_rod) {
            mc.playerController.sendUseItem(player, mc.theWorld, held);
            player.swingItem();
        }
    }
}
