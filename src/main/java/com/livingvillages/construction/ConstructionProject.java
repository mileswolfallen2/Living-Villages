package com.livingvillages.construction;

import com.livingvillages.registry.VillageData;
import net.minecraft.core.BlockPos;

import java.util.UUID;

public class ConstructionProject {
    public enum Type { BUILDING, ROAD, FARM }
    public enum State { QUEUED, IN_PROGRESS, COMPLETE }

    public final Type type;
    public final BlockPos site;
    public final VillageData village;
    public final int totalWork;
    public final Runnable onComplete;

    public State state = State.QUEUED;
    public int workRemaining;
    public UUID assignedVillager;

    public ConstructionProject(Type type, BlockPos site, VillageData village, int totalWork, Runnable onComplete) {
        this.type = type;
        this.site = site;
        this.village = village;
        this.totalWork = totalWork;
        this.workRemaining = totalWork;
        this.onComplete = onComplete;
    }
}
