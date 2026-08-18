package github.c2f4n.fluidcrafting.common.upgrade;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 升级定义的客户端/服务端注册表。数据来自 data/&lt;namespace&gt;/upgrades 下的 JSON。 */
public final class UpgradeRegistry {

    private static final UpgradeRegistry SERVER = new UpgradeRegistry();
    private static final UpgradeRegistry CLIENT = new UpgradeRegistry();

    private volatile Map<ResourceLocation, UpgradeDefinition> byId = Map.of();
    private volatile Map<Item, UpgradeDefinition> byItem = Map.of();
    private volatile List<UpgradeDefinition> ordered = List.of();

    private UpgradeRegistry() {
    }

    public static UpgradeRegistry server() {
        return SERVER;
    }

    public static UpgradeRegistry client() {
        return CLIENT;
    }

    /** 按方块实体所在 Level 选择注册表，避免单机集成服务器被物理端判断误导。 */
    public static UpgradeRegistry getForLevel(@Nullable Level level) {
        return level != null && level.isClientSide ? CLIENT : SERVER;
    }

    public void replace(Collection<UpgradeDefinition> definitions) {
        Map<ResourceLocation, UpgradeDefinition> newById = new HashMap<>();
        Map<Item, UpgradeDefinition> newByItem = new HashMap<>();
        List<UpgradeDefinition> newOrdered = new ArrayList<>(definitions);
        newOrdered.sort(Comparator.comparing(UpgradeDefinition::id));
        List<UpgradeDefinition> valid = new ArrayList<>();
        for (UpgradeDefinition definition : newOrdered) {
            if (definition.maxCount() <= 0) {
                continue;
            }
            newById.put(definition.id(), definition);
            valid.add(definition);
            UpgradeDefinition existing = newByItem.putIfAbsent(definition.item(), definition);
            if (existing != null) {
                // 同一个物品有多个定义时保留 ID 字典序最小的定义，避免输入槽校验出现歧义。
                newByItem.put(definition.item(), existing);
            }
        }
        byId = Map.copyOf(newById);
        byItem = Map.copyOf(newByItem);
        ordered = List.copyOf(valid);
    }

    @Nullable
    public UpgradeDefinition get(ResourceLocation id) {
        return byId.get(id);
    }

    @Nullable
    public UpgradeDefinition getForItem(Item item) {
        return byItem.get(item);
    }

    @Nullable
    public UpgradeDefinition getForStack(ItemStack stack) {
        return stack.isEmpty() ? null : getForItem(stack.getItem());
    }

    public List<UpgradeDefinition> getDefinitions() {
        return ordered;
    }
}
