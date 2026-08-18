package github.c2f4n.fluidcrafting.common.tile.component;

import github.c2f4n.fluidcrafting.MachineConfig;
import github.c2f4n.fluidcrafting.common.inventory.container.sync.ISyncableData;
import github.c2f4n.fluidcrafting.common.inventory.container.sync.SyncableInt;
import github.c2f4n.fluidcrafting.common.inventory.container.sync.SyncableItemStack;
import github.c2f4n.fluidcrafting.common.inventory.slot.BasicInventorySlot;
import github.c2f4n.fluidcrafting.common.tile.base.TileEntityBase;
import github.c2f4n.fluidcrafting.common.upgrade.UpgradeDefinition;
import github.c2f4n.fluidcrafting.common.upgrade.UpgradeKind;
import github.c2f4n.fluidcrafting.common.upgrade.UpgradeRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据驱动升级组件：升级卡类型与数值来自 {@link UpgradeRegistry}，
 * 已安装数量按定义 ID 存储，输入卡自动安装，卸载时写入输出槽。
 */
public class TileComponentUpgrade implements ITileComponent {

    public static final String NBT_KEY = "upgrade_component";

    private final TileEntityBase tile;
    private final BasicInventorySlot inputSlot;
    private final BasicInventorySlot outputSlot;
    private final Map<ResourceLocation, Integer> upgrades = new HashMap<>();
    private int installTicks;

    public TileComponentUpgrade(TileEntityBase tile) {
        this.tile = tile;
        this.inputSlot = new BasicInventorySlot(tile,
              stack -> registry().getForStack(stack) != null, 64);
        // 输出槽只能由机器写入，玩家不能往里放东西。
        this.outputSlot = new BasicInventorySlot(tile, stack -> false, 64);
        tile.addComponent(this);
    }

    public BasicInventorySlot getInputSlot() {
        return inputSlot;
    }

    public BasicInventorySlot getOutputSlot() {
        return outputSlot;
    }

    public int getUpgrades(ResourceLocation id) {
        return upgrades.getOrDefault(id, 0);
    }

    public List<UpgradeDefinition> getInstalledDefinitions() {
        List<UpgradeDefinition> result = new ArrayList<>();
        for (UpgradeDefinition definition : registry().getDefinitions()) {
            if (getUpgrades(definition.id()) > 0) {
                result.add(definition);
            }
        }
        return result;
    }

    public int getUpgradeCount() {
        int total = 0;
        for (int amount : upgrades.values()) {
            total += amount;
        }
        return total;
    }

    /** 总速度倍率 = Σ(已安装数量 × 每张 speed_multiplier)；0 张时保持基础 1.0。 */
    public double getSpeedMultiplier() {
        boolean hasSpeedUpgrade = false;
        double total = 0;
        for (UpgradeDefinition definition : registry().getDefinitions()) {
            if (definition.kind() != UpgradeKind.SPEED) {
                continue;
            }
            int amount = getUpgrades(definition.id());
            if (amount > 0) {
                hasSpeedUpgrade = true;
                total += amount * definition.speedMultiplier();
            }
        }
        return Math.max(0.05, hasSpeedUpgrade ? total : 1);
    }

    /** 是否装有创造模式升级：配方耗时直接设为 1 tick。 */
    public boolean hasCreativeUpgrade() {
        return registry().getDefinitions().stream()
              .anyMatch(definition -> definition.kind() == UpgradeKind.CREATIVE
                    && getUpgrades(definition.id()) > 0);
    }

    public boolean hasUpgrades() {
        return getUpgradeCount() > 0;
    }

    public double getScaledInstallProgress() {
        return installTicks / (double) MachineConfig.upgradeInstallTicks();
    }

    /** 升级窗口打开时才开始同步的数据。 */
    public List<ISyncableData> getContainerTrackers() {
        List<ISyncableData> trackers = new ArrayList<>();
        trackers.add(new SyncableItemStack(inputSlot::getStack, inputSlot::setStackUnchecked));
        trackers.add(new SyncableItemStack(outputSlot::getStack, outputSlot::setStackUnchecked));
        trackers.add(new SyncableInt(() -> installTicks, value -> installTicks = value));
        for (UpgradeDefinition definition : registry().getDefinitions()) {
            ResourceLocation id = definition.id();
            trackers.add(new SyncableInt(() -> getUpgrades(id), value -> setUpgrades(id, value)));
        }
        return trackers;
    }

