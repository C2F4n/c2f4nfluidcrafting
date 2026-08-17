package github.C2F4n.client.gui.window;

import java.util.EnumMap;
import java.util.Map;

/**
 * 小窗口类型。每种窗口记录上一次的拖动位置，
 * 关闭再打开时恢复（客户端会话内）。
 */
public enum WindowType {
    IO_CONFIG(128, 148),
    UPGRADE(156, 100);

    private static final Map<WindowType, Position> LAST_POSITIONS = new EnumMap<>(WindowType.class);

    private final int width;
    private final int height;

    WindowType(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @org.jetbrains.annotations.Nullable
    public Position getLastPosition() {
        return LAST_POSITIONS.get(this);
    }

    public void saveLastPosition(int relativeX, int relativeY) {
        LAST_POSITIONS.put(this, new Position(relativeX, relativeY));
    }

    public record Position(int x, int y) {
    }
}
