package com.uelm.utility.features;

import com.uelm.utility.config.ConfigHandler;
import com.uelm.utility.util.ChatUtil;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 1. Cancels left clicks on wheat that is not fully grown (age < 7).
 * 2. Schedules a replant after fully grown wheat is broken; after the
 *    configured delay (ticks or ms) it right clicks the farmland with
 *    wheat seeds from the hotbar.
 *
 * Enable Debug Messages in the config GUI to see exactly what this
 * feature is doing in chat.
 */
public class AutoReplant {

    private static final int WHEAT_MAX_AGE = 7;

    private static class ReplantTask {
        private final BlockPos pos;
        private final boolean useMs;
        private final long readyAtMs;
        private int ticksLeft;

        private ReplantTask(BlockPos pos) {
            this.pos = pos;
            this.useMs = ConfigHandler.delayInMs;
            this.readyAtMs = System.currentTimeMillis() + ConfigHandler.replantDelay;
            this.ticksLeft = Math.max(0, ConfigHandler.replantDelay);
        }

        private boolean tickAndCheckReady() {
            if (useMs) {
                return System.currentTimeMillis() >= readyAtMs;
            }
            return --ticksLeft <= 0;
        }
    }

    private final List<ReplantTask> pending = new ArrayList<ReplantTask>();

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onLeftClickBlock(PlayerInteractEvent event) {
        if (event.action != PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || event.pos == null) {
            return;
        }
        // This event also fires on the integrated server thread in singleplayer;
        // only handle the client-side firing.
        if (event.entityPlayer != null && !event.entityPlayer.worldObj.isRemote) {
            return;
        }

        IBlockState state = mc.theWorld.getBlockState(event.pos);
        if (state.getBlock() != Blocks.wheat) {
            return;
        }

        int age = state.getValue(BlockCrops.AGE).intValue();
        ChatUtil.debug("Wheat clicked, age " + age + "/" + WHEAT_MAX_AGE);

        if (age < WHEAT_MAX_AGE) {
            if (ConfigHandler.cancelUngrownBreak) {
                event.setCanceled(true);
                ChatUtil.debug("Break cancelled (wheat not fully grown)");
            }
            return;
        }

        if (ConfigHandler.autoReplantEnabled) {
            schedule(event.pos);
        }
    }

    private void schedule(BlockPos pos) {
        for (ReplantTask task : pending) {
            if (task.pos.equals(pos)) {
                return; // already queued for this position
            }
        }
        pending.add(new ReplantTask(pos));
        ChatUtil.debug("Replant scheduled in " + ConfigHandler.replantDelay
                + (ConfigHandler.delayInMs ? " ms" : " ticks"));
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) {
            pending.clear();
            return;
        }
        if (pending.isEmpty() || mc.currentScreen != null) {
            return; // don't act while a GUI is open
        }

        Iterator<ReplantTask> iterator = pending.iterator();
        while (iterator.hasNext()) {
            ReplantTask task = iterator.next();
            if (!task.tickAndCheckReady()) {
                continue;
            }
            iterator.remove();
            tryReplant(mc, task.pos);
        }
    }

    private void tryReplant(Minecraft mc, BlockPos cropPos) {
        if (!ConfigHandler.autoReplantEnabled) {
            return;
        }
        if (!mc.theWorld.isAirBlock(cropPos)) {
            ChatUtil.debug("Replant skipped: spot is not empty (block: "
                    + mc.theWorld.getBlockState(cropPos).getBlock().getLocalizedName() + ")");
            return;
        }
        BlockPos soilPos = cropPos.down();
        if (mc.theWorld.getBlockState(soilPos).getBlock() != Blocks.farmland) {
            ChatUtil.debug("Replant skipped: no farmland below");
            return;
        }

        EntityPlayerSP player = mc.thePlayer;
        int seedSlot = findSeedsInHotbar(player);
        if (seedSlot == -1) {
            ChatUtil.debug("Replant skipped: no wheat seeds in your HOTBAR (bottom 9 slots)");
            return;
        }

        int previousSlot = player.inventory.currentItem;
        player.inventory.currentItem = seedSlot;
        mc.playerController.updateController(); // sync the held item to the server

        ItemStack seeds = player.inventory.getCurrentItem();
        if (seeds != null && seeds.getItem() == Items.wheat_seeds) {
            Vec3 hitVec = new Vec3(soilPos.getX() + 0.5D, soilPos.getY() + 1.0D, soilPos.getZ() + 0.5D);
            mc.playerController.onPlayerRightClick(player, mc.theWorld, seeds, soilPos, EnumFacing.UP, hitVec);
            player.swingItem();
            ChatUtil.debug("Replanted!");
        }

        player.inventory.currentItem = previousSlot;
        mc.playerController.updateController();
    }

    private int findSeedsInHotbar(EntityPlayerSP player) {
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = player.inventory.mainInventory[slot];
            if (stack != null && stack.getItem() == Items.wheat_seeds) {
                return slot;
            }
        }
        return -1;
    }
}
