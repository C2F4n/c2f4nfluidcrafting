package github.C2F4n.datagen;

import github.C2F4n.block.BasicFluidMixerBlock;
import github.C2F4n.c2f4nfluidcrafting;
import github.C2F4n.registry.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.Objects;

/** 方块状态与方块物品模型：沿用 Blockbench 静态模型，只生成朝向变体。 */
public class ModBlockStateProvider extends BlockStateProvider {

    public ModBlockStateProvider(PackOutput output, String modid, ExistingFileHelper existing) {
        super(output, modid, existing);
    }

    @Override
    protected void registerStatesAndModels() {
        var model = models().getExistingFile(Objects.requireNonNull(
              ResourceLocation.tryParse(c2f4nfluidcrafting.MODID + ":block/basicfluidmixer")));
        getVariantBuilder(ModBlocks.BASIC_FLUID_MIXER.get()).forAllStates(state -> {
            Direction facing = state.getValue(BasicFluidMixerBlock.FACING);
            int rotation = switch (facing) {
                case EAST -> 90;
                case SOUTH -> 180;
                case WEST -> 270;
                default -> 0;
            };
            return ConfiguredModel.builder().modelFile(model).rotationY(rotation).build();
        });

        ItemModelBuilder item = itemModels().withExistingParent("basicfluidmixer",
              Objects.requireNonNull(ResourceLocation.tryParse(c2f4nfluidcrafting.MODID + ":block/basicfluidmixer")));
        item.transforms()
              .transform(ItemDisplayContext.GUI).rotation(30, 225, 0).translation(0, 0, 0).scale(0.625f).end()
              .transform(ItemDisplayContext.GROUND).rotation(0, 0, 0).translation(0, 3, 0).scale(0.25f).end()
              .transform(ItemDisplayContext.FIXED).rotation(0, 0, 0).translation(0, 0, 0).scale(0.5f).end()
              .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND).rotation(75, 45, 0).translation(0, 2.5f, 0).scale(0.375f).end()
              .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND).rotation(0, 45, 0).translation(0, 0, 0).scale(0.4f).end()
              .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND).rotation(0, 225, 0).translation(0, 0, 0).scale(0.4f).end();
    }
}
