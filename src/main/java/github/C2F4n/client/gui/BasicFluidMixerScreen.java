package github.C2F4n.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import github.C2F4n.block.entity.MiningMode;
import github.C2F4n.client.gui.element.GuiButton;
import github.C2F4n.client.gui.element.GuiElement;
import github.C2F4n.client.gui.element.GuiSideTab;
import github.C2F4n.client.gui.element.GuiTankGauge;
import github.C2F4n.client.gui.element.GuiUpgradeSlot;
import github.C2F4n.client.gui.window.GuiWindow;
import github.C2F4n.client.gui.window.IoConfigWindow;
import github.C2F4n.client.gui.window.UpgradeWindow;
import github.C2F4n.client.gui.window.WindowType;
import github.C2F4n.common.inventory.container.BasicFluidMixerContainer;
import github.C2F4n.common.inventory.container.slot.SelectedWindowData;
import github.C2F4n.common.network.PacketHandler;
import github.C2F4n.common.network.to_server.PacketGuiInteract;
import github.C2F4n.common.recipe.lookup.RecipeError;
import github.C2F4n.common.tile.component.TileComponentRedstone;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 基础流体混合器 GUI。
 * <p>
 * 整体执行框架参考 Mekanism 的 GuiMekanism + GuiWindow：
 * 主界面元素与小窗口统一为 GuiElement 树；小窗口放在窗口栈里，
 * 可同时打开多个、点谁谁获得焦点；窗口在 renderLabels 阶段用 z 偏移绘制，
 * 因此永远盖住槽位物品，而原版拖拽物品晚于窗口绘制、浮在最上层；
 * 命中测试从窗口到槽位逐层分发，窗口挡住的地方不会穿透到背包。
 */
public class BasicFluidMixerScreen extends AbstractContainerScreen<BasicFluidMixerContainer> implements IGuiHost {

    private static final int MAIN_SIZE = 224;
    private static final int TANK_Y = 16;
    private static final int TANK_W = 16;
    private static final int TANK_H = 80;
    private static final int[] TANK_XS = {16, 48, 80, 176};

    private static final int PROGRESS_X = 112;
    private static final int PROGRESS_Y = 58;
    private static final int PROGRESS_WIDTH = 56;
    private static final int PROGRESS_HEIGHT = 8;
    private static final int PROGRESS_BORDER = 0xFF2B2B2B;
    private static final int PROGRESS_TRACK = 0xFF555555;
    private static final int PROGRESS_FILL = 0xFF8FC7FF;

    public static int tankX(int index) {
        return TANK_XS[index];
    }

    public static int tankY() {
        return TANK_Y;
    }

    public static int tankWidth() {
        return TANK_W;
    }

    public static int tankHeight() {
        return TANK_H;
    }

    /** 主界面元素（入口按钮、流体罐）。 */
    private final List<GuiElement> elements = new ArrayList<>();
    /** 小窗口栈：索引 0 在最底，末尾在最顶/焦点。 */
    private final List<GuiWindow> windowStack = new ArrayList<>();
    private final Map<WindowType, GuiWindow> windowsByType = new EnumMap<>(WindowType.class);

    @Nullable
    private GuiElement focusedElement;
    private boolean leftMouseDown;
    private double pressMouseX;
    private double pressMouseY;
    private float partialTick;

    public BasicFluidMixerScreen(BasicFluidMixerContainer menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = MAIN_SIZE;
        imageHeight = MAIN_SIZE;
        // 主贴图的背包从 (32,144) 开始，把原版“物品栏”标签挪到背包左上
        inventoryLabelX = 32;
        inventoryLabelY = 132;
    }

