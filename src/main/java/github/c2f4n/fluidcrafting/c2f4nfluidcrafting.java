package github.c2f4n.fluidcrafting;

import github.c2f4n.fluidcrafting.registry.ModBlockEntities;
import github.c2f4n.fluidcrafting.registry.ModBlocks;
import github.c2f4n.fluidcrafting.registry.ModCreativeTabs;
import github.c2f4n.fluidcrafting.registry.ModItems;
import github.c2f4n.fluidcrafting.registry.ModMenuTypes;
import github.c2f4n.fluidcrafting.registry.ModRecipeTypes;
import github.c2f4n.fluidcrafting.common.network.PacketHandler;
import github.c2f4n.fluidcrafting.common.network.to_client.PacketSyncMachine;
import github.c2f4n.fluidcrafting.common.network.to_client.PacketSyncUpgradeRegistry;
import github.c2f4n.fluidcrafting.common.network.to_client.PacketUpdateContainer;
import github.c2f4n.fluidcrafting.common.network.to_server.PacketGuiInteract;
import github.c2f4n.fluidcrafting.common.network.to_server.PacketJeiRecipeTransfer;
import github.c2f4n.fluidcrafting.common.network.to_server.PacketWindowSelect;
import github.c2f4n.fluidcrafting.common.upgrade.UpgradeReloadListener;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(c2f4nfluidcrafting.MODID)
public class c2f4nfluidcrafting {
    public static final String MODID = "c2f4nfluidcrafting";
    private static final UpgradeReloadListener UPGRADE_RELOAD_LISTENER = new UpgradeReloadListener();

    public c2f4nfluidcrafting() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModMenuTypes.MENUS.register(modEventBus);
        ModRecipeTypes.SERIALIZERS.register(modEventBus);
        ModRecipeTypes.TYPES.register(modEventBus);
        registerPackets();

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.addListener(this::addReloadListeners);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, MachineConfig.SPEC);
    }

    private void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(UPGRADE_RELOAD_LISTENER);
    }

    private static void registerPackets() {
        int id = 0;
        PacketHandler.CHANNEL.registerMessage(id, PacketGuiInteract.class,
              PacketGuiInteract::encode, PacketGuiInteract::decode, PacketGuiInteract::handle);
        id += 1;
        PacketHandler.CHANNEL.registerMessage(id, PacketWindowSelect.class,
              PacketWindowSelect::encode, PacketWindowSelect::decode, PacketWindowSelect::handle);
        id += 1;
        PacketHandler.CHANNEL.registerMessage(id, PacketUpdateContainer.class,
              PacketUpdateContainer::encode, PacketUpdateContainer::decode, PacketUpdateContainer::handle);
        id += 1;
        PacketHandler.CHANNEL.registerMessage(id, PacketSyncMachine.class,
              PacketSyncMachine::encode, PacketSyncMachine::decode, PacketSyncMachine::handle);
        id += 1;
        PacketHandler.CHANNEL.registerMessage(id, PacketSyncUpgradeRegistry.class,
              PacketSyncUpgradeRegistry::encode, PacketSyncUpgradeRegistry::decode, PacketSyncUpgradeRegistry::handle);
        id += 1;
        PacketHandler.CHANNEL.registerMessage(id, PacketJeiRecipeTransfer.class,
              PacketJeiRecipeTransfer::encode, PacketJeiRecipeTransfer::decode, PacketJeiRecipeTransfer::handle);
    }
}
