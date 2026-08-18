package github.c2f4n.fluidcrafting.registry;

import github.c2f4n.fluidcrafting.c2f4nfluidcrafting;
import github.c2f4n.fluidcrafting.common.recipe.MixingRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Objects;

public final class ModRecipeTypes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, c2f4nfluidcrafting.MODID);
    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, c2f4nfluidcrafting.MODID);

    public static final RegistryObject<RecipeSerializer<MixingRecipe>> MIXING_SERIALIZER =
            SERIALIZERS.register("mixing", MixingRecipe.Serializer::new);
    public static final RegistryObject<RecipeType<MixingRecipe>> MIXING_TYPE =
            TYPES.register("mixing", () -> RecipeType.simple(
                    Objects.requireNonNull(ResourceLocation.tryParse(c2f4nfluidcrafting.MODID + ":mixing"))));

    private ModRecipeTypes() {
    }
}