    @Override
    protected void init() {
        super.init();
        elements.clear();
        // 流体罐
        for (int i = 0; i < TANK_XS.length; i++) {
            elements.add(new GuiTankGauge(this, TANK_XS[i], TANK_Y, TANK_W, TANK_H, i));
        }
        // 左侧：输入输出配置（开窗）
        elements.add(new GuiSideTab(this, -26, 6, 26, true, GuiTextures.BTN_IO_OPEN,
              () -> Component.translatable("gui.c2f4nfluidcrafting.io_config"),
              () -> toggleWindow(WindowType.IO_CONFIG),
              () -> hasWindow(WindowType.IO_CONFIG)));
        // 右侧顶部：升级（开窗）
        elements.add(new GuiSideTab(this, MAIN_SIZE, 6, 26, false, GuiTextures.BTN_UPGRADE_OPEN,
              () -> Component.translatable("gui.c2f4nfluidcrafting.upgrade"),
              () -> toggleWindow(WindowType.UPGRADE),
              () -> hasWindow(WindowType.UPGRADE)));
        // 右侧底部：红石开关、挖掘开关
        elements.add(new GuiSideTab(this, MAIN_SIZE, 170, 26, false, GuiTextures.BTN_REDSTONE_OPEN,
              this::redstoneModeText, this::toggleRedstoneMode, this::isRedstoneOn));
        elements.add(new GuiSideTab(this, MAIN_SIZE, 198, 26, false, GuiTextures.BTN_MINING_OPEN,
              this::miningModeText, this::toggleMiningMode, this::isMiningDrop));
        // 窗口位置相对主界面存储，主界面重排（如窗口缩放）后自动跟随；
        // 若屏幕变小导致出界，再收回来。
        windowStack.forEach(GuiWindow::clampToScreen);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        elements.forEach(GuiElement::tick);
        windowStack.forEach(GuiWindow::tick);
    }

    private net.minecraft.resources.ResourceLocation frameForSlot(int index) {
        if (index == 0) {
            return GuiTextures.SLOT_INPUT;
        } else if (index == 1) {
            return GuiTextures.SLOT_OUTPUT;
        }
        return GuiTextures.SLOT_NORMAL;
    }

    private net.minecraft.resources.ResourceLocation overlayForSlot(int index, int machineSlots) {
        if (index == 0) {
            return GuiTextures.OVERLAY_INPUT;
        } else if (index == 1) {
            return GuiTextures.OVERLAY_OUTPUT;
        } else if (index >= 2 && index < machineSlots) {
            return GuiTextures.OVERLAY_CONTAINER;
        }
        return null;
    }

    private boolean isRedstoneOn() {
        return menu.getBlockEntity().getRedstoneComponent().getMode() == TileComponentRedstone.RedstoneMode.ON_SIGNAL;
    }

    private void toggleRedstoneMode() {
        PacketHandler.sendToServer(new PacketGuiInteract(PacketGuiInteract.GuiInteraction.REDSTONE_MODE,
              menu.getBlockEntity().getBlockPos(),
              isRedstoneOn() ? TileComponentRedstone.RedstoneMode.DISABLED.ordinal()
                    : TileComponentRedstone.RedstoneMode.ON_SIGNAL.ordinal()));
    }

    private Component redstoneModeText() {
        return Component.translatable("gui.c2f4nfluidcrafting.redstone.current",
              Component.translatable(isRedstoneOn()
                    ? "gui.c2f4nfluidcrafting.mode.on_signal"
                    : "gui.c2f4nfluidcrafting.mode.disabled"));
    }

    private boolean isMiningDrop() {
        return menu.getBlockEntity().getMiningMode() == MiningMode.DROP_ITEMS;
    }

    private void toggleMiningMode() {
        PacketHandler.sendToServer(new PacketGuiInteract(PacketGuiInteract.GuiInteraction.MINING_MODE,
              menu.getBlockEntity().getBlockPos(),
              isMiningDrop() ? MiningMode.KEEP_NBT.ordinal() : MiningMode.DROP_ITEMS.ordinal()));
    }

    private Component miningModeText() {
        return Component.translatable("gui.c2f4nfluidcrafting.mining.current",
              Component.translatable(isMiningDrop()
                    ? "gui.c2f4nfluidcrafting.mining.drop_items"
                    : "gui.c2f4nfluidcrafting.mining.keep_nbt"));
    }

    // ---------- 窗口栈 ----------

    @Override
    public void addWindow(GuiWindow window) {
        GuiWindow existing = windowsByType.remove(window.getWindowType());
        if (existing != null) {
            windowStack.remove(existing);
        }
        windowsByType.put(window.getWindowType(), window);
        windowStack.add(window);
        menu.setSelectedWindow(selectedWindowData(window));
    }

