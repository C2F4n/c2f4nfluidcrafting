package github.c2f4n.fluidcrafting.registry;

import github.c2f4n.fluidcrafting.block.entity.BasicFluidMixerBlockEntity;
import github.c2f4n.fluidcrafting.c2f4nfluidcrafting;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, c2f4nfluidcrafting.MODID);

    public static final RegistryObject<BlockEntityType<BasicFluidMixerBlockEntity>> BASIC_FLUID_MIXER =
            BLOCK_ENTITIES.register("basicfluidmixer",
                    () -> BlockEntityType.Builder.of(BasicFluidMixerBlockEntity::new, ModBlocks.BASIC_FLUID_MIXER.get()).build(null));

    private ModBlockEntities() {
    }
}
