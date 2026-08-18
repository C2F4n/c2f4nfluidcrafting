package github.c2f4n.fluidcrafting.client.gui;

import github.c2f4n.fluidcrafting.c2f4nfluidcrafting;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

/** 集中管理 GUI 贴图资源位置。 */
public final class GuiTextures {

    public static final ResourceLocation BASE = gui("base");
    public static final ResourceLocation SHADOW = gui("shadow");
    public static final ResourceLocation BLUR = gui("blur");

    public static final ResourceLocation BTN_IO_OPEN = gui("io_config_open");
    public static final ResourceLocation BTN_REDSTONE_OPEN = gui("redstone_open");
    public static final ResourceLocation BTN_MINING_OPEN = gui("mining_config_open");
    public static final ResourceLocation BTN_UPGRADE_OPEN = gui("upgrade_open");

    public static final ResourceLocation BTN_CLOSE = gui("button_close");
    public static final ResourceLocation BTN_TAB_FLUID = gui("tab_fluid");
    public static final ResourceLocation BTN_TAB_ITEM = gui("tab_item");
    public static final ResourceLocation BTN_AUTO_EJECT = gui("button_auto_eject");
    public static final ResourceLocation PANEL_TEXT = gui("panel_text");
    public static final ResourceLocation SIDE_HOLDER = gui("side_holder");
    public static final ResourceLocation HOLDER_LEFT = gui("holder_left");
    public static final ResourceLocation HOLDER_RIGHT = gui("holder_right");
    public static final ResourceLocation ELEMENT_HOLDER = gui("element_holder");
    public static final ResourceLocation BUTTON = gui("button");

    public static final ResourceLocation SLOT_INPUT = gui("slot/input");
    public static final ResourceLocation SLOT_OUTPUT = gui("slot/output");
    public static final ResourceLocation SLOT_NORMAL = gui("slot/normal");
    public static final ResourceLocation OVERLAY_INPUT = gui("slot/overlay_input");
    public static final ResourceLocation OVERLAY_OUTPUT = gui("slot/overlay_output");
    public static final ResourceLocation OVERLAY_UPGRADE = gui("slot/overlay_upgrade");
    public static final ResourceLocation OVERLAY_CONTAINER = gui("slot/overlay_container");

    public static final ResourceLocation GAUGE_TALL = gui("gauge/16x80");
    public static final ResourceLocation GAUGE_BACKGROUND = gui("gauge/background");

    private GuiTextures() {
    }

    private static ResourceLocation gui(String name) {
        return Objects.requireNonNull(
              ResourceLocation.tryParse(c2f4nfluidcrafting.MODID + ":textures/gui/" + name + ".png"));
    }
}