    @Override
    public void removeWindow(GuiWindow window) {
        if (windowsByType.get(window.getWindowType()) == window) {
            windowsByType.remove(window.getWindowType());
        }
        windowStack.remove(window);
        if (focusedElement == window) {
            focusedElement = null;
        }
        menu.setSelectedWindow(windowStack.isEmpty() ? null : selectedWindowData(windowStack.get(windowStack.size() - 1)));
    }

    @Override
    public boolean hasWindow(WindowType type) {
        return windowsByType.containsKey(type);
    }

    @Override
    public void focusWindow(GuiWindow window) {
        if (windowStack.remove(window)) {
            windowStack.add(window);
        }
        menu.setSelectedWindow(selectedWindowData(window));
    }

    private SelectedWindowData selectedWindowData(GuiWindow window) {
        return new SelectedWindowData(window.getWindowType() == WindowType.UPGRADE
              ? SelectedWindowData.WindowType.UPGRADE
              : SelectedWindowData.WindowType.IO_CONFIG);
    }

    @Override
    @Nullable
    public GuiWindow getWindowHovering(double mouseX, double mouseY) {
        for (int i = windowStack.size() - 1; i >= 0; i--) {
            GuiWindow window = windowStack.get(i);
            if (window.isMouseOver(mouseX, mouseY)) {
                return window;
            }
        }
        return null;
    }

    private void toggleWindow(WindowType type) {
        GuiWindow existing = windowsByType.get(type);
        if (existing != null) {
            existing.close();
        } else {
            addWindow(createWindow(type));
        }
    }

    private GuiWindow createWindow(WindowType type) {
        return switch (type) {
            case IO_CONFIG -> new IoConfigWindow(this);
            case UPGRADE -> new UpgradeWindow(this);
        };
    }

