package github.C2F4n.common.tile.component;

import github.C2F4n.common.inventory.container.ModContainer;
import github.C2F4n.common.inventory.container.sync.SyncableBoolean;
import github.C2F4n.common.inventory.container.sync.SyncableInt;
import github.C2F4n.common.lib.RelativeSide;
import github.C2F4n.common.tile.base.TileEntityBase;
import github.C2F4n.common.tile.component.config.ConfigInfo;
import github.C2F4n.common.tile.component.config.DataType;
import github.C2F4n.common.tile.component.config.TransmissionType;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** 面配置组件：流体/物品各一套 ConfigInfo，按相对面存储。 */
public class TileComponentConfig implements ITileComponent {

    private final TileEntityBase tile;
    private final Map<TransmissionType, ConfigInfo> configs = new EnumMap<>(TransmissionType.class);
    private final List<TransmissionType> transmissionTypes = new ArrayList<>();

    public TileComponentConfig(TileEntityBase tile, TransmissionType... types) {
        this.tile = tile;
        for (TransmissionType type : types) {
            addSupported(type);
        }
        tile.addComponent(this);
    }

    public void addSupported(TransmissionType type) {
        if (!configs.containsKey(type)) {
            configs.put(type, new ConfigInfo(this::getFacing, type));
            transmissionTypes.add(type);
        }
    }

    private Direction getFacing() {
        return tile.getBlockState().getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING);
    }

    public List<TransmissionType> getTransmissions() {
        return transmissionTypes;
    }

    public ConfigInfo getConfig(TransmissionType type) {
        return configs.get(type);
    }

    public DataType getDataType(TransmissionType type, Direction side) {
        ConfigInfo config = configs.get(type);
        return config == null ? DataType.NONE : config.getDataType(side);
    }

    public void setDataType(TransmissionType type, Direction side, DataType dataType) {
        ConfigInfo config = configs.get(type);
        if (config != null) {
            config.setDataType(dataType, RelativeSide.fromDirections(getFacing(), side));
            tile.onContentsChanged();
        }
    }

    public DataType increment(TransmissionType type, Direction side) {
        ConfigInfo config = configs.get(type);
        if (config == null) {
            return DataType.NONE;
        }
        DataType next = config.increment(RelativeSide.fromDirections(getFacing(), side));
        tile.onContentsChanged();
        return next;
    }

    public DataType decrement(TransmissionType type, Direction side) {
        ConfigInfo config = configs.get(type);
        if (config == null) {
            return DataType.NONE;
        }
        DataType next = config.decrement(RelativeSide.fromDirections(getFacing(), side));
        tile.onContentsChanged();
        return next;
    }

    public void setEjecting(TransmissionType type, boolean ejecting) {
        ConfigInfo config = configs.get(type);
        if (config != null) {
            config.setEjecting(ejecting);
            tile.onContentsChanged();
        }
    }

    public boolean isEjecting(TransmissionType type) {
        ConfigInfo config = configs.get(type);
        return config != null && config.isEjecting();
    }

    @Override
    public void addContainerTrackers(ModContainer container) {
        for (TransmissionType type : transmissionTypes) {
            ConfigInfo config = configs.get(type);
            container.track(new SyncableBoolean(() -> config.isEjecting(), value -> config.setEjecting(value)));
            for (RelativeSide side : RelativeSide.values()) {
                container.track(new SyncableInt(
                      () -> config.getDataType(side).ordinal(),
                      value -> config.setDataType(DataType.values()[value], side)));
            }
        }
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        for (TransmissionType type : transmissionTypes) {
            ConfigInfo config = configs.get(type);
            CompoundTag data = new CompoundTag();
            data.putBoolean("ejecting", config.isEjecting());
            CompoundTag sides = new CompoundTag();
            for (RelativeSide side : RelativeSide.values()) {
                sides.putInt(side.name(), config.getDataType(side).ordinal());
            }
            data.put("sides", sides);
            tag.put("side_config_" + type.name().toLowerCase(), data);
        }
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        for (TransmissionType type : transmissionTypes) {
            ConfigInfo config = configs.get(type);
            String key = "side_config_" + type.name().toLowerCase();
            if (tag.contains(key)) {
                CompoundTag data = tag.getCompound(key);
                config.setEjecting(data.getBoolean("ejecting"));
                CompoundTag sides = data.getCompound("sides");
                for (RelativeSide side : RelativeSide.values()) {
                    config.setDataType(DataType.values()[sides.getInt(side.name())], side);
                }
            }
        }
    }
}
