package github.C2F4n.common.recipe.lookup;

import github.C2F4n.common.recipe.MixingRecipe;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * 输入内容版本的配方缓存：内容没变就不重新查配方。
 * 本 mod 是“流体三合一 + 固体”的无序集合匹配，查找逻辑仍由 BE 提供，
 * 这里只做失效缓存与错误状态，避免每 tick 全表扫描。
 */
public class RecipeCache {

    private final IntSupplier contentVersion;
    private final Supplier<MixingRecipe> finder;
    @Nullable
    private MixingRecipe recipe;
    private RecipeError error = RecipeError.NO_MATCHING_RECIPE;
    private int lastVersion = -1;

    public RecipeCache(IntSupplier contentVersion, Supplier<MixingRecipe> finder) {
        this.contentVersion = contentVersion;
        this.finder = finder;
    }

    public void refresh() {
        int version = contentVersion.getAsInt();
        if (version == lastVersion) {
            return;
        }
        lastVersion = version;
        recipe = finder.get();
        error = recipe == null ? RecipeError.NO_MATCHING_RECIPE : null;
    }

    @Nullable
    public MixingRecipe getRecipe() {
        return recipe;
    }

    @Nullable
    public RecipeError getError() {
        return error;
    }

    public void setError(RecipeError error) {
        this.error = error;
    }

    /** 产出完成后丢弃当前缓存，下一 tick 依据最新内容重新匹配。 */
    public void invalidate() {
        recipe = null;
        error = null;
        lastVersion = -1;
    }
}
