package com.livingvillages.guardian;

import com.livingvillages.config.ModConfig;
import com.livingvillages.registry.VillageData;
import com.livingvillages.registry.VillageRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.*;

public class GuardianSystem {
    private static final Random RANDOM = new Random();
    private final Map<BlockPos, List<UUID>> villageGuards = new HashMap<>();
    private int tickCounter = 0;

    public GuardianSystem(MinecraftServer server) {
    }

    public void tick(MinecraftServer server) {
        tickCounter++;
        ModConfig config = ModConfig.get();
        int interval = (int) (config.patrol_interval / config.evolution_speed);
        if (interval < 100) interval = 100;

        if (tickCounter % interval != 0) return;

        for (ServerLevel level : server.getAllLevels()) {
            if (!isNight(level)) {
                despawnGuards(level);
                continue;
            }

            for (VillageData village : VillageRegistry.getInstance().getAllVillages()) {
                if (!village.dimension.equals(level.dimension())) continue;
                if (village.phase.getLevel() < 1) continue;

                int guardCount = countGuards(village, level);
                int desiredGuards = Math.max(1, village.population / 4);

                if (guardCount < desiredGuards) {
                    spawnGuard(village, level);
                }
            }
        }
    }

    private boolean isNight(Level level) {
        long time = level.getOverworldClockTime() % 24000;
        return time >= 13000 && time <= 23000;
    }

    private int countGuards(VillageData village, ServerLevel level) {
        AABB bounds = new AABB(
            village.center.getX() - village.radius, village.center.getY() - 16, village.center.getZ() - village.radius,
            village.center.getX() + village.radius, village.center.getY() + 16, village.center.getZ() + village.radius
        );
        return level.getEntitiesOfClass(IronGolem.class, bounds).size();
    }

    private void spawnGuard(VillageData village, ServerLevel level) {
        BlockPos pos = findSpawnPos(village, level);
        if (pos == null) return;

        IronGolem golem = EntityTypes.IRON_GOLEM.create(level, EntitySpawnReason.EVENT);
        if (golem != null) {
            golem.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            golem.setPlayerCreated(true);
            level.addFreshEntity(golem);

            List<UUID> guards = villageGuards.computeIfAbsent(village.center, k -> new ArrayList<>());
            guards.add(golem.getUUID());
        }
    }

    private BlockPos findSpawnPos(VillageData village, ServerLevel level) {
        for (int attempt = 0; attempt < 10; attempt++) {
            int dx = RANDOM.nextInt(village.radius * 2) - village.radius;
            int dz = RANDOM.nextInt(village.radius * 2) - village.radius;
            BlockPos candidate = village.center.offset(dx, 0, dz);

            int y = level.getHeight();
            while (y > level.getMinY()) {
                BlockPos check = candidate.atY(y);
                if (level.getBlockState(check).isAir() && level.getBlockState(check.below()).blocksMotion()) {
                    return check;
                }
                y--;
            }
        }
        return null;
    }

    private void despawnGuards(ServerLevel level) {
        for (Map.Entry<BlockPos, List<UUID>> entry : villageGuards.entrySet()) {
            Iterator<UUID> it = entry.getValue().iterator();
            while (it.hasNext()) {
                UUID uuid = it.next();
                var entity = level.getEntity(uuid);
                if (entity != null && entity instanceof IronGolem) {
                    entity.remove(net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
                }
                it.remove();
            }
        }
    }

    public int spawnGuardDebug(VillageData village, ServerLevel level) {
        spawnGuard(village, level);
        return 1;
    }
}
