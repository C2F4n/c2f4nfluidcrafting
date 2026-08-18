package github.c2f4n.fluidcrafting.common.network.to_server;

import github.c2f4n.fluidcrafting.common.inventory.container.ModContainer;
import github.c2f4n.fluidcrafting.common.inventory.container.slot.SelectedWindowData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/** 客户端告知服务端当前选中的窗口（用于升级虚拟槽的可见性）。 */
public class PacketWindowSelect {

    @Nullable
    private final SelectedWindowData window;

    public PacketWindowSelect(@Nullable SelectedWindowData window) {
        this.window = window;
    }

    public static void encode(PacketWindowSelect msg, FriendlyByteBuf buffer) {
        buffer.writeBoolean(msg.window != null);
        if (msg.window != null) {
            buffer.writeEnum(msg.window.type);
        }
    }

    public static PacketWindowSelect decode(FriendlyByteBuf buffer) {
        return buffer.readBoolean()
              ? new PacketWindowSelect(new SelectedWindowData(buffer.readEnum(SelectedWindowData.WindowType.class)))
              : new PacketWindowSelect(null);
    }

    public static void handle(PacketWindowSelect msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Player player = ctx.get().getSender();
            if (player != null && player.containerMenu instanceof ModContainer container) {
                container.setSelectedWindow(player.getUUID(), msg.window);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
