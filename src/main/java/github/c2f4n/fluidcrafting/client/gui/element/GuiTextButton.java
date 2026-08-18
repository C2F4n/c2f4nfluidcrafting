package github.c2f4n.fluidcrafting.client.gui.element;

import com.mojang.blaze3d.systems.RenderSystem;
import github.c2f4n.fluidcrafting.client.gui.GuiRenderUtils;
import github.c2f4n.fluidcrafting.client.gui.GuiTextures;
import github.c2f4n.fluidcrafting.client.gui.IGuiHost;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/** 带文字的 holder 按钮，用于“卸载”等需要文字的控件。 */
public class GuiTextButton extends GuiElement {

    private static final int HOVER_COLOR = 0x2AFFFFFF;
    private static final int PRESSED_COLOR = 0x66000000;

    private final Supplier<Component> text;
    private final Supplier<Component> tooltip;
    private final Runnable action;
    private final BooleanSupplier enabled;

    public GuiTextButton(IGuiHost gui, int relativeX, int relativeY, int width, int height,
                         Supplier<Component> text, Supplier<Component> tooltip,
                         Runnable action, BooleanSupplier enabled) {
        super(gui, relativeX, relativeY, width, height);
        this.text = text;
        this.tooltip = tooltip;
        this.action = action;
        this.enabled = enabled;
        updateEnabled();
    }

    private void updateEnabled() {
        active = enabled.getAsBoolean();
    }

    @Override
    public void tick() {
        super.tick();
        updateEnabled();
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        float shade = active ? 1f : 0.5f;
        RenderSystem.setShaderColor(shade, shade, shade, 1f);
        GuiRenderUtils.drawNineSliced(guiGraphics, GuiTextures.ELEMENT_HOLDER,
              relativeX, relativeY, width, height, 1, 256, 256);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (isHovered(mouseX, mouseY)) {
            guiGraphics.fill(relativeX, relativeY, relativeX + width, relativeY + height, HOVER_COLOR);
        }
        if (isPressed()) {
            guiGraphics.fill(relativeX + 1, relativeY + 1, relativeX + width, relativeY + height, PRESSED_COLOR);
        }
        Component label = text.get();
        int textWidth = gui.getFont().width(label);
        int x = relativeX + Math.max(0, (width - textWidth) / 2);
        int y = relativeY + Math.max(0, (height - gui.getFont().lineHeight) / 2);
        guiGraphics.drawString(gui.getFont(), label, x, y, active ? 0x404040 : 0x777777, false);
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        if (active) {
            action.run();
        }
    }

    @Override
    public void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (isHovered(mouseX, mouseY)) {
            gui.displayTooltip(guiGraphics, mouseX, mouseY, tooltip.get());
        }
    }
}
