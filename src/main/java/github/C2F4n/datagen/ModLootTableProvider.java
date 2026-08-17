package github.C2F4n.datagen;

import github.C2F4n.registry.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;

/** 战利品表生成：方块默认掉落自身（NBT 由方块 getDrops 处理）。 */
public class ModLootTableProvider extends LootTableProvider {

    public ModLootTableProvider(PackOutput output) {
        super(output, Set.of(), List.of(new SubProviderEntry(ModBlockLoot::new, LootContextParamSets.BLOCK)));
    }

    private static class ModBlockLoot extends BlockLootSubProvider {

        protected ModBlockLoot() {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags());
        }

        @Override
        protected void generate() {
            dropSelf(ModBlocks.BASIC_FLUID_MIXER.get());
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return List.of(ModBlocks.BASIC_FLUID_MIXER.get());
        }
    }
}
