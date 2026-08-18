package github.c2f4n.fluidcrafting.common.recipe;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 机器配方匹配输入：一个固体槽 + 最多三个流体槽。
 * 继续使用 SimpleContainer 是为了兼容 1.20.1 的 Recipe&lt;C extends Container&gt;，
 * 流体不塞进物品容器，而是单独维护为只读列表。
 */
public class MixingRecipeInput extends SimpleContainer {

    private final List<FluidStack> fluids;

    public MixingRecipeInput(ItemStack solid, List<FluidStack> fluids) {
        super(1);
        setItem(0, solid == null ? ItemStack.EMPTY : solid);
        List<FluidStack> normalized = new ArrayList<>(Math.min(3, fluids == null ? 0 : fluids.size()));
        if (fluids != null) {
            for (int i = 0; i < Math.min(3, fluids.size()); i++) {
                FluidStack stack = fluids.get(i);
                normalized.add(stack == null ? FluidStack.EMPTY : stack.copy());
            }
        }
        while (normalized.size() < 3) {
            normalized.add(FluidStack.EMPTY);
        }
        this.fluids = List.copyOf(normalized);
    }

    public List<FluidStack> getFluids() {
        return fluids;
    }

    public List<FluidStack> getNonEmptyFluids() {
        return fluids.stream().filter(stack -> !stack.isEmpty()).toList();
    }
}
