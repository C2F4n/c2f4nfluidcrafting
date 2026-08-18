package github.c2f4n.fluidcrafting.common.network.to_server;

import github.c2f4n.fluidcrafting.block.entity.BasicFluidMixerBlockEntity;
import github.c2f4n.fluidcrafting.block.entity.MiningMode;
import github.c2f4n.fluidcrafting.common.inventory.container.BasicFluidMixerContainer;
import github.c2f4n.fluidcrafting.common.tile.component.TileComponentRedstone;
import github.c2f4n.fluidcrafting.common.tile.component.config.DataType;
import github.c2f4n.fluidcrafting.common.tile.component.config.TransmissionType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/** 统一的 GUI 交互包：一个枚举替代之前 6 个散包。 */
public class PacketGuiInteract {

    private final GuiInteraction interaction;
    private final BlockPos pos;
    private final int extra;
    @Nullable
    private final String upgradeId;

    public PacketGuiInteract(GuiInteraction interaction, BlockPos pos) {
        this(interaction, pos, 0, null);
    }

    public PacketGuiInteract(GuiInteraction interaction, BlockPos pos, int extra) {
        this(interaction, pos, extra, null);
    }

    public PacketGuiInteract(GuiInteraction interaction, BlockPos pos, int extra, @Nullable String upgradeId) {
        this.interaction = interaction;
        this.pos = pos;
        this.extra = extra;
        this.upgradeId = upgradeId;
    }

    public static void encode(PacketGuiInteract msg, FriendlyByteBuf buffer) {
        buffer.writeEnum(msg.interaction);
        buffer.writeBlockPos(msg.pos);
        buffer.writeVarInt(msg.extra);
        buffer.writeBoolean(msg.upgradeId != null);
        if (msg.upgradeId != null) {
            buffer.writeUtf(msg.upgradeId);
        }
    }

    public static PacketGuiInteract decode(FriendlyByteBuf buffer) {
        GuiInteraction interaction = buffer.readEnum(GuiInteraction.class);
        BlockPos pos = buffer.readBlockPos();
        int extra = buffer.readVarInt();
        String upgradeId = buffer.readBoolean() ? buffer.readUtf() : null;
        return new PacketGuiInteract(interaction, pos, extra, upgradeId);
    }

    public static void handle(PacketGuiInteract msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Player player = ctx.get().getSender();
            if (player == null) {
                return;
            }
            if (player.level().getBlockEntity(msg.pos) instanceof BasicFluidMixerBlockEntity be) {
                msg.interaction.consume(be, player, msg.extra, msg.upgradeId);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public enum GuiInteraction {
        FACE_MODE((be, player, extra, upgradeId) -> {
            int directionIndex = extra & 0x0F;
            if (directionIndex < 0 || directionIndex >= Direction.values().length) {
                return;
            }
            Direction side = Direction.values()[directionIndex];
            TransmissionType type = (extra & 0x10) != 0 ? TransmissionType.ITEM : TransmissionType.FLUID;
            if ((extra & 0x40) != 0) {
                be.getConfigComponent().setDataType(type, side, DataType.NONE);
            } else if ((extra & 0x20) != 0) {
                be.getConfigComponent().decrement(type, side);
            } else {
                be.getConfigComponent().increment(type, side);
            }
        }),
        AUTO_EJECT((be, player, extra, upgradeId) -> {
            TransmissionType type = (extra & 1) != 0 ? TransmissionType.FLUID : TransmissionType.ITEM;
            be.getConfigComponent().setEjecting(type, (extra & 2) != 0);
        }),
        REDSTONE_MODE((be, player, extra, upgradeId) -> be.setRedstoneMode(TileComponentRedstone.RedstoneMode.byIndex(extra))),
        MINING_MODE((be, player, extra, upgradeId) -> be.setMiningMode(MiningMode.byIndex(extra))),
        TANK_POUR((be, player, extra, upgradeId) -> be.interactWithTank(player, extra & 0x7F, false, (extra & 0x80) != 0)),
        TANK_EXTRACT((be, player, extra, upgradeId) -> be.interactWithTank(player, extra & 0x7F, true, (extra & 0x80) != 0)),
        UPGRADE_REMOVE((be, player, extra, upgradeId) -> {
            if (upgradeId == null) {
                return;
            }
            ResourceLocation id = ResourceLocation.tryParse(upgradeId);
            if (id != null) {
                be.getUpgradeComponent().removeUpgrade(id, (extra & 1) != 0);
            }
        }),
        CONTAINER_TRACK_UPGRADES((be, player, extra, upgradeId) -> {
            if (player.containerMenu instanceof BasicFluidMixerContainer container) {
                container.startTracking(BasicFluidMixerContainer.UPGRADE_TRACKING,
                      be.getUpgradeComponent().getContainerTrackers());
                container.sendAllDataToRemote();
            }
        }),
        CONTAINER_STOP_TRACKING((be, player, extra, upgradeId) -> {
            if (player.containerMenu instanceof BasicFluidMixerContainer container) {
                container.stopTracking(BasicFluidMixerContainer.UPGRADE_TRACKING);
                container.sendAllDataToRemote();
            }
        });

        private final QuadConsumer<BasicFluidMixerBlockEntity, Player, Integer, String> consumer;

        GuiInteraction(QuadConsumer<BasicFluidMixerBlockEntity, Player, Integer, String> consumer) {
            this.consumer = consumer;
        }

        public void consume(BasicFluidMixerBlockEntity be, Player player, int extra, @Nullable String upgradeId) {
            consumer.accept(be, player, extra, upgradeId);
        }

        @FunctionalInterface
        private interface QuadConsumer<A, B, C, D> {
            void accept(A a, B b, C c, D d);
        }
    }
}
