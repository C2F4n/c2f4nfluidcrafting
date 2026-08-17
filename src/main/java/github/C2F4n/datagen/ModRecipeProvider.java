package github.C2F4n.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import github.C2F4n.c2f4nfluidcrafting;
import github.C2F4n.registry.ModRecipeTypes;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/** 配方生成：输入输出使用明确的记录类型，不再用 Object[][] 拼 JSON。 */
public class ModRecipeProvider extends RecipeProvider {

    public ModRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        consumer.accept(new MixingFinishedRecipe(
              new ResourceLocation(c2f4nfluidcrafting.MODID, "mixing/water_lava"),
              200,
              List.of(
                    new FluidInputData("minecraft:water", null, 1000),
                    new FluidInputData("minecraft:lava", null, 1000)),
              new FluidResultData("minecraft:water", 2000),
              null,
              null));
    }

    private record FluidInputData(@Nullable String fluid, @Nullable String tag, int amount) {
    }

    private record FluidResultData(String fluid, int amount) {
    }

    private record ItemInputData(String item, int count) {
    }

    private record ItemResultData(String item, int count) {
    }

    private static class MixingFinishedRecipe implements FinishedRecipe {

        private final ResourceLocation id;
        private final int duration;
        private final List<FluidInputData> fluidInputs;
        private final FluidResultData resultFluid;
        @Nullable
        private final ItemInputData itemInput;
        @Nullable
        private final ItemResultData resultItem;

        MixingFinishedRecipe(ResourceLocation id, int duration, List<FluidInputData> fluidInputs,
                             FluidResultData resultFluid, @Nullable ItemInputData itemInput,
                             @Nullable ItemResultData resultItem) {
            this.id = id;
            this.duration = duration;
            this.fluidInputs = fluidInputs;
            this.resultFluid = resultFluid;
            this.itemInput = itemInput;
            this.resultItem = resultItem;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            json.addProperty("type", c2f4nfluidcrafting.MODID + ":mixing");
            json.addProperty("duration", duration);
            json.addProperty("priority", 0);
            JsonArray inputs = new JsonArray();
            for (FluidInputData input : fluidInputs) {
                JsonObject entry = new JsonObject();
                if (input.fluid() != null) {
                    entry.addProperty("fluid", input.fluid());
                } else if (input.tag() != null) {
                    entry.addProperty("tag", input.tag());
                } else {
                    throw new IllegalStateException("Fluid input requires fluid or tag");
                }
                entry.addProperty("amount", input.amount());
                inputs.add(entry);
            }
            json.add("fluidInputs", inputs);

            if (itemInput != null) {
                JsonObject item = new JsonObject();
                item.addProperty("item", itemInput.item());
                item.addProperty("count", itemInput.count());
                json.add("itemInput", item);
            }

            JsonObject result = new JsonObject();
            result.addProperty("fluid", resultFluid.fluid());
            result.addProperty("amount", resultFluid.amount());
            json.add("resultFluid", result);

            if (resultItem != null) {
                JsonObject item = new JsonObject();
                item.addProperty("item", resultItem.item());
                item.addProperty("count", resultItem.count());
                json.add("resultItem", item);
            }
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return ModRecipeTypes.MIXING_SERIALIZER.get();
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return null;
        }
    }
}
