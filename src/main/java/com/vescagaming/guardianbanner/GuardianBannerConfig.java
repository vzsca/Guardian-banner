package com.vescagaming.guardianbanner;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

public final class GuardianBannerConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue MIN_BANNER_DISTANCE;
    public static final ForgeConfigSpec.IntValue ATTRACTION_RADIUS;
    public static final ForgeConfigSpec.IntValue ATTRACTION_INTERVAL_TICKS;
    public static final ForgeConfigSpec.IntValue MAX_ATTRACTED_MOBS;
    public static final ForgeConfigSpec.IntValue MOB_TICK_INTERVAL_TICKS;
    public static final ForgeConfigSpec.IntValue CLIENT_BORDER_CHUNK_RADIUS;
    public static final ForgeConfigSpec.IntValue BASE_BARRIER_HP;
    public static final ForgeConfigSpec.IntValue HP_PER_LEVEL;
    public static final ForgeConfigSpec.IntValue KILLS_PER_LEVEL;
    public static final ForgeConfigSpec.IntValue REPAIR_DELAY_SECONDS;
    public static final ForgeConfigSpec.IntValue REGEN_INTERVAL_SECONDS;
    public static final ForgeConfigSpec.IntValue REGEN_AMOUNT;
    public static final ForgeConfigSpec.IntValue CLAIMS_PER_LEVEL;
    public static final ForgeConfigSpec.BooleanValue PROTECT_MOB_DAMAGE;
    public static final ForgeConfigSpec.BooleanValue PROTECT_EXPLOSIONS;
    public static final ForgeConfigSpec.BooleanValue PROTECT_BLOCKS_FROM_EXPLOSIONS;
    public static final ForgeConfigSpec.BooleanValue PREVENT_HOSTILE_SPAWNS;
    public static final ForgeConfigSpec.BooleanValue ATTRACT_HOSTILES;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.push("guardian_banner");
        MIN_BANNER_DISTANCE = b.comment("Minimum horizontal distance between Guardian Banners.")
                .defineInRange("minimum_banner_distance", 100, 1, 4096);
        ATTRACTION_RADIUS = b.comment("Radius in blocks used to attract hostile mobs.")
                .defineInRange("monster_attraction_radius", 30, 1, 128);
        ATTRACTION_INTERVAL_TICKS = b.comment("How often the banner updates hostile mob attraction.")
                .defineInRange("attraction_interval_ticks", 10, 1, 200);
        MAX_ATTRACTED_MOBS = b.comment("Maximum hostile mobs processed by one active banner per attraction update.")
                .defineInRange("max_attracted_mobs", 32, 1, 256);
        MOB_TICK_INTERVAL_TICKS = b.comment("Server tick interval for claim boundary and siege mob checks.")
                .defineInRange("mob_tick_interval_ticks", 5, 1, 20);
        CLIENT_BORDER_CHUNK_RADIUS = b.comment("Chunk radius around each player used for client border synchronization.")
                .defineInRange("client_border_chunk_radius", 8, 1, 32);
        BASE_BARRIER_HP = b.comment("Base barrier hit points.")
                .defineInRange("base_barrier_hp", 100, 1, 100000);
        HP_PER_LEVEL = b.comment("Additional barrier hit points per banner level.")
                .defineInRange("hp_per_level", 100, 0, 100000);
        KILLS_PER_LEVEL = b.comment("Hostile mob kills required for each level.")
                .defineInRange("kills_per_level", 25, 1, 100000);
        REPAIR_DELAY_SECONDS = b.comment("Seconds before a destroyed barrier is fully repaired automatically when quiet.")
                .defineInRange("repair_delay_seconds", 5, 0, 3600);
        REGEN_INTERVAL_SECONDS = b.comment("Seconds between normal barrier regeneration ticks.")
                .defineInRange("regen_interval_seconds", 60, 1, 3600);
        REGEN_AMOUNT = b.comment("Base HP restored during a normal regeneration tick.")
                .defineInRange("regen_amount", 10, 1, 100000);
        CLAIMS_PER_LEVEL = b.comment("Additional chunks granted per banner level.")
                .defineInRange("claims_per_level", 5, 0, 1000);
        PROTECT_MOB_DAMAGE = b.comment("Protect players from hostile mob damage inside an active claim.")
                .define("protect_from_hostile_mobs", true);
        PROTECT_EXPLOSIONS = b.comment("Prevent explosion damage to players inside active claims.")
                .define("protect_from_explosions", true);
        PROTECT_BLOCKS_FROM_EXPLOSIONS = b.comment("Prevent explosions from destroying blocks inside active claims.")
                .define("protect_blocks_from_explosions", true);
        PREVENT_HOSTILE_SPAWNS = b.comment("Prevent natural hostile mob spawns inside active claims.")
                .define("prevent_hostile_spawns", true);
        ATTRACT_HOSTILES = b.comment("Attract hostile mobs toward active Guardian Banners. Disabled barriers do not generate siege pressure.")
                .define("attract_hostiles", true);
        b.pop();
        SPEC = b.build();
    }

    private GuardianBannerConfig() {}
}
