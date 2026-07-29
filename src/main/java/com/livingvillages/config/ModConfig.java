package com.livingvillages.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.livingvillages.LivingVillages;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ModConfig INSTANCE;

    public double evolution_speed = 1.0;
    public int max_village_radius = 128;
    public String guardian_strength = "iron";
    public String road_material = "stone";
    public int build_cooldown_ticks = 24000;
    public int farm_expand_interval = 12000;
    public int road_interval = 18000;
    public int patrol_interval = 24000;
    public int population_per_house = 2;
    public int houses_per_phase_upgrade = 5;
    public boolean place_lamps = true;
    public boolean place_fences = true;
    public boolean place_banners = true;
    public int max_buildings_per_village = 50;

    public static ModConfig load() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        Path configFile = configDir.resolve("livingvillages.json");

        if (Files.exists(configFile)) {
            try {
                INSTANCE = GSON.fromJson(Files.readString(configFile), ModConfig.class);
            } catch (IOException e) {
                LivingVillages.LOGGER.error("Failed to load config", e);
                INSTANCE = new ModConfig();
            }
        } else {
            INSTANCE = new ModConfig();
            save();
        }

        if (INSTANCE == null) INSTANCE = new ModConfig();
        return INSTANCE;
    }

    public static void save() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        Path configFile = configDir.resolve("livingvillages.json");
        try {
            Files.createDirectories(configDir);
            Files.writeString(configFile, GSON.toJson(INSTANCE));
        } catch (IOException e) {
            LivingVillages.LOGGER.error("Failed to save config", e);
        }
    }

    public static ModConfig get() {
        if (INSTANCE == null) {
            return load();
        }
        return INSTANCE;
    }

    public static void reload() {
        INSTANCE = null;
        load();
    }
}
