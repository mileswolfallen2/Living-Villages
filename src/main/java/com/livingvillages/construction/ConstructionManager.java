package com.livingvillages.construction;

import com.livingvillages.LivingVillages;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class ConstructionManager {
    private final List<ConstructionProject> projects = new ArrayList<>();
    private final MinecraftServer server;
    private int tickCounter = 0;

    public ConstructionManager(MinecraftServer server) {
        this.server = server;
    }

    public void addProject(ConstructionProject project) {
        projects.add(project);
        LivingVillages.LOGGER.info("Construction project queued: {} at {} ({} ticks)", project.type, project.site, project.totalWork);
    }

    public void tick() {
        tickCounter++;

        for (ConstructionProject project : List.copyOf(projects)) {
            if (project.state == ConstructionProject.State.COMPLETE) continue;

            ServerLevel level = server.getLevel(project.village.dimension);
            if (level == null) continue;

            if (project.state == ConstructionProject.State.IN_PROGRESS) {
                emitWorkParticles(project, level);
                if (tickCounter % 30 == 0) {
                    emitWorkSound(project, level);
                }
            }

            if (project.workRemaining <= 0 && project.state == ConstructionProject.State.IN_PROGRESS) {
                completeProject(project, level);
            }

            if (project.state == ConstructionProject.State.QUEUED) {
                maybeAssignVillager(project, level);
            }
        }

        if (tickCounter % 100 == 0) {
            for (ServerLevel level : server.getAllLevels()) {
                refreshAllVillagerNames(level);
            }
        }

        if (tickCounter % 200 == 0) {
            projects.removeIf(p -> p.state == ConstructionProject.State.COMPLETE);
        }
    }

    public void tickVillagerWork(Villager villager) {
        ServerLevel level = (ServerLevel) villager.level();
        for (ConstructionProject project : projects) {
            if (project.state != ConstructionProject.State.IN_PROGRESS) continue;
            if (!villager.getUUID().equals(project.assignedVillager)) continue;
            if (!project.village.dimension.equals(level.dimension())) continue;

            BlockPos site = project.site;
            Vec3 siteCenter = Vec3.atCenterOf(site);
            double dist = villager.distanceToSqr(siteCenter);

            if (dist > 64) {
                setNameTag(villager, null);
                project.assignedVillager = null;
                project.state = ConstructionProject.State.QUEUED;
                return;
            }

            if (dist > 9) {
                villager.getNavigation().moveTo(site.getX() + 0.5, site.getY(), site.getZ() + 0.5, 0.5);
                return;
            }

            villager.getLookControl().setLookAt(siteCenter);

            if (tickCounter % 15 == 0) {
                villager.swing(InteractionHand.MAIN_HAND);
            }

            project.workRemaining--;
            return;
        }
    }

    private void maybeAssignVillager(ConstructionProject project, ServerLevel level) {
        Villager worker = findNearbyVillager(project, level);
        if (worker != null) {
            project.assignedVillager = worker.getUUID();
            project.state = ConstructionProject.State.IN_PROGRESS;
            setNameTag(worker, switch (project.type) {
                case BUILDING -> "Builder";
                case ROAD -> "Road Worker";
                case FARM -> "Farmer";
            });
            LivingVillages.LOGGER.info("Assigned villager {} as {} for project at {}",
                worker.getUUID().toString().substring(0, 8),
                worker.getCustomName() != null ? worker.getCustomName().getString() : "?",
                project.site);
        }
    }

    private Villager findNearbyVillager(ConstructionProject project, ServerLevel level) {
        AABB searchArea = new AABB(project.site).inflate(project.village.radius);
        List<Villager> villagers = level.getEntitiesOfClass(Villager.class, searchArea);

        Set<UUID> assigned = new HashSet<>();
        for (ConstructionProject p : projects) {
            if (p.assignedVillager != null && p.state != ConstructionProject.State.COMPLETE) {
                assigned.add(p.assignedVillager);
            }
        }

        Villager nearest = null;
        double nearestDist = Double.MAX_VALUE;
        for (Villager v : villagers) {
            if (!assigned.contains(v.getUUID()) && v.isAlive()) {
                double d = v.distanceToSqr(Vec3.atCenterOf(project.site));
                if (d < nearestDist) {
                    nearestDist = d;
                    nearest = v;
                }
            }
        }
        return nearest;
    }

    private void refreshAllVillagerNames(ServerLevel level) {
        Set<UUID> assignedVillagers = new HashSet<>();
        for (ConstructionProject p : projects) {
            if (p.assignedVillager != null && p.state == ConstructionProject.State.IN_PROGRESS) {
                assignedVillagers.add(p.assignedVillager);
            }
        }

        for (Villager v : level.getEntitiesOfClass(Villager.class, AABB.ofSize(
            new Vec3(0, 64, 0), 60000000, 256, 60000000))) {
            if (assignedVillagers.contains(v.getUUID())) {
                for (ConstructionProject p : projects) {
                    if (v.getUUID().equals(p.assignedVillager) && p.state == ConstructionProject.State.IN_PROGRESS) {
                        setNameTag(v, switch (p.type) {
                            case BUILDING -> "Builder";
                            case ROAD -> "Road Worker";
                            case FARM -> "Farmer";
                        });
                        break;
                    }
                }
            } else {
                if (v.hasCustomName()) {
                    setNameTag(v, null);
                }
            }
        }
    }

    private void setNameTag(Villager villager, String role) {
        if (role != null) {
            villager.setCustomName(Component.literal(role));
            villager.setCustomNameVisible(true);
        } else {
            villager.setCustomName(null);
            villager.setCustomNameVisible(false);
        }
    }

    private void emitWorkParticles(ConstructionProject project, ServerLevel level) {
        double x = project.site.getX() + 0.5;
        double y = project.site.getY() + 1.0;
        double z = project.site.getZ() + 0.5;

        switch (project.type) {
            case BUILDING -> {
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, x, y, z, 1, 0.5, 0.5, 0.5, 0);
                if (tickCounter % 5 == 0) {
                    level.sendParticles(ParticleTypes.CLOUD, x, y + 0.5, z, 1, 0.3, 0.1, 0.3, 0);
                }
            }
            case ROAD -> {
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, x, y + 0.3, z, 1, 0.5, 0.1, 0.5, 0);
                if (tickCounter % 5 == 0) {
                    level.sendParticles(ParticleTypes.CLOUD, x, y + 0.3, z, 1, 0.2, 0.1, 0.2, 0);
                }
            }
            case FARM -> {
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, x, y, z, 1, 0.5, 0.2, 0.5, 0);
                if (tickCounter % 4 == 0) {
                    level.sendParticles(ParticleTypes.COMPOSTER, x, y + 0.3, z, 1, 0.3, 0.1, 0.3, 0);
                }
            }
        }
    }

    private void emitWorkSound(ConstructionProject project, ServerLevel level) {
        switch (project.type) {
            case BUILDING -> level.playSound(null, project.site, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.5f, 1.0f);
            case ROAD -> level.playSound(null, project.site, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 0.5f, 1.0f);
            case FARM -> level.playSound(null, project.site, SoundEvents.GRASS_PLACE, SoundSource.BLOCKS, 0.5f, 1.0f);
        }
    }

    private void completeProject(ConstructionProject project, ServerLevel level) {
        project.state = ConstructionProject.State.COMPLETE;

        Villager worker = (Villager) level.getEntity(project.assignedVillager);
        if (worker != null) {
            setNameTag(worker, null);
        }

        project.onComplete.run();

        double x = project.site.getX() + 0.5;
        double y = project.site.getY() + 0.5;
        double z = project.site.getZ() + 0.5;
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, x, y + 1.0, z, 15, 1.5, 0.5, 1.5, 0.3);
        level.playSound(null, project.site, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.0f);
        LivingVillages.LOGGER.info("Project completed: {} at {}", project.type, project.site);
    }

    public int getActiveProjectCount() {
        return (int) projects.stream().filter(p -> p.state != ConstructionProject.State.COMPLETE).count();
    }

    public List<ConstructionProject> getActiveProjects() {
        return List.copyOf(projects);
    }
}
