package github.c2f4n.fluidcrafting.registry;

import github.c2f4n.fluidcrafting.c2f4nfluidcrafting;
import github.c2f4n.fluidcrafting.common.inventory.container.BasicFluidMixerContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, c2f4nfluidcrafting.MODID);

    public static final RegistryObject<MenuType<BasicFluidMixerContainer>> BASIC_FLUID_MIXER =
            MENUS.register("basicfluidmixer", () -> IForgeMenuType.create(BasicFluidMixerContainer::new));

    private ModMenuTypes() {
    }
}
