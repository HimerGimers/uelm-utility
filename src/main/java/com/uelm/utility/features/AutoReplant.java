package com.uelm.utility.features;

import com.uelm.utility.config.ConfigHandler;
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
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Two things happen here:
 * 1. Left clicks on wheat that is not fully grown (age < 7) are canceled,
 *    so you can't accidentally break immature crops.
 * 2. When fully grown wheat is broken, a replant task is scheduled. After the
 *    configured delay (in ticks or milliseconds) the mod finds wheat seeds in
 *    your hotbar and right clicks the farmland to replant.
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

    @SubscribeEvent
    public void onLeftClickBlock(PlayerInteractEvent event) {
        if (event.action != PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || event.pos == null) {
            return;
        }

        IBlockState state = mc.theWorld.getBlockState(event.pos);
        if (state.getBlock() != Blocks.wheat) {
            return;
        }

        int age = state.getValue(BlockCrops.AGE).intValue();
        if (age < WHEAT_MAX_AGE && ConfigHandler.cancelUngrownBreak) {
            // Not fully grown - cancel the break entirely.
            event.setCanceled(true);
            return;
        }

        // The wheat is about to be broken (crops break instantly) - queue a replant.
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
        if (pending.isEmpty()) {
            return;
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
        // The spot the wheat occupied must now be empty and sitting on farmland.
        if (!mc.theWorld.isAirBlock(cropPos)) {
            return;
        }
        BlockPos soilPos = cropPos.down();
        if (mc.theWorld.getBlockState(soilPos).getBlock() != Blocks.farmland) {
            return;
        }

        EntityPlayerSP player = mc.thePlayer;
        int seedSlot = findSeedsInHotbar(player);
        if (seedSlot == -1) {
            return; // no seeds available
        }

        int previousSlot = player.inventory.currentItem;
        player.inventory.currentItem = seedSlot;
        mc.playerController.updateController(); // sync the held item to the server

        ItemStack seeds = player.inventory.getCurrentItem();
        if (seeds != null && seeds.getItem() == Items.wheat_seeds) {
            Vec3 hitVec = new Vec3(soilPos.getX() + 0.5D, soilPos.getY() + 1.0D, soilPos.getZ() + 0.5D);
            mc.playerController.onPlayerRightClick(player, mc.theWorld, seeds, soilPos, EnumFacing.UP, hitVec);
            player.swingItem();
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
