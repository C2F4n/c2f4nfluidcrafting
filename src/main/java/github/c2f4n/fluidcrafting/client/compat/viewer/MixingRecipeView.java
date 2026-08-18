package github.c2f4n.fluidcrafting.client.compat.viewer;

import github.c2f4n.fluidcrafting.MachineConfig;
import github.c2f4n.fluidcrafting.common.recipe.MixingRecipe;
import github.c2f4n.fluidcrafting.common.recipe.ingredient.FluidIngredient;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * 配方查看器共用的“显示模型”与布局常量。
 * JEI 负责把这里的结构化数据转成自己的 widget，保持显示逻辑集中。
 *
 * 布局：左侧最多 3 个 16x80 输入流体槽，中间一列是物品输入 / 动画箭头 / 物品输出，
 * 右侧是 16x80 输出流体槽。所有坐标都以配方页左上角为原点。
 */
public final class MixingRecipeView {

    public static final int WIDTH = 176;
    public static final int HEIGHT = 100;

    public static final int[] INPUT_TANK_X = {6, 28, 50};
    public static final int INPUT_TANK_COUNT = INPUT_TANK_X.length;
    public static final int OUTPUT_TANK_X = 118;
    public static final int TANK_Y = 6;
    public static final int TANK_WIDTH = 16;
    public static final int TANK_HEIGHT = 80;

    public static final int ITEM_INPUT_X = 83;
    public static final int ITEM_INPUT_Y = 8;
    public static final int ITEM_OUTPUT_X = 83;
    public static final int ITEM_OUTPUT_Y = 60;
    public static final int ARROW_X = 80;
    public static final int ARROW_Y = 37;
    public static final int DURATION_Y = 88;
    public static final int DURATION_CENTER_X = WIDTH / 2;

    private final MixingRecipe recipe;
    private final List<FluidOption> fluidInputs;
    private final Ingredient itemInput;
    private final FluidStack resultFluid;
    private final ItemStack resultItem;

    public MixingRecipeView(MixingRecipe recipe) {
        this.recipe = recipe;
        this.fluidInputs = recipe.getFluidInputs().stream()
              .map(MixingRecipeView::toFluidOption)
              .toList();
        this.itemInput = recipe.getItemInput();
        this.resultFluid = recipe.getResultFluid();
        this.resultItem = recipe.getResultItem();
    }

    public MixingRecipe getRecipe() {
        return recipe;
    }

    public int getDuration() {
        return recipe.getDuration();
    }

    public int fluidInputCount() {
        return fluidInputs.size();
    }

    public FluidOption fluidInput(int index) {
        return fluidInputs.get(index);
    }

    public boolean hasItemInput() {
        return !itemInput.isEmpty();
    }

    public Ingredient itemInput() {
        return itemInput;
    }

    public FluidStack resultFluid() {
        return resultFluid;
    }

    public ItemStack resultItem() {
        return resultItem;
    }

    /** 与真实机器一致的罐容量，配方页用它计算液柱高度。 */
    public static long tankCapacity() {
        return MachineConfig.tankCapacity();
    }

    /** 把 mB 用量换算成 0..1 的液柱比例，超过容量按满罐处理。 */
    public static float fillRatio(long amount) {
        return Math.max(0f, Math.min(1f, amount / (float) tankCapacity()));
    }

    /** 把 tick 换算成用于显示的秒数文本，整秒不带小数，非整秒保留一位。 */
    public static String durationSeconds(int ticks) {
        if (ticks % 20 == 0) {
            return Integer.toString(ticks / 20);
        }
        return String.format(Locale.ROOT, "%.1f", ticks / 20f);
    }

    /** 计算让文字在配方页底部整行水平居中的 x 坐标。 */
    public static int durationX(Component text) {
        return Math.max(0, (WIDTH - Minecraft.getInstance().font.width(text)) / 2);
    }

    private static FluidOption toFluidOption(FluidIngredient ingredient) {
        return new FluidOption(resolve(ingredient), ingredient.getAmount());
    }

    /**
     * 把具体流体或流体 Tag 展开成候选流体列表。
     * 展开失败（例如 Tag 不存在）时返回空列表，由各适配器决定是否隐藏该槽。
     */
    private static List<Fluid> resolve(FluidIngredient ingredient) {
        if (!ingredient.isTag()) {
            Fluid fluid = ingredient.getFluid();
            return fluid == null ? List.of() : List.of(fluid);
        }
        TagKey<Fluid> tag = ingredient.getTag();
        if (tag == null) {
            return List.of();
        }
        return BuiltInRegistries.FLUID.getTag(tag)
              .map(HolderSet.Named::stream)
              .orElseGet(Stream::empty)
              .map(Holder::value)
              .distinct()
              .toList();
    }

    /** 一个流体输入槽：所有可匹配的流体 + 需要的 mB 数。 */
    public record FluidOption(List<Fluid> fluids, long amount) {
    }
}
