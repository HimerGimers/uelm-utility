package com.uelm.utility.config;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

/**
 * Handles loading and saving all Uelm Utility settings to
 * config/uelmutility.cfg using Forge's built-in config system.
 */
public class ConfigHandler {

    public static final String CATEGORY_FEATURES = "features";
    public static final String CATEGORY_DELAYS = "delays";

    private static Configuration config;

    // Feature toggles
    public static boolean autoReplantEnabled = true;
    public static boolean cancelUngrownBreak = true;
    public static boolean autoFishEnabled = false;
    public static boolean debugMessages = false;

    // Delays
    /** If true, {@link #replantDelay} is interpreted as milliseconds; otherwise as game ticks. */
    public static boolean delayInMs = false;
    public static int replantDelay = 5;
    public static int recastDelayTicks = 20;

    public static void init(File file) {
        config = new Configuration(file);
        load();
    }

    public static void load() {
        config.load();

        autoReplantEnabled = config.getBoolean("autoReplant", CATEGORY_FEATURES, true,
                "Automatically replants wheat after you break it");
        cancelUngrownBreak = config.getBoolean("cancelUngrownBreak", CATEGORY_FEATURES, true,
                "Stops you from breaking wheat that is not fully grown");
        autoFishEnabled = config.getBoolean("autoFish", CATEGORY_FEATURES, false,
                "Automatically reels in and recasts your fishing rod while you hold it");
        debugMessages = config.getBoolean("debugMessages", CATEGORY_FEATURES, false,
                "Prints chat messages explaining what the mod is doing (for troubleshooting)");

        delayInMs = config.getBoolean("replantDelayInMilliseconds", CATEGORY_DELAYS, false,
                "If true the replant delay is measured in milliseconds, otherwise in game ticks (20 ticks = 1 second)");
        replantDelay = config.getInt("replantDelay", CATEGORY_DELAYS, 5, 0, 1000000,
                "How long to wait before replanting broken wheat (unit depends on replantDelayInMilliseconds)");
        recastDelayTicks = config.getInt("fishRecastDelayTicks", CATEGORY_DELAYS, 20, 1, 1200,
                "Delay in ticks before the fishing rod is recast after reeling in");

        if (config.hasChanged()) {
            config.save();
        }
    }

    public static void save() {
        config.get(CATEGORY_FEATURES, "autoReplant", true).set(autoReplantEnabled);
        config.get(CATEGORY_FEATURES, "cancelUngrownBreak", true).set(cancelUngrownBreak);
        config.get(CATEGORY_FEATURES, "autoFish", false).set(autoFishEnabled);
        config.get(CATEGORY_FEATURES, "debugMessages", false).set(debugMessages);
        config.get(CATEGORY_DELAYS, "replantDelayInMilliseconds", false).set(delayInMs);
        config.get(CATEGORY_DELAYS, "replantDelay", 5).set(replantDelay);
        config.get(CATEGORY_DELAYS, "fishRecastDelayTicks", 20).set(recastDelayTicks);
        config.save();
    }
}
