package github.c2f4n.fluidcrafting.client.gui.element;

import github.c2f4n.fluidcrafting.client.gui.IGuiHost;
import github.c2f4n.fluidcrafting.client.gui.element.scroll.GuiScrollList;
import github.c2f4n.fluidcrafting.client.gui.util.ScrollingText;
import github.c2f4n.fluidcrafting.common.tile.component.TileComponentUpgrade;
import github.c2f4n.fluidcrafting.common.upgrade.UpgradeDefinition;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** 升级窗口左侧的已装载升级列表，通用滚动与行选择由 GuiScrollList 提供。 */
public class GuiUpgradeList extends GuiScrollList<UpgradeDefinition> {

    private static final int ROW_HEIGHT = 12;
    private static final int TEXT_LEFT = 13;

    private final Supplier<UpgradeDefinition> selected;
    private final Map<ResourceLocation, ScrollingText> scrollingTexts = new HashMap<>();

    public GuiUpgradeList(IGuiHost gui, int relativeX, int relativeY, int width, int height,
                          Supplier<TileComponentUpgrade> component, Supplier<UpgradeDefinition> selected,
                          Consumer<UpgradeDefinition> onSelect) {
        super(gui, relativeX, relativeY, width, height, ROW_HEIGHT,
              () -> component.get().getInstalledDefinitions(), onSelect);
        this.selected = selected;
    }

    @Override
    public void tick() {
        super.tick();
        UpgradeDefinition externalSelection = selected.get();
        if (getSelected() != externalSelection) {
            setSelected(externalSelection);
        }
        List<UpgradeDefinition> installed = getElements();
        Set<ResourceLocation> currentIds = new HashSet<>();
        for (UpgradeDefinition definition : installed) {
            currentIds.add(definition.id());
            ScrollingText state = scrollingTexts.computeIfAbsent(definition.id(), id -> new ScrollingText());
            state.tick(gui.getFont(), definition.getDisplayName().getString(), maxTextWidth());
        }
        scrollingTexts.keySet().removeIf(id -> !currentIds.contains(id));
    }

    private int maxTextWidth() {
        return Math.max(1, width - TEXT_LEFT - 2);
    }

    @Override
    protected void renderRow(GuiGraphics guiGraphics, UpgradeDefinition element, int index,
                             int rowY, int mouseX, int mouseY) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(relativeX + 3, rowY + 2, 0);
        guiGraphics.pose().scale(0.5f, 0.5f, 1f);
        guiGraphics.renderItem(element.createStack(1), 0, 0);
        guiGraphics.pose().popPose();
        ScrollingText state = scrollingTexts.computeIfAbsent(element.id(), id -> new ScrollingText());
        state.prepare(gui.getFont(), element.getDisplayName().getString(), maxTextWidth());
        guiGraphics.drawString(gui.getFont(), element.getDisplayName(),
              relativeX + TEXT_LEFT - state.getOffset(), rowY + 2, 0x404040, false);
    }

    @Override
    protected void renderRowTooltip(GuiGraphics guiGraphics, UpgradeDefinition element,
                                    int mouseX, int mouseY) {
        gui.displayTooltip(guiGraphics, mouseX, mouseY,
              element.getDisplayName(), element.getDescription());
    }
}
