package github.c2f4n.fluidcrafting.common.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import github.c2f4n.fluidcrafting.common.recipe.ingredient.FluidIngredient;
import github.c2f4n.fluidcrafting.registry.ModRecipeTypes;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

/**
 * 流体三合一 + 可选固体的无序集合配方。
 * 流体输入通过 {@link FluidIngredient} 表达，标签到流体的解析在匹配时进行。
 */
public class MixingRecipe implements Recipe<MixingRecipeInput> {

    private final ResourceLocation id;
    private final List<FluidIngredient> fluidInputs;
    private final Ingredient itemInput;
    private final FluidStack resultFluid;
    private final ItemStack resultItem;
    private final int duration;
    private final int priority;

    public MixingRecipe(ResourceLocation id, List<FluidIngredient> fluidInputs, Ingredient itemInput,
                        FluidStack resultFluid, ItemStack resultItem, int duration, int priority) {
        this.id = id;
        this.fluidInputs = List.copyOf(fluidInputs == null ? List.of() : fluidInputs);
        this.itemInput = itemInput == null ? Ingredient.EMPTY : itemInput;
        this.resultFluid = resultFluid == null ? FluidStack.EMPTY : resultFluid.copy();
        this.resultItem = resultItem == null ? ItemStack.EMPTY : resultItem.copy();
        this.duration = Math.max(1, duration);
        this.priority = priority;
        if (this.resultFluid.isEmpty() && this.resultItem.isEmpty()) {
            throw new IllegalArgumentException("Mixing recipe " + id + " must define at least one output");
        }
    }

    public List<FluidIngredient> getFluidInputs() {
        return fluidInputs;
    }

    public Ingredient getItemInput() {
        return itemInput;
    }

    public FluidStack getResultFluid() {
        return resultFluid.copy();
    }

    public ItemStack getResultItem() {
        return resultItem.copy();
    }

    public int getDuration() {
        return duration;
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public boolean matches(MixingRecipeInput input, Level level) {
        List<FluidStack> present = input.getNonEmptyFluids();
        if (present.size() != fluidInputs.size()) {
            return false;
        }
        for (FluidStack stack : present) {
            boolean matched = false;
            for (FluidIngredient ingredient : fluidInputs) {
                if (ingredient.matches(stack)) {
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                return false;
            }
        }
        for (FluidIngredient ingredient : fluidInputs) {
            int total = 0;
            for (FluidStack stack : present) {
                if (ingredient.matches(stack)) {
                    total += stack.getAmount();
                }
            }
            if (total < ingredient.getAmount()) {
                return false;
            }
        }
        ItemStack solid = input.getItem(0);
        if (itemInput.isEmpty()) {
            return solid.isEmpty();
        }
        return !solid.isEmpty() && itemInput.test(solid);
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        if (!itemInput.isEmpty()) {
            ingredients.add(itemInput);
        }
        return ingredients;
    }

    @Override
    public ItemStack assemble(MixingRecipeInput input, RegistryAccess registryAccess) {
        return resultItem.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return resultItem.copy();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.MIXING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.MIXING_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<MixingRecipe> {

        @Override
        public MixingRecipe fromJson(ResourceLocation id, JsonObject json) {
            List<FluidIngredient> fluidInputs = new ArrayList<>();
            if (json.has("fluidInputs")) {
                JsonArray array = json.getAsJsonArray("fluidInputs");
                for (JsonElement element : array) {
                    fluidInputs.add(parseFluidIngredient(element.getAsJsonObject()));
                }
            }
            Ingredient itemInput = json.has("itemInput") && !json.get("itemInput").isJsonNull()
                  ? Ingredient.fromJson(json.get("itemInput")) : Ingredient.EMPTY;
            FluidStack resultFluid = json.has("resultFluid") && !json.get("resultFluid").isJsonNull()
                  ? parseFluidStack(json.getAsJsonObject("resultFluid")) : FluidStack.EMPTY;
            ItemStack resultItem = json.has("resultItem") && !json.get("resultItem").isJsonNull()
                  ? ShapedRecipe.itemStackFromJson(json.getAsJsonObject("resultItem")) : ItemStack.EMPTY;
            int duration = json.has("duration") ? json.get("duration").getAsInt() : 100;
            int priority = json.has("priority") ? json.get("priority").getAsInt() : 0;
            return new MixingRecipe(id, fluidInputs, itemInput, resultFluid, resultItem, duration, priority);
        }

        private static FluidIngredient parseFluidIngredient(JsonObject json) {
            int amount = json.has("amount") ? json.get("amount").getAsInt() : 1000;
            if (json.has("fluid") && json.has("tag")) {
                throw new IllegalArgumentException("Fluid input cannot define both fluid and tag");
            }
            ResourceLocation inputId;
            if (json.has("fluid")) {
                inputId = ResourceLocation.tryParse(json.get("fluid").getAsString());
                Fluid fluid = inputId == null ? null : ForgeRegistries.FLUIDS.getValue(inputId);
                if (fluid == null) {
                    throw new IllegalArgumentException("Unknown fluid input: " + json.get("fluid"));
                }
                return FluidIngredient.of(fluid, amount);
            }
            if (json.has("tag")) {
                inputId = ResourceLocation.tryParse(json.get("tag").getAsString());
                if (inputId == null) {
                    throw new IllegalArgumentException("Invalid fluid tag: " + json.get("tag"));
                }
                return FluidIngredient.of(FluidTags.create(inputId), amount);
            }
            throw new IllegalArgumentException("Fluid input must define fluid or tag");
        }

        private static FluidStack parseFluidStack(JsonObject json) {
            ResourceLocation fluidId = ResourceLocation.tryParse(json.get("fluid").getAsString());
            Fluid fluid = fluidId == null ? null : ForgeRegistries.FLUIDS.getValue(fluidId);
            if (fluid == null) {
                throw new IllegalArgumentException("Unknown result fluid: " + json.get("fluid"));
            }
            int amount = json.has("amount") ? json.get("amount").getAsInt() : 1000;
            return new FluidStack(fluid, amount);
        }

        @Override
        public MixingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            int count = buf.readVarInt();
            List<FluidIngredient> fluidInputs = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                boolean isTag = buf.readBoolean();
                ResourceLocation inputId = buf.readResourceLocation();
                int amount = buf.readVarInt();
                fluidInputs.add(FluidIngredient.fromNetwork(inputId, isTag, amount));
            }
            Ingredient itemInput = Ingredient.fromNetwork(buf);
            FluidStack resultFluid = buf.readFluidStack();
            ItemStack resultItem = buf.readItem();
            int duration = buf.readVarInt();
            int priority = buf.readVarInt();
            return new MixingRecipe(id, fluidInputs, itemInput, resultFluid, resultItem, duration, priority);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, MixingRecipe recipe) {
            buf.writeVarInt(recipe.fluidInputs.size());
            for (FluidIngredient ingredient : recipe.fluidInputs) {
                buf.writeBoolean(ingredient.isTag());
                buf.writeResourceLocation(ingredient.getId());
                buf.writeVarInt(ingredient.getAmount());
            }
            recipe.itemInput.toNetwork(buf);
            buf.writeFluidStack(recipe.resultFluid);
            buf.writeItem(recipe.resultItem);
            buf.writeVarInt(recipe.duration);
            buf.writeVarInt(recipe.priority);
        }
    }
}
