package com.livingvillages.util;

import com.livingvillages.LivingVillages;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class SchematicLoader {

    public static boolean placeSchematic(ServerLevel level, BlockPos pos, Identifier schematicId, Rotation rotation) {
        StructureTemplateManager manager = level.getStructureManager();

        Optional<StructureTemplate> template = manager.get(schematicId);
        if (template.isEmpty()) {
            LivingVillages.LOGGER.warn("Schematic not found: {}", schematicId);
            return false;
        }

        StructurePlaceSettings settings = new StructurePlaceSettings()
            .setMirror(Mirror.NONE)
            .setRotation(rotation)
            .setIgnoreEntities(false);

        template.get().placeInWorld(level, pos, pos, settings, level.getRandom(), 3);
        return true;
    }

    public static boolean placeSchematicFromData(ServerLevel level, BlockPos pos, String path) {
        Identifier id = Identifier.tryParse(path);
        if (id != null) {
            return placeSchematic(level, pos, id, Rotation.NONE);
        }
        return false;
    }
}
