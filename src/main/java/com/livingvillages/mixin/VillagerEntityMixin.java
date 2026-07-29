package com.livingvillages.mixin;

import com.livingvillages.registry.VillageData;
import com.livingvillages.registry.VillageRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public class VillagerEntityMixin {

    @Inject(method = "registerBrainGoals", at = @At("TAIL"))
    private void onRegisterBrainGoals(CallbackInfo ci) {
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        Villager villager = (Villager) (Object) this;
        Level level = villager.level();

        if (level.isClientSide()) return;

        if (level.getGameTime() % 200 == 0) {
            BlockPos pos = villager.blockPosition();
            VillageRegistry registry = VillageRegistry.getInstance();
            VillageData village = registry.getVillage(pos).orElse(null);

            if (village == null) {
                registry.createVillage(pos, level.dimension());
            }
        }
    }
}
