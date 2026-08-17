package github.C2F4n.registry;

import github.C2F4n.block.BasicFluidMixerBlock;
import github.C2F4n.c2f4nfluidcrafting;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, c2f4nfluidcrafting.MODID);

    public static final RegistryObject<Block> BASIC_FLUID_MIXER = BLOCKS.register("basicfluidmixer", BasicFluidMixerBlock::new);

    private ModBlocks() {
    }
}
