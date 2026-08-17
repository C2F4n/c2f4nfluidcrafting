package github.C2F4n.registry;

import github.C2F4n.c2f4nfluidcrafting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, c2f4nfluidcrafting.MODID);

    /** PTFE|功能方块 */
    public static final RegistryObject<CreativeModeTab> FUNCTIONAL_BLOCKS = TABS.register("functional_blocks",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.c2f4nfluidcrafting"))
                    .icon(() -> new ItemStack(ModItems.BASIC_FLUID_MIXER.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.BASIC_FLUID_MIXER.get());
                        output.accept(ModItems.SPEED_UPGRADE_1.get());
                        output.accept(ModItems.SPEED_UPGRADE_2.get());
                        output.accept(ModItems.SPEED_UPGRADE_3.get());
                        output.accept(ModItems.SPEED_UPGRADE_4.get());
                        output.accept(ModItems.SPEED_UPGRADE_5.get());
                        output.accept(ModItems.CREATIVE_UPGRADE.get());
                    })
                    .build());

    private ModCreativeTabs() {
    }
}
