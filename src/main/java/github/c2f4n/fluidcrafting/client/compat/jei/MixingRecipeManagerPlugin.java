package github.c2f4n.fluidcrafting.client.compat.jei;

import github.c2f4n.fluidcrafting.common.recipe.MixingRecipe;
import github.c2f4n.fluidcrafting.registry.ModRecipeTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.advanced.ISimpleRecipeManagerPlugin;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 把数据包里的 {@link MixingRecipe} 接入 JEI。
 * 这样右键某个流体/物品时能反查出本 mod 的混合配方，而不是只能浏览配方列表。
 */
@OnlyIn(Dist.CLIENT)
public class MixingRecipeManagerPlugin implements ISimpleRecipeManagerPlugin<MixingRecipe> {

    @Override
    public boolean isHandledInput(ITypedIngredient<?> ingredient) {
        return matchingInputs(ingredient).findAny().isPresent();
    }

    @Override
    public boolean isHandledOutput(ITypedIngredient<?> ingredient) {
        return matchingOutputs(ingredient).findAny().isPresent();
    }

    @Override
    public List<MixingRecipe> getRecipesForInput(ITypedIngredient<?> ingredient) {
        return matchingInputs(ingredient).toList();
    }

    @Override
    public List<MixingRecipe> getRecipesForOutput(ITypedIngredient<?> ingredient) {
        return matchingOutputs(ingredient).toList();
    }

    @Override
    public List<MixingRecipe> getAllRecipes() {
        return allRecipes().toList();
    }

    private Stream<MixingRecipe> matchingInputs(ITypedIngredient<?> ingredient) {
        Optional<FluidStack> fluid = ingredient.getIngredient(ForgeTypes.FLUID_STACK);
        Optional<ItemStack> item = ingredient.getItemStack();
        if (fluid.isEmpty() && item.isEmpty()) {
            return Stream.empty();
        }
        return allRecipes().filter(recipe ->
              fluid.filter(stack -> !stack.isEmpty())
                    .map(stack -> recipe.getFluidInputs().stream().anyMatch(input -> input.matches(stack)))
                    .orElse(false)
                    || item.filter(stack -> !stack.isEmpty())
                    .map(stack -> !recipe.getItemInput().isEmpty() && recipe.getItemInput().test(stack))
                    .orElse(false));
    }

    private Stream<MixingRecipe> matchingOutputs(ITypedIngredient<?> ingredient) {
        Optional<FluidStack> fluid = ingredient.getIngredient(ForgeTypes.FLUID_STACK);
        Optional<ItemStack> item = ingredient.getItemStack();
        if (fluid.isEmpty() && item.isEmpty()) {
            return Stream.empty();
        }
        return allRecipes().filter(recipe ->
              fluid.filter(stack -> !stack.isEmpty())
                    .map(stack -> !recipe.getResultFluid().isEmpty()
                          && recipe.getResultFluid().isFluidEqual(stack))
                    .orElse(false)
                    || item.filter(stack -> !stack.isEmpty())
                    .map(stack -> !recipe.getResultItem().isEmpty()
                          && ItemStack.isSameItemSameTags(recipe.getResultItem(), stack))
                    .orElse(false));
    }

    private Stream<MixingRecipe> allRecipes() {
        if (Minecraft.getInstance().level == null) {
            return Stream.empty();
        }
        return Minecraft.getInstance().level.getRecipeManager()
              .getAllRecipesFor(ModRecipeTypes.MIXING_TYPE.get())
              .stream()
              .map(this::cast);
    }

    private MixingRecipe cast(Recipe<?> recipe) {
        return (MixingRecipe) recipe;
    }
}
