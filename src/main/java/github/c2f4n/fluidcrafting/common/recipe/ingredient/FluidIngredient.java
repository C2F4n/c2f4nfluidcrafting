package github.c2f4n.fluidcrafting.common.recipe.ingredient;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * 流体配方输入：一个具体流体或一个流体 Tag，外加需求数量。
 * Tag 不在 JSON 解析时提前展开，而是在实际匹配时查询注册表，避免数据包加载阶段 tag 未就绪。
 */
public final class FluidIngredient {

    @Nullable
    private final Fluid fluid;
    @Nullable
    private final TagKey<Fluid> tag;
    private final int amount;

    private FluidIngredient(@Nullable Fluid fluid, @Nullable TagKey<Fluid> tag, int amount) {
        if ((fluid == null) == (tag == null)) {
            throw new IllegalArgumentException("FluidIngredient must have exactly one of fluid or tag");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("FluidIngredient amount must be positive");
        }
        this.fluid = fluid;
        this.tag = tag;
        this.amount = amount;
    }

    public static FluidIngredient of(Fluid fluid, int amount) {
        return new FluidIngredient(Objects.requireNonNull(fluid), null, amount);
    }

    public static FluidIngredient of(TagKey<Fluid> tag, int amount) {
        return new FluidIngredient(null, Objects.requireNonNull(tag), amount);
    }

    public int getAmount() {
        return amount;
    }

    @Nullable
    public Fluid getFluid() {
        return fluid;
    }

    @Nullable
    public TagKey<Fluid> getTag() {
        return tag;
    }

    public boolean matches(FluidStack stack) {
        return !stack.isEmpty() && matches(stack.getFluid());
    }

    public boolean matches(Fluid candidate) {
        if (fluid != null) {
            return fluid == candidate;
        }
        return candidate.builtInRegistryHolder().is(tag);
    }

    public boolean isTag() {
        return tag != null;
    }

    public ResourceLocation getId() {
        if (fluid != null) {
            return ForgeRegistries.FLUIDS.getKey(fluid);
        }
        return tag.location();
    }

    public static FluidIngredient fromNetwork(ResourceLocation id, boolean isTag, int amount) {
        if (isTag) {
            return of(FluidTags.create(id), amount);
        }
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(id);
        if (fluid == null) {
            throw new IllegalStateException("Unknown fluid in network recipe: " + id);
        }
        return of(fluid, amount);
    }
}
