package github.C2F4n.common.lib;

import net.minecraft.core.Direction;

/** 相对方块朝向的面；配置以相对面存储，旋转方块后无需改数据。 */
public enum RelativeSide {
    TOP,
    BOTTOM,
    FRONT,
    BACK,
    LEFT,
    RIGHT;

    public Direction getDirection(Direction facing) {
        return switch (this) {
            case TOP -> Direction.UP;
            case BOTTOM -> Direction.DOWN;
            case FRONT -> facing;
            case BACK -> facing.getOpposite();
            // 与旧实现一致：玩家视角的左/右
            case LEFT -> facing.getClockWise();
            case RIGHT -> facing.getCounterClockWise();
        };
    }

    public static RelativeSide fromDirections(Direction facing, Direction side) {
        if (side == Direction.UP) {
            return TOP;
        } else if (side == Direction.DOWN) {
            return BOTTOM;
        } else if (side == facing) {
            return FRONT;
        } else if (side == facing.getOpposite()) {
            return BACK;
        } else if (side == facing.getClockWise()) {
            return LEFT;
        }
        return RIGHT;
    }
}
