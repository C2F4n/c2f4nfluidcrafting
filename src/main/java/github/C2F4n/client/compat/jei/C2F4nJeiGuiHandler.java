package github.C2F4n.client.compat.jei;

import github.C2F4n.client.gui.BasicFluidMixerScreen;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.client.renderer.Rect2i;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 让 JEI 认识机器 GUI 里自绘的四个流体槽：
 * 鼠标悬停后按 U/R 可以查该流体的配方与用途，点击/拖拽行为也由 JEI 接管。
 * 物品槽是原版 Slot，JEI 本身就能识别，这里不需要处理。
 */
@OnlyIn(Dist.CLIENT)
public class C2F4nJeiGuiHandler implements IGuiContainerHandler<BasicFluidMixerScreen> {

    private final IIngredientManager ingredientManager;

    // 与 BasicFluidMixerScreen 里的侧边按钮布局保持一致，主界面宽 224、按钮宽 26。
    private static final int MAIN_WIDTH = 224;
    private static final int SIDE_TAB_WIDTH = 26;
    private static final int[] SIDE_TAB_Y = {6, 170, 198};

    public C2F4nJeiGuiHandler(IIngredientManager ingredientManager) {
        this.ingredientManager = ingredientManager;
    }

    @Override
    public Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(
          BasicFluidMixerScreen screen, double mouseX, double mouseY) {
        int guiLeft = screen.getGuiLeft();
        int guiTop = screen.getGuiTop();
        for (int i = 0; i < 4; i++) {
            FluidStack fluid = screen.getMenu().getBlockEntity().getTank(i).getFluid();
            if (fluid.isEmpty()) {
                continue;
            }
            Rect2i area = new Rect2i(
                  guiLeft + BasicFluidMixerScreen.tankX(i),
                  guiTop + BasicFluidMixerScreen.tankY(),
                  BasicFluidMixerScreen.tankWidth(),
                  BasicFluidMixerScreen.tankHeight());
            if (area.contains((int) mouseX, (int) mouseY)) {
                Optional<IClickableIngredient<FluidStack>> created = ingredientManager
                      .createClickableIngredient(ForgeTypes.FLUID_STACK, fluid, area, true);
                if (created.isPresent()) {
                    return Optional.ofNullable((IClickableIngredient<?>) created.get());
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Rect2i> getGuiExtraAreas(BasicFluidMixerScreen screen) {
        // 左侧一个、右侧三个按钮都伸出主界面，告诉 JEI 不要把自己的面板盖在上面。
        int guiLeft = screen.getGuiLeft();
        int guiTop = screen.getGuiTop();
        List<Rect2i> areas = new ArrayList<>();
        areas.add(new Rect2i(guiLeft - SIDE_TAB_WIDTH, guiTop + SIDE_TAB_Y[0], SIDE_TAB_WIDTH, SIDE_TAB_WIDTH));
        for (int y : SIDE_TAB_Y) {
            areas.add(new Rect2i(guiLeft + MAIN_WIDTH, guiTop + y, SIDE_TAB_WIDTH, SIDE_TAB_WIDTH));
        }
        return areas;
    }
}
