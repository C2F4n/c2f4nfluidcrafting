package github.C2F4n.common.network;

import github.C2F4n.c2f4nfluidcrafting;
import github.C2F4n.common.network.to_client.PacketSyncUpgradeRegistry;
import github.C2F4n.common.upgrade.UpgradeRegistry;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 服务端升级定义重载后同步给所有在线玩家；新登录玩家单独补发。 */
@Mod.EventBusSubscriber(modid = c2f4nfluidcrafting.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class UpgradeRegistrySyncHandler {

    private UpgradeRegistrySyncHandler() {
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        PacketSyncUpgradeRegistry packet = new PacketSyncUpgradeRegistry(
              UpgradeRegistry.server().getDefinitions());
        if (event.getPlayer() != null) {
            PacketHandler.sendTo(packet, event.getPlayer());
        } else {
            event.getPlayers().forEach(player -> PacketHandler.sendTo(packet, player));
        }
    }
}
