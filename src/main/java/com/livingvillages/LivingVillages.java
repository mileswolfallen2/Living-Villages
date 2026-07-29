package com.livingvillages;

import com.livingvillages.command.LivingVillagesCommand;
import com.livingvillages.config.ModConfig;
import com.livingvillages.construction.ConstructionManager;
import com.livingvillages.evolution.EvolutionScheduler;
import com.livingvillages.guardian.GuardianSystem;
import com.livingvillages.registry.VillageRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LivingVillages implements ModInitializer {
    public static final String MOD_ID = "livingvillages";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static LivingVillages INSTANCE;

    private EvolutionScheduler evolutionScheduler;
    private GuardianSystem guardianSystem;
    private ConstructionManager constructionManager;

    @Override
    public void onInitialize() {
        INSTANCE = this;
        ModConfig.load();

        LivingVillagesCommand.register();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            VillageRegistry.getInstance().loadAll(server);
            constructionManager = new ConstructionManager(server);
            evolutionScheduler = new EvolutionScheduler(server, constructionManager);
            guardianSystem = new GuardianSystem(server);
            LOGGER.info("Living Villages initialized");
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (constructionManager != null) {
                constructionManager.tick();
            }
            if (evolutionScheduler != null) {
                evolutionScheduler.tick(server);
            }
            if (guardianSystem != null) {
                guardianSystem.tick(server);
            }
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            VillageRegistry.getInstance().saveAll(server);
        });
    }

    public static LivingVillages getInstance() {
        return INSTANCE;
    }

    public ConstructionManager getConstructionManager() {
        return constructionManager;
    }
}
