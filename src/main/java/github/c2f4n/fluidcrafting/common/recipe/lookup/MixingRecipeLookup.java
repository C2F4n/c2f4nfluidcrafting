package github.c2f4n.fluidcrafting.common.recipe.lookup;

import github.c2f4n.fluidcrafting.common.recipe.MixingRecipe;
import github.c2f4n.fluidcrafting.common.recipe.MixingRecipeInput;
import github.c2f4n.fluidcrafting.registry.ModRecipeTypes;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

/** 从配方管理器中取出混合配方并按输入匹配。与 BE 解耦，便于以后扩展多配方选择策略。 */
public class MixingRecipeLookup {

    private final Level level;
    private final List<MixingRecipe> recipes;

    public MixingRecipeLookup(Level level) {
        this.level = level;
        this.recipes = level.getRecipeManager()
              .getAllRecipesFor(ModRecipeTypes.MIXING_TYPE.get())
              .stream()
              .map(recipe -> (MixingRecipe) recipe)
              .sorted(Comparator.comparingInt(MixingRecipe::getPriority).reversed()
                    .thenComparing(MixingRecipe::getId))
              .toList();
    }

    @Nullable
    public MixingRecipe find(MixingRecipeInput input) {
        for (MixingRecipe recipe : recipes) {
            if (recipe.matches(input, level)) {
                return recipe;
            }
        }
        return null;
    }

    public List<MixingRecipe> getRecipes() {
        return recipes;
    }
}
