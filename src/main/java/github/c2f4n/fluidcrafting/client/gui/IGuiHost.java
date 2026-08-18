package github.c2f4n.fluidcrafting.client.gui;

import github.c2f4n.fluidcrafting.block.entity.BasicFluidMixerBlockEntity;
import github.c2f4n.fluidcrafting.client.gui.window.GuiWindow;
import github.c2f4n.fluidcrafting.client.gui.window.WindowType;
import github.c2f4n.fluidcrafting.common.inventory.container.BasicFluidMixerContainer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * GUI 窗口体系的宿主接口，由 {@link BasicFluidMixerScreen} 实现。
 * 参考 Mekanism 的 IGuiWrapper，只保留本项目需要的部分。
 */
public interface IGuiHost {

    int getGuiLeft();

    int getGuiTop();

    int getGuiWidth();

    int getGuiHeight();

    Font getFont();

    BasicFluidMixerContainer getMenu();

    default BasicFluidMixerBlockEntity getBlockEntity() {
        return getMenu().getBlockEntity();
    }

    @Nullable
    GuiWindow getWindowHovering(double mouseX, double mouseY);

    void addWindow(GuiWindow window);

    void removeWindow(GuiWindow window);

    boolean hasWindow(WindowType type);

    void focusWindow(GuiWindow window);

    default void displayTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY, Component... components) {
        displayTooltip(guiGraphics, mouseX, mouseY, List.of(components));
    }

    default void displayTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY, List<Component> components) {
        guiGraphics.renderComponentTooltip(getFont(), components, mouseX, mouseY);
    }

    default void renderItem(GuiGraphics guiGraphics, ItemStack stack, int x, int y) {
        guiGraphics.renderItem(stack, x, y);
    }

    void playClickSound();

    boolean isLeftMouseDown();

    double getPressMouseX();

    double getPressMouseY();

    float getPartialTick();
}
