package github.c2f4n.fluidcrafting.common.inventory.container.slot;

import java.util.Objects;

/** 当前选中的 GUI 窗口；服务端按玩家记录，用于虚拟槽的可见性。 */
public class SelectedWindowData {

    public static final SelectedWindowData UNSPECIFIED = new SelectedWindowData(WindowType.UNSPECIFIED);

    public final WindowType type;

    public SelectedWindowData(WindowType type) {
        this.type = Objects.requireNonNull(type);
    }

    public boolean isSpecified() {
        return type != WindowType.UNSPECIFIED;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SelectedWindowData other && type == other.type;
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    public enum WindowType {
        UNSPECIFIED,
        IO_CONFIG,
        UPGRADE
    }
}
