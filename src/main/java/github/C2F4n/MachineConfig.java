package github.C2F4n;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

/** 机器运行参数。修改后需要重启游戏或 /reload 对应的 Forge 配置。 */
@Mod.EventBusSubscriber(modid = c2f4nfluidcrafting.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class MachineConfig {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.IntValue TANK_CAPACITY = BUILDER
          .comment("Capacity of each fluid tank in mB.")
          .defineInRange("tankCapacity", 8000, 1, 1_000_000);
    public static final ForgeConfigSpec.IntValue FLUID_EJECT_PER_TICK = BUILDER
          .comment("Maximum mB automatically ejected per output face each tick.")
          .defineInRange("fluidEjectPerTick", 10000, 0, 1_000_000);
    public static final ForgeConfigSpec.IntValue CONTAINER_INTERVAL = BUILDER
          .comment("Ticks between each container slot fill/drain attempt.")
          .defineInRange("containerInterval", 10, 1, 1200);
    public static final ForgeConfigSpec.IntValue BUCKET_AMOUNT = BUILDER
          .comment("mB transferred by one right-click with a fluid container.")
          .defineInRange("bucketAmount", 1000, 1, 1_000_000);
    public static final ForgeConfigSpec.IntValue UPGRADE_INSTALL_TICKS = BUILDER
          .comment("Ticks required to install one batch of upgrades.")
          .defineInRange("upgradeInstallTicks", 20, 1, 1200);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    private static int tankCapacity = 8000;
    private static int fluidEjectPerTick = 10000;
    private static int containerInterval = 10;
    private static int bucketAmount = 1000;
    private static int upgradeInstallTicks = 20;

    private MachineConfig() {
    }

    public static int tankCapacity() {
        return tankCapacity;
    }

    public static int fluidEjectPerTick() {
        return fluidEjectPerTick;
    }

    public static int containerInterval() {
        return containerInterval;
    }

    public static int bucketAmount() {
        return bucketAmount;
    }

    public static int upgradeInstallTicks() {
        return upgradeInstallTicks;
    }

    @SubscribeEvent
    public static void onLoad(ModConfigEvent event) {
        if (event.getConfig().getSpec() != SPEC) {
            return;
        }
        tankCapacity = TANK_CAPACITY.get();
        fluidEjectPerTick = FLUID_EJECT_PER_TICK.get();
        containerInterval = CONTAINER_INTERVAL.get();
        bucketAmount = BUCKET_AMOUNT.get();
        upgradeInstallTicks = UPGRADE_INSTALL_TICKS.get();
    }
}
