package github.c2f4n.fluidcrafting.client.compat.jei;

import github.c2f4n.fluidcrafting.client.gui.BasicFluidMixerScreen;
import github.c2f4n.fluidcrafting.client.gui.FluidRecipeViewer;
import github.c2f4n.fluidcrafting.c2f4nfluidcrafting;
import github.c2f4n.fluidcrafting.registry.ModBlocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IAdvancedRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

import java.util.Objects;

/** JEI 集成入口。只有玩家安装了 JEI 时才会被 JEI 扫描并实例化。 */
@JeiPlugin
@OnlyIn(Dist.CLIENT)
public class C2F4nJeiPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return Objects.requireNonNull(ResourceLocation.tryParse(c2f4nfluidcrafting.MODID + ":jei_plugin"));
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
              new MixingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(ModBlocks.BASIC_FLUID_MIXER.get(), MixingRecipeCategory.TYPE);
    }

    @Override
    public void registerAdvanced(IAdvancedRegistration registration) {
        registration.addTypedRecipeManagerPlugin(
              MixingRecipeCategory.TYPE, new MixingRecipeManagerPlugin());
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(BasicFluidMixerScreen.class,
              112, 58, 56, 8, MixingRecipeCategory.TYPE);
        registration.addGuiContainerHandler(BasicFluidMixerScreen.class,
              new C2F4nJeiGuiHandler(registration.getJeiHelpers().getIngredientManager()));
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        FluidRecipeViewer.setOpener(fluid -> {
            IFocus<FluidStack> focus = jeiRuntime.getJeiHelpers().getFocusFactory()
                  .createFocus(RecipeIngredientRole.OUTPUT, ForgeTypes.FLUID_STACK, fluid);
            jeiRuntime.getRecipesGui().show(focus);
        });
    }

    @Override
    public void onRuntimeUnavailable() {
        FluidRecipeViewer.clearOpener();
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(
              new C2F4nJeiRecipeTransferHandler(registration.getTransferHelper()),
              MixingRecipeCategory.TYPE);
    }
}
