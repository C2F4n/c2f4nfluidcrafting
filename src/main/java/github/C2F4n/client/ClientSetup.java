package github.C2F4n.client;

import github.C2F4n.c2f4nfluidcrafting;
import github.C2F4n.client.gui.BasicFluidMixerScreen;
import github.C2F4n.client.renderer.BasicFluidMixerRenderer;
import github.C2F4n.registry.ModBlockEntities;
import github.C2F4n.registry.ModMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = c2f4nfluidcrafting.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(ModMenuTypes.BASIC_FLUID_MIXER.get(), BasicFluidMixerScreen::new));
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.BASIC_FLUID_MIXER.get(), BasicFluidMixerRenderer::new);
    }
}
