package github.c2f4n.fluidcrafting.common.network.to_client;

import github.c2f4n.fluidcrafting.common.upgrade.UpgradeDefinition;
import github.c2f4n.fluidcrafting.common.upgrade.UpgradeKind;
import github.c2f4n.fluidcrafting.common.upgrade.UpgradeRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** 数据包重载或玩家登录时，把服务端升级定义同步到客户端。 */
public record PacketSyncUpgradeRegistry(List<UpgradeDefinition> definitions) {

    public static void encode(PacketSyncUpgradeRegistry msg, FriendlyByteBuf buffer) {
        buffer.writeVarInt(msg.definitions().size());
        for (UpgradeDefinition definition : msg.definitions()) {
            buffer.writeResourceLocation(definition.id());
            buffer.writeResourceLocation(ForgeRegistries.ITEMS.getKey(definition.item()));
            buffer.writeEnum(definition.kind());
            buffer.writeDouble(definition.speedMultiplier());
            buffer.writeVarInt(definition.maxCount());
        }
    }

    public static PacketSyncUpgradeRegistry decode(FriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        List<UpgradeDefinition> definitions = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            ResourceLocation id = buffer.readResourceLocation();
            ResourceLocation itemId = buffer.readResourceLocation();
            Item item = ForgeRegistries.ITEMS.getValue(itemId);
            UpgradeKind kind = buffer.readEnum(UpgradeKind.class);
            double speedMultiplier = buffer.readDouble();
            int maxCount = buffer.readVarInt();
            if (item != null) {
                definitions.add(new UpgradeDefinition(id, item, kind, speedMultiplier, maxCount));
            }
        }
        return new PacketSyncUpgradeRegistry(definitions);
    }

    public static void handle(PacketSyncUpgradeRegistry msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> UpgradeRegistry.client().replace(msg.definitions()));
        ctx.get().setPacketHandled(true);
    }
}
