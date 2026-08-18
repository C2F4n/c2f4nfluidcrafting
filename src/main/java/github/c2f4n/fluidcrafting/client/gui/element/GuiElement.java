package github.c2f4n.fluidcrafting.client.gui.element;

import com.mojang.blaze3d.vertex.PoseStack;
import github.c2f4n.fluidcrafting.client.gui.IGuiHost;
import github.c2f4n.fluidcrafting.client.gui.window.GuiWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * GUI 元素基类。参考 Mekanism 的 GuiElement：
 * 元素持有相对坐标（相对主界面左上角），输入命中用屏幕坐标，
 * 绘制则在 renderLabels 的 (leftPos, topPos) 平移坐标系里使用相对坐标，
 * 这样窗口拖到主界面之外也能正确绘制与命中。
 */
public abstract class GuiElement {

    public static final Minecraft minecraft = Minecraft.getInstance();

    protected final IGuiHost gui;
    protected int relativeX;
    protected int relativeY;
    protected final int width;
    protected final int height;
    protected boolean visible = true;
    protected boolean active = true;
    protected final List<GuiElement> children = new ArrayList<>();

    public GuiElement(IGuiHost gui, int relativeX, int relativeY, int width, int height) {
        this.gui = gui;
        this.relativeX = relativeX;
        this.relativeY = relativeY;
        this.width = width;
        this.height = height;
    }

    /** 元素左上角的屏幕坐标。 */
    public int getX() {
        return gui.getGuiLeft() + relativeX;
    }

    public int getY() {
        return gui.getGuiTop() + relativeY;
    }

    public int getRelativeX() {
        return relativeX;
    }

    public int getRelativeY() {
        return relativeY;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    protected <ELEMENT extends GuiElement> ELEMENT addChild(ELEMENT element) {
        // 窗口子控件传入的是“窗口局部坐标”，必须换算成相对主 GUI 原点的坐标；
        // 后续拖动窗口时由窗口的 move() 统一平移整棵子树，不会再发生错位。
        element.move(relativeX, relativeY);
        children.add(element);
        return element;
    }

    public List<GuiElement> getChildren() {
        return children;
    }

    /** 是否命中（含子元素扩展区域，例如窗口左侧凸出的切换按钮）。 */
    public boolean isMouseOver(double mouseX, double mouseY) {
        return isMouseOverSelf(mouseX, mouseY)
              || children.stream().anyMatch(child -> child.isMouseOver(mouseX, mouseY));
    }

    /** 仅按自身矩形命中，屏幕坐标。 */
    public boolean isMouseOverSelf(double mouseX, double mouseY) {
        return mouseX >= getX() && mouseX < getX() + width && mouseY >= getY() && mouseY < getY() + height;
    }

    /** 元素树中是否包含满足条件的元素。 */
    public boolean containsElement(Predicate<GuiElement> checker) {
        return checker.test(this) || children.stream().anyMatch(child -> child.containsElement(checker));
    }

    /**
     * 鼠标位置是否被“不包含本元素”的窗口挡住。
     * 用于防止窗口后面的主界面元素被点到。
     */
    protected final boolean isBlockedByWindow(double mouseX, double mouseY) {
        GuiWindow window = gui.getWindowHovering(mouseX, mouseY);
        return window != null && !window.containsElement(element -> element == this);
    }

    public final boolean isMouseOverCheckWindows(double mouseX, double mouseY) {
        return isMouseOver(mouseX, mouseY) && !isBlockedByWindow(mouseX, mouseY);
    }

    protected final boolean isHovered(double mouseX, double mouseY) {
        return isMouseOverSelf(mouseX, mouseY) && !isBlockedByWindow(mouseX, mouseY);
    }

    protected final boolean isPressed() {
        double pressX = gui.getPressMouseX();
        double pressY = gui.getPressMouseY();
        return gui.isLeftMouseDown()
              && isMouseOverSelf(pressX, pressY)
              && !isBlockedByWindow(pressX, pressY);
    }

    /**
     * 命中分发：先子后己。子元素命中则返回子元素，自身命中则返回 this，
     * 未命中返回 null。
     */
    @Nullable
    public GuiElement mouseClickedNested(double mouseX, double mouseY, int button) {
        if (!visible) {
            return null;
        }
        for (int i = children.size() - 1; i >= 0; i--) {
            GuiElement result = children.get(i).mouseClickedNested(mouseX, mouseY, button);
            if (result != null) {
                return result;
            }
        }
        if (active && isMouseOverSelf(mouseX, mouseY) && !isBlockedByWindow(mouseX, mouseY)) {
            onClick(mouseX, mouseY, button);
            gui.playClickSound();
            return this;
        }
        return null;
    }

    public void onClick(double mouseX, double mouseY, int button) {
    }

    public void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        children.forEach(child -> child.onDrag(mouseX, mouseY, deltaX, deltaY));
    }

    public void onRelease(double mouseX, double mouseY) {
        children.forEach(child -> child.onRelease(mouseX, mouseY));
    }

    /** 滚轮事件：子元素优先处理。 */
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        for (int i = children.size() - 1; i >= 0; i--) {
            if (children.get(i).mouseScrolled(mouseX, mouseY, delta)) {
                return true;
            }
        }
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (GuiElement child : children) {
            if (child.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return false;
    }

    public void tick() {
        children.forEach(GuiElement::tick);
    }

    /** 相对坐标平移，子元素跟随。 */
    public void move(int changeX, int changeY) {
        relativeX += changeX;
        relativeY += changeY;
        children.forEach(child -> child.move(changeX, changeY));
    }

    public void onWindowClose() {
        children.forEach(GuiElement::onWindowClose);
    }

    /**
     * 背景绘制。使用相对坐标，调用时已处于 (leftPos, topPos) 坐标系。
     */
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
    }

    /**
     * 仅由窗口使用的“背景之上的覆盖层”（窗口贴图本身）。
     * 在主元素的第一遍绘制中不会被调用。
     */
    public void renderBackgroundOverlay(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    }

    /** 前景绘制（文字、悬停/按下覆盖层等）。 */
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    }

    /**
     * 前景 + 子元素完整绘制，zOffset 用于把窗口叠在槽位物品之上。
     */
    public final void onRenderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, int zOffset) {
        if (!visible) {
            return;
        }
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(0, 0, zOffset);
        renderBackgroundOverlay(guiGraphics, mouseX, mouseY);
        for (GuiElement child : children) {
            child.renderBackground(guiGraphics, mouseX, mouseY, gui.getPartialTick());
        }
        renderForeground(guiGraphics, mouseX, mouseY);
        for (GuiElement child : children) {
            pose.pushPose();
            child.onRenderForeground(guiGraphics, mouseX, mouseY, 50);
            pose.popPose();
        }
        pose.popPose();
    }

    /** tooltip 绘制，调用时已回到屏幕坐标。 */
    public void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        for (GuiElement child : children) {
            if (child.isMouseOver(mouseX, mouseY)) {
                child.renderTooltip(guiGraphics, mouseX, mouseY);
            }
        }
    }
}
