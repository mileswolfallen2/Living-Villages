package com.livingvillages.building;

import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Random;

public class VillageStructurePool {
    private static final Random RANDOM = new Random();

    private static final List<String> SMALL_HOUSES = List.of(
        "plains_small_house_1", "plains_small_house_2", "plains_small_house_3",
        "plains_small_house_4", "plains_small_house_5", "plains_small_house_6",
        "plains_small_house_7", "plains_small_house_8",
        "plains_fisher_cottage_1",
        "plains_shepherds_house_1",
        "plains_stable_1", "plains_stable_2"
    );

    private static final List<String> MEDIUM_HOUSES = List.of(
        "plains_medium_house_1", "plains_medium_house_2",
        "plains_armorer_house_1",
        "plains_butcher_shop_1", "plains_butcher_shop_2",
        "plains_cartographer_1",
        "plains_fletcher_house_1",
        "plains_masons_house_1",
        "plains_tannery_1",
        "plains_tool_smith_1",
        "plains_weaponsmith_1",
        "plains_small_farm_1",
        "plains_accessory_1"
    );

    private static final List<String> LARGE_HOUSES = List.of(
        "plains_big_house_1",
        "plains_large_farm_1",
        "plains_library_1", "plains_library_2",
        "plains_temple_3", "plains_temple_4"
    );

    public record StructureSelection(Identifier id, int workTicks, String size) {}

    public static StructureSelection selectStructure() {
        double roll = RANDOM.nextDouble();
        List<String> pool;
        int workTicks;
        String size;

        if (roll < 0.50) {
            pool = SMALL_HOUSES;
            workTicks = 60 + RANDOM.nextInt(20);
            size = "small";
        } else if (roll < 0.80) {
            pool = MEDIUM_HOUSES;
            workTicks = 120 + RANDOM.nextInt(40);
            size = "medium";
        } else {
            pool = LARGE_HOUSES;
            workTicks = 200 + RANDOM.nextInt(100);
            size = "large";
        }

        String name = pool.get(RANDOM.nextInt(pool.size()));
        Identifier id = Identifier.withDefaultNamespace("village/plains/houses/" + name);
        return new StructureSelection(id, workTicks, size);
    }
}