    @Override
    public void tickServer() {
        clampToDefinitions();
        ItemStack stack = inputSlot.getStack();
        UpgradeDefinition definition = registry().getForStack(stack);
        if (definition == null || getUpgrades(definition.id()) >= definition.maxCount()) {
            installTicks = 0;
            return;
        }
        if (installTicks < MachineConfig.upgradeInstallTicks()) {
            installTicks++;
            return;
        }
        installTicks = 0;
        int added = addUpgrades(definition.id(), stack.getCount());
        if (added > 0) {
            inputSlot.shrinkStack(added);
        }
    }

    private void clampToDefinitions() {
        UpgradeRegistry registry = registry();
        boolean changed = upgrades.keySet().removeIf(id -> registry.get(id) == null);
        for (UpgradeDefinition definition : registry.getDefinitions()) {
            int amount = getUpgrades(definition.id());
            if (amount > definition.maxCount()) {
                setUpgrades(definition.id(), definition.maxCount());
                changed = true;
            }
        }
        if (changed) {
            tile.onContentsChanged();
        }
    }

    public int addUpgrades(ResourceLocation id, int maxAvailable) {
        UpgradeDefinition definition = registry().get(id);
        if (definition == null) {
            return 0;
        }
        int installed = getUpgrades(id);
        int toAdd = Math.min(definition.maxCount() - installed, Math.max(0, maxAvailable));
        if (toAdd <= 0) {
            return 0;
        }
        setUpgrades(id, installed + toAdd);
        tile.onContentsChanged();
        return toAdd;
    }

    /** 卸下一个（removeAll 为全部），尽量放入输出槽；空间不足则不动。 */
    public boolean removeUpgrade(ResourceLocation id, boolean removeAll) {
        UpgradeDefinition definition = registry().get(id);
        int installed = getUpgrades(id);
        if (definition == null || installed <= 0) {
            return false;
        }
        int toRemove = removeAll ? installed : 1;
        ItemStack remainder = outputSlot.insertItemInternal(definition.createStack(toRemove), true);
        int accepted = toRemove - remainder.getCount();
        if (accepted <= 0) {
            return false;
        }
        ItemStack leftover = outputSlot.insertItemInternal(definition.createStack(accepted), false);
        if (!leftover.isEmpty()) {
            return false;
        }
        int remaining = installed - accepted;
        if (remaining == 0) {
            upgrades.remove(id);
        } else {
            upgrades.put(id, remaining);
        }
        tile.onContentsChanged();
        return true;
    }

    public void setUpgrades(ResourceLocation id, int amount) {
        if (amount <= 0) {
            upgrades.remove(id);
        } else {
            upgrades.put(id, amount);
        }
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        CompoundTag upgradeNbt = new CompoundTag();
        upgradeNbt.put("input", inputSlot.getStack().save(new CompoundTag()));
        upgradeNbt.put("output", outputSlot.getStack().save(new CompoundTag()));
        CompoundTag installed = new CompoundTag();
        for (Map.Entry<ResourceLocation, Integer> entry : upgrades.entrySet()) {
            if (entry.getValue() > 0) {
                installed.putInt(entry.getKey().toString(), entry.getValue());
            }
        }
        upgradeNbt.put("installed", installed);
        upgradeNbt.putInt("install_ticks", installTicks);
        tag.put(NBT_KEY, upgradeNbt);
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        if (!tag.contains(NBT_KEY)) {
            return;
        }
        CompoundTag upgradeNbt = tag.getCompound(NBT_KEY);
        if (upgradeNbt.contains("input")) {
            inputSlot.setStackUnchecked(ItemStack.of(upgradeNbt.getCompound("input")));
        }
        if (upgradeNbt.contains("output")) {
            outputSlot.setStackUnchecked(ItemStack.of(upgradeNbt.getCompound("output")));
        }
        upgrades.clear();
        if (upgradeNbt.contains("installed")) {
            CompoundTag installed = upgradeNbt.getCompound("installed");
            for (String key : installed.getAllKeys()) {
                ResourceLocation id = ResourceLocation.tryParse(key);
                if (id == null && "SPEED".equals(key)) {
                    id = findFirstSpeedDefinition();
                }
                if (id != null) {
                    UpgradeDefinition definition = registry().get(id);
                    int amount = installed.getInt(key);
                    if (definition != null) {
                        amount = Math.min(amount, definition.maxCount());
                    }
                    setUpgrades(id, amount);
                }
            }
        }
        installTicks = upgradeNbt.getInt("install_ticks");
    }

    private ResourceLocation findFirstSpeedDefinition() {
        for (UpgradeDefinition definition : registry().getDefinitions()) {
            if (definition.kind() == github.c2f4n.fluidcrafting.common.upgrade.UpgradeKind.SPEED) {
                return definition.id();
            }
        }
        return null;
    }

    private UpgradeRegistry registry() {
        return UpgradeRegistry.getForLevel(tile.getLevel());
    }
}