    // ---------- 渲染 ----------

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        this.partialTick = partialTick;
        // 主背景九宫格
        drawMainBackground(guiGraphics);
        // 槽位底板画在物品之前（MEK 同款顺序），避免不透明槽框盖住物品图标
        int machineSlots = BasicFluidMixerContainer.MACHINE_SLOTS;
        for (int i = 0; i < menu.slots.size(); i++) {
            if (i >= BasicFluidMixerContainer.UPGRADE_SLOT_START && i < BasicFluidMixerContainer.UPGRADE_SLOT_END) {
                continue;
            }
            Slot slot = menu.slots.get(i);
            int x = leftPos + slot.x - 1;
            int y = topPos + slot.y - 1;
            guiGraphics.blit(frameForSlot(i), x, y, 0, 0, 18, 18, 18, 18);
            net.minecraft.resources.ResourceLocation overlay = overlayForSlot(i, machineSlots);
            if (overlay != null) {
                guiGraphics.blit(overlay, x, y, 0, 0, 18, 18, 18, 18);
            }
        }
        drawProgressBar(guiGraphics);
    }

    private void drawMainBackground(GuiGraphics guiGraphics) {
        GuiRenderUtils.drawNineSliced(guiGraphics, GuiTextures.BASE,
              leftPos, topPos, MAIN_SIZE, MAIN_SIZE, 4, 256, 256);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        PoseStack pose = guiGraphics.pose();
        // 与 Mekanism 相同的 z 基础偏移，给窗口栈留出堆叠空间
        pose.translate(0, 0, 300);

        // 主界面元素背景（按钮贴图、流体液面），在槽位物品之上
        for (GuiElement element : elements) {
            element.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        }

        guiGraphics.drawString(font, title, titleLabelX, titleLabelY, 0x404040, false);
        guiGraphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
        RecipeError recipeError = menu.getBlockEntity().getRecipeError();
        if (recipeError != null) {
            guiGraphics.drawString(font,
                  Component.translatable("gui.c2f4nfluidcrafting.recipe_error",
                        Component.translatable(recipeError.getTranslationKey())),
                  16, 101, 0xFFFF5555, true);
        }

        // 主界面元素前景（悬停/按下覆盖层），低于一切窗口
        int zOffset = 200;
        for (GuiElement element : elements) {
            pose.pushPose();
            element.onRenderForeground(guiGraphics, mouseX, mouseY, zOffset);
            pose.popPose();
        }
        int maxZOffset = zOffset + 50;

        // 窗口从底到顶依次绘制，顶层最后画；非顶层盖模糊蒙层
        for (int i = 0; i < windowStack.size(); i++) {
            GuiWindow window = windowStack.get(i);
            zOffset = maxZOffset + 150;
            pose.pushPose();
            window.onRenderForeground(guiGraphics, mouseX, mouseY, zOffset);
            if (i < windowStack.size() - 1) {
                window.renderBlur(guiGraphics);
            }
            pose.popPose();
            maxZOffset = Math.max(maxZOffset, zOffset + 50);
        }

        // tooltip 抬到最高层，并切回屏幕坐标
        pose.translate(0, 0, maxZOffset);
        pose.translate(-leftPos, -topPos, 0);
        GuiElement tooltipTarget = null;
        GuiWindow hoveredWindow = getWindowHovering(mouseX, mouseY);
        if (hoveredWindow != null) {
            tooltipTarget = hoveredWindow;
        } else {
            for (int i = elements.size() - 1; i >= 0; i--) {
                GuiElement element = elements.get(i);
                if (element.isMouseOver(mouseX, mouseY)) {
                    tooltipTarget = element;
                    break;
                }
            }
        }
        if (tooltipTarget != null) {
            tooltipTarget.renderTooltip(guiGraphics, mouseX, mouseY);
        }
        pose.translate(leftPos, topPos, 0);
        // 原版拖拽物品在本方法之后渲染：再 +200 让它和其装饰层都浮在窗口之上
        pose.translate(0, 0, 200);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        // 整体后移，为窗口、tooltip、拖拽物品的 z 层级腾出空间
        pose.translate(0, 0, -500);
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        pose.popPose();
        // AbstractContainerScreen 本身不画槽位物品名 tooltip（那是 ContainerScreen 做的），显式补上
        if (menu.getCarried().isEmpty() && hoveredSlot != null && hoveredSlot.hasItem() && hoveredSlot.isActive()) {
            renderTooltip(guiGraphics, mouseX, mouseY);
        }
    }

    private void drawProgressBar(GuiGraphics guiGraphics) {
        int progress = menu.getBlockEntity().getProgress();
        int duration = menu.getBlockEntity().getCurrentDuration();
        int x = leftPos + PROGRESS_X;
        int y = topPos + PROGRESS_Y;
        guiGraphics.fill(x, y, x + PROGRESS_WIDTH, y + PROGRESS_HEIGHT, PROGRESS_TRACK);
        float fraction = duration <= 0 ? 0 : Math.min(1f, progress / (float) duration);
        int filledWidth = Math.round((PROGRESS_WIDTH - 2) * Math.max(0, fraction));
        if (filledWidth > 0) {
            guiGraphics.fill(x + 1, y + 1, x + 1 + filledWidth, y + PROGRESS_HEIGHT - 1, PROGRESS_FILL);
        }
        guiGraphics.renderOutline(x, y, PROGRESS_WIDTH, PROGRESS_HEIGHT, PROGRESS_BORDER);
    }

    // ---------- 输入与命中 ----------

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            leftMouseDown = true;
            pressMouseX = mouseX;
            pressMouseY = mouseY;
        }
        // 窗口优先，最上层先试
        for (int i = windowStack.size() - 1; i >= 0; i--) {
            GuiWindow window = windowStack.get(i);
            GuiElement hit = window.mouseClickedNested(mouseX, mouseY, button);
            if (hit != null) {
                focusWindow(window);
                focusedElement = hit;
                return true;
            }
        }
        // 再分发主界面元素
        for (int i = elements.size() - 1; i >= 0; i--) {
            GuiElement hit = elements.get(i).mouseClickedNested(mouseX, mouseY, button);
            if (hit != null) {
                focusedElement = hit;
                return true;
            }
        }
        focusedElement = null;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        boolean handled = button == 0 && focusedElement != null;
        if (handled) {
            focusedElement.onDrag(mouseX, mouseY, dragX, dragY);
        }
        return handled || super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            leftMouseDown = false;
        }
        GuiUpgradeSlot upgradeSlot = findUpgradeSlot(mouseX, mouseY);
        GuiElement previousFocus = focusedElement;
        boolean handled = false;
        if (previousFocus != null) {
            previousFocus.onRelease(mouseX, mouseY);
            handled = true;
        }
        // 原版 findSlot 不会把窗口里的虚拟槽当目标，这里补上“从背包拖到输入/输出槽”的松手逻辑；
        // 直接点击该槽时 previousFocus == upgradeSlot，避免重复执行一次拾取。
        if (upgradeSlot != null && !menu.getCarried().isEmpty() && previousFocus != upgradeSlot) {
            upgradeSlot.handleClick(button);
            playClickSound();
            handled = true;
        }
        // 从背包拖桶/储罐到流体槽时，原版不会命中我们的 GuiTankGauge，这里补一次。
        GuiTankGauge tankGauge = findTankGauge(mouseX, mouseY);
        if (tankGauge != null && !menu.getCarried().isEmpty() && previousFocus != tankGauge) {
            tankGauge.onClick(mouseX, mouseY, button);
            playClickSound();
            handled = true;
        }
        focusedElement = null;
        return super.mouseReleased(mouseX, mouseY, button) || handled;
    }

    @Nullable
    private GuiUpgradeSlot findUpgradeSlot(double mouseX, double mouseY) {
        GuiWindow window = getWindowHovering(mouseX, mouseY);
        if (window == null) {
            return null;
        }
        for (GuiElement child : window.getChildren()) {
            if (child instanceof GuiUpgradeSlot slot && slot.isMouseOverSelf(mouseX, mouseY)) {
                return slot;
            }
        }
        return null;
    }

    @Nullable
    private GuiTankGauge findTankGauge(double mouseX, double mouseY) {
        if (getWindowHovering(mouseX, mouseY) != null) {
            return null;
        }
        for (int i = elements.size() - 1; i >= 0; i--) {
            GuiElement element = elements.get(i);
            if (element instanceof GuiTankGauge gauge && gauge.isMouseOverSelf(mouseX, mouseY)) {
                return gauge;
            }
        }
        return null;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        for (int i = windowStack.size() - 1; i >= 0; i--) {
            if (windowStack.get(i).mouseScrolled(mouseX, mouseY, delta)) {
                return true;
            }
        }
        for (int i = elements.size() - 1; i >= 0; i--) {
            if (elements.get(i).mouseScrolled(mouseX, mouseY, delta)) {
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // E 关闭当前焦点窗口；ESC 保持原版行为，直接关闭整个界面
        if (keyCode == GLFW.GLFW_KEY_E && !windowStack.isEmpty()) {
            windowStack.get(windowStack.size() - 1).close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop, int mouseButton) {
        GuiWindow window = getWindowHovering(mouseX, mouseY);
        return (window == null || window.allowsContainerInteraction(mouseX, mouseY))
              && super.hasClickedOutside(mouseX, mouseY, guiLeft, guiTop, mouseButton);
    }

    // 1.20.1 的 findSlot/isHovering(Slot) 是 private，但两者最终都走
    // protected isHovering(int,int,int,int,double,double)，覆盖它即可挡住窗口下的槽位。
    @Override
    protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        GuiWindow window = getWindowHovering(mouseX, mouseY);
        return super.isHovering(x, y, width, height, mouseX, mouseY)
              && (window == null || window.allowsContainerInteraction(mouseX, mouseY));
    }

    @Override
    public void removed() {
        for (GuiWindow window : List.copyOf(windowStack)) {
            window.close();
        }
        super.removed();
    }

    // ---------- IGuiHost ----------

    @Override
    public int getGuiLeft() {
        return leftPos;
    }

    @Override
    public int getGuiTop() {
        return topPos;
    }

    @Override
    public int getGuiWidth() {
        return imageWidth;
    }

    @Override
    public int getGuiHeight() {
        return imageHeight;
    }

    @Override
    public Font getFont() {
        return font;
    }

    @Override
    public BasicFluidMixerContainer getMenu() {
        return menu;
    }

    @Override
    public void playClickSound() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    public boolean isLeftMouseDown() {
        return leftMouseDown;
    }

    @Override
    public double getPressMouseX() {
        return pressMouseX;
    }

    @Override
    public double getPressMouseY() {
        return pressMouseY;
    }

    @Override
    public float getPartialTick() {
        return partialTick;
    }
}
