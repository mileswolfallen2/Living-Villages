package com.livingvillages.command;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class LivingVillagesCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            TestCommands.register(dispatcher);
        });
    }
}
