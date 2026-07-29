package com.livingvillages.building;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BuildingPool {
    public record BlockLayer(int y, BlockState[][] pattern) {}

    public record BuildingBlueprint(
        String name,
        int width,
        int depth,
        int height,
        List<BlockLayer> layers,
        BlockState wallMaterial,
        BlockState floorMaterial,
        BlockState roofMaterial,
        boolean hasDoor,
        int doorX,
        int doorZ
    ) {}

    private static final Random RANDOM = new Random();

    public static BuildingBlueprint selectBuilding(Level level, BlockPos pos) {
        List<BuildingBlueprint> candidates = getAll();
        return candidates.get(RANDOM.nextInt(candidates.size()));
    }

    public static List<BuildingBlueprint> getAll() {
        List<BuildingBlueprint> buildings = new ArrayList<>();
        buildings.add(smallHouse());
        buildings.add(mediumHouse());
        buildings.add(largeHouse());
        buildings.add(workshop());
        buildings.add(storehouse());
        return buildings;
    }

    private static BuildingBlueprint smallHouse() {
        int w = 5, d = 5, h = 4;
        BlockState plank = Blocks.OAK_PLANKS.defaultBlockState();
        BlockState log = Blocks.OAK_LOG.defaultBlockState();
        BlockState cobble = Blocks.COBBLESTONE.defaultBlockState();
        BlockState glass = Blocks.GLASS_PANE.defaultBlockState();

        List<BlockLayer> layers = new ArrayList<>();

        BlockState[][] floor = new BlockState[w][d];
        BlockState[][] walls = new BlockState[w][d];
        BlockState[][] roof = new BlockState[w][d];

        for (int x = 0; x < w; x++) {
            for (int z = 0; z < d; z++) {
                floor[x][z] = plank;
                walls[x][z] = Blocks.AIR.defaultBlockState();
                roof[x][z] = Blocks.AIR.defaultBlockState();
            }
        }

        for (int x = 0; x < w; x++) {
            for (int z = 0; z < d; z++) {
                if (x == 0 || x == w-1 || z == 0 || z == d-1) {
                    walls[x][z] = (x % 2 == 0 && z % 2 == 0) ? log : plank;
                }
            }
        }

        walls[2][0] = Blocks.AIR.defaultBlockState();
        walls[2][1] = Blocks.AIR.defaultBlockState();

        walls[1][0] = glass;
        walls[3][0] = glass;

        for (int x = 1; x < w-1; x++) {
            for (int z = 1; z < d-1; z++) {
                walls[x][z] = Blocks.AIR.defaultBlockState();
            }
        }

        for (int x = 0; x < w; x++) {
            for (int z = 0; z < d; z++) {
                if (z == 0 || z == d-1) {
                    roof[x][z] = cobble;
                } else if (x == 0 || x == w-1) {
                    roof[x][z] = cobble;
                } else {
                    roof[x][z] = Blocks.AIR.defaultBlockState();
                }
            }
        }

        layers.add(new BlockLayer(0, floor));
        layers.add(new BlockLayer(1, walls));
        layers.add(new BlockLayer(2, walls));
        layers.add(new BlockLayer(3, roof));

        return new BuildingBlueprint("small_house", w, d, h, layers, plank, plank, cobble, true, 2, 0);
    }

    private static BuildingBlueprint mediumHouse() {
        int w = 7, d = 7, h = 5;
        BlockState plank = Blocks.SPRUCE_PLANKS.defaultBlockState();
        BlockState log = Blocks.SPRUCE_LOG.defaultBlockState();
        BlockState cobble = Blocks.STONE_BRICKS.defaultBlockState();
        BlockState glass = Blocks.GLASS_PANE.defaultBlockState();

        List<BlockLayer> layers = new ArrayList<>();

        BlockState[][] floor = new BlockState[w][d];
        BlockState[][] walls1 = new BlockState[w][d];
        BlockState[][] walls2 = new BlockState[w][d];
        BlockState[][] roof = new BlockState[w][d];

        for (int x = 0; x < w; x++) {
            for (int z = 0; z < d; z++) {
                floor[x][z] = plank;
                walls1[x][z] = Blocks.AIR.defaultBlockState();
                walls2[x][z] = Blocks.AIR.defaultBlockState();
                roof[x][z] = Blocks.AIR.defaultBlockState();
            }
        }

        for (int x = 0; x < w; x++) {
            for (int z = 0; z < d; z++) {
                if (x == 0 || x == w-1 || z == 0 || z == d-1) {
                    walls1[x][z] = (x % 2 == 0 && z % 2 == 0) ? log : plank;
                    walls2[x][z] = (x % 2 == 0 && z % 2 == 0) ? log : plank;
                }
            }
        }

        walls1[3][0] = Blocks.AIR.defaultBlockState();
        walls1[3][1] = Blocks.AIR.defaultBlockState();

        walls1[1][0] = glass;
        walls1[5][0] = glass;
        walls1[3][6] = glass;

        for (int x = 0; x < w; x++) {
            for (int z = 0; z < d; z++) {
                if (x == 0 || x == w-1 || z == 0 || z == d-1) {
                    roof[x][z] = cobble;
                }
            }
        }

        layers.add(new BlockLayer(0, floor));
        layers.add(new BlockLayer(1, walls1));
        layers.add(new BlockLayer(2, walls2));
        layers.add(new BlockLayer(3, roof));

        return new BuildingBlueprint("medium_house", w, d, h, layers, plank, plank, cobble, true, 3, 0);
    }

    private static BuildingBlueprint largeHouse() {
        int w = 9, d = 7, h = 5;
        BlockState plank = Blocks.OAK_PLANKS.defaultBlockState();
        BlockState log = Blocks.OAK_LOG.defaultBlockState();
        BlockState roofMat = Blocks.BRICKS.defaultBlockState();
        BlockState glass = Blocks.GLASS_PANE.defaultBlockState();

        List<BlockLayer> layers = new ArrayList<>();

        BlockState[][] floor = new BlockState[w][d];
        BlockState[][] walls1 = new BlockState[w][d];
        BlockState[][] walls2 = new BlockState[w][d];
        BlockState[][] roof = new BlockState[w][d];

        for (int x = 0; x < w; x++) {
            for (int z = 0; z < d; z++) {
                floor[x][z] = plank;
                walls1[x][z] = Blocks.AIR.defaultBlockState();
                walls2[x][z] = Blocks.AIR.defaultBlockState();
                roof[x][z] = Blocks.AIR.defaultBlockState();
            }
        }

        for (int x = 0; x < w; x++) {
            for (int z = 0; z < d; z++) {
                if (x == 0 || x == w-1 || z == 0 || z == d-1) {
                    walls1[x][z] = log;
                    walls2[x][z] = log;
                }
            }
        }

        walls1[4][0] = Blocks.AIR.defaultBlockState();
        walls1[4][1] = Blocks.AIR.defaultBlockState();

        walls1[2][0] = glass;
        walls1[6][0] = glass;
        walls1[4][6] = glass;

        for (int x = 0; x < w; x++) {
            for (int z = 0; z < d; z++) {
                if (x == 0 || x == w-1 || z == 0 || z == d-1) {
                    roof[x][z] = roofMat;
                }
            }
        }

        layers.add(new BlockLayer(0, floor));
        layers.add(new BlockLayer(1, walls1));
        layers.add(new BlockLayer(2, walls2));
        layers.add(new BlockLayer(3, roof));

        return new BuildingBlueprint("large_house", w, d, h, layers, plank, plank, roofMat, true, 4, 0);
    }

    private static BuildingBlueprint workshop() {
        int w = 7, d = 5, h = 4;
        BlockState plank = Blocks.OAK_PLANKS.defaultBlockState();
        BlockState log = Blocks.OAK_LOG.defaultBlockState();
        BlockState roofMat = Blocks.STONE_BRICKS.defaultBlockState();

        List<BlockLayer> layers = new ArrayList<>();

        BlockState[][] floor = new BlockState[w][d];
        BlockState[][] walls1 = new BlockState[w][d];
        BlockState[][] roof = new BlockState[w][d];

        for (int x = 0; x < w; x++) {
            for (int z = 0; z < d; z++) {
                floor[x][z] = plank;
                walls1[x][z] = Blocks.AIR.defaultBlockState();
                roof[x][z] = Blocks.AIR.defaultBlockState();
            }
        }

        for (int x = 0; x < w; x++) {
            for (int z = 0; z < d; z++) {
                if (x == 0 || x == w-1 || z == 0 || z == d-1) {
                    walls1[x][z] = (x % 2 == 0) ? log : plank;
                }
            }
        }

        walls1[3][0] = Blocks.AIR.defaultBlockState();
        walls1[3][1] = Blocks.AIR.defaultBlockState();

        for (int x = 0; x < w; x++) {
            for (int z = 0; z < d; z++) {
                if (x == 0 || x == w-1 || z == 0 || z == d-1) {
                    roof[x][z] = roofMat;
                }
            }
        }

        layers.add(new BlockLayer(0, floor));
        layers.add(new BlockLayer(1, walls1));
        layers.add(new BlockLayer(2, walls1));
        layers.add(new BlockLayer(3, roof));

        return new BuildingBlueprint("workshop", w, d, h, layers, plank, plank, roofMat, true, 3, 0);
    }

    private static BuildingBlueprint storehouse() {
        int w = 5, d = 7, h = 3;
        BlockState plank = Blocks.SPRUCE_PLANKS.defaultBlockState();
        BlockState log = Blocks.SPRUCE_LOG.defaultBlockState();

        List<BlockLayer> layers = new ArrayList<>();

        BlockState[][] floor = new BlockState[w][d];
        BlockState[][] walls = new BlockState[w][d];
        BlockState[][] roof = new BlockState[w][d];

        for (int x = 0; x < w; x++) {
            for (int z = 0; z < d; z++) {
                floor[x][z] = plank;
                walls[x][z] = Blocks.AIR.defaultBlockState();
                roof[x][z] = Blocks.AIR.defaultBlockState();
            }
        }

        for (int x = 0; x < w; x++) {
            for (int z = 0; z < d; z++) {
                if (x == 0 || x == w-1 || z == 0 || z == d-1) {
                    walls[x][z] = log;
                }
            }
        }

        walls[2][0] = Blocks.AIR.defaultBlockState();
        walls[2][1] = Blocks.AIR.defaultBlockState();

        for (int x = 0; x < w; x++) {
            for (int z = 0; z < d; z++) {
                if (x == 0 || x == w-1 || z == 0 || z == d-1) {
                    roof[x][z] = Blocks.OAK_SLAB.defaultBlockState();
                }
            }
        }

        layers.add(new BlockLayer(0, floor));
        layers.add(new BlockLayer(1, walls));
        layers.add(new BlockLayer(2, roof));

        return new BuildingBlueprint("storehouse", w, d, h, layers, plank, plank, Blocks.OAK_SLAB.defaultBlockState(), true, 2, 0);
    }
}
