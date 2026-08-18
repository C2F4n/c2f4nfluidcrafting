package github.c2f4n.fluidcrafting.common.tile.component.config;

import github.c2f4n.fluidcrafting.common.lib.RelativeSide;
import net.minecraft.core.Direction;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/** 一个传输类型的面配置：相对面 → DataType，附带自动弹出开关。 */
public class ConfigInfo {

    private final Supplier<Direction> facingSupplier;
    private final Map<RelativeSide, DataType> sideConfig = new EnumMap<>(RelativeSide.class);
    private final Set<DataType> supported;
    private boolean canEject = true;
    private boolean ejecting;

    public ConfigInfo(Supplier<Direction> facingSupplier, TransmissionType type) {
        this.facingSupplier = facingSupplier;
        this.supported = EnumSet.noneOf(DataType.class);
        for (DataType dataType : type.getSupportedDataTypes()) {
            supported.add(dataType);
        }
        for (RelativeSide side : RelativeSide.values()) {
            sideConfig.put(side, DataType.NONE);
        }
    }

    public boolean canEject() {
        return canEject;
    }

    public void setCanEject(boolean canEject) {
        this.canEject = canEject;
    }

    public boolean isEjecting() {
        return ejecting;
    }

    public void setEjecting(boolean ejecting) {
        this.ejecting = ejecting;
    }

    public DataType getDataType(RelativeSide side) {
        return sideConfig.get(side);
    }

    public DataType getDataType(Direction direction) {
        return getDataType(RelativeSide.fromDirections(facingSupplier.get(), direction));
    }

    public void setDataType(DataType dataType, RelativeSide... sides) {
        for (RelativeSide side : sides) {
            if (supported.contains(dataType)) {
                sideConfig.put(side, dataType);
            }
        }
    }

    public DataType increment(RelativeSide side) {
        DataType current = getDataType(side);
        DataType next = current == null ? DataType.NONE : cycle(current, false);
        sideConfig.put(side, next);
        return next;
    }

    public DataType decrement(RelativeSide side) {
        DataType current = getDataType(side);
        DataType next = current == null ? DataType.NONE : cycle(current, true);
        sideConfig.put(side, next);
        return next;
    }

    private DataType cycle(DataType current, boolean reverse) {
        DataType[] values = new DataType[supported.size()];
        int index = 0;
        int currentIndex = 0;
        for (DataType type : supported) {
            values[index] = type;
            if (type == current) {
                currentIndex = index;
            }
            index++;
        }
        int size = values.length;
        currentIndex = reverse ? (currentIndex - 1 + size) % size : (currentIndex + 1) % size;
        return values[currentIndex];
    }

    public Set<Direction> getSidesFor(DataType dataType) {
        Set<Direction> directions = EnumSet.noneOf(Direction.class);
        for (Map.Entry<RelativeSide, DataType> entry : sideConfig.entrySet()) {
            if (entry.getValue() == dataType) {
                directions.add(entry.getKey().getDirection(facingSupplier.get()));
            }
        }
        return directions;
    }

    public boolean isSideEmpty() {
        for (DataType type : sideConfig.values()) {
            if (type != DataType.NONE) {
                return false;
            }
        }
        return true;
    }
}
