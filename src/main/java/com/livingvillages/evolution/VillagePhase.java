package com.livingvillages.evolution;

import net.minecraft.util.StringRepresentable;

public enum VillagePhase implements StringRepresentable {
    HAMLET("hamlet", 0, 0, 2),
    VILLAGE("village", 1, 5, 5),
    TOWN("town", 2, 15, 10),
    CITY("city", 3, 30, 20);

    private final String name;
    private final int level;
    private final int minBuildings;
    private final int minPopulation;

    VillagePhase(String name, int level, int minBuildings, int minPopulation) {
        this.name = name;
        this.level = level;
        this.minBuildings = minBuildings;
        this.minPopulation = minPopulation;
    }

    public int getLevel() { return level; }
    public int getMinBuildings() { return minBuildings; }
    public int getMinPopulation() { return minPopulation; }

    public VillagePhase next() {
        VillagePhase[] phases = values();
        int nextIdx = ordinal() + 1;
        return nextIdx < phases.length ? phases[nextIdx] : this;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public static VillagePhase fromName(String name) {
        for (VillagePhase p : values()) {
            if (p.name.equalsIgnoreCase(name)) return p;
        }
        return HAMLET;
    }
}
