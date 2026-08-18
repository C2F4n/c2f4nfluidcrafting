package github.c2f4n.fluidcrafting.common.network.to_client;

import github.c2f4n.fluidcrafting.block.entity.BasicFluidMixerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** 方块实体完整客户端状态：流体罐 + 面配置/模式 + 是否正在合成。 */
public record PacketSyncMachine(BlockPos pos,
                                FluidStack input1, FluidStack input2, FluidStack input3, FluidStack output,
                                boolean fluidAutoEject, boolean itemAutoEject,
                                int redstoneMode, int miningMode,
                                int[] fluidModes, int[] itemModes,
                                boolean crafting) {

    public static void encode(PacketSyncMachine msg, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(msg.pos());
        buffer.writeFluidStack(msg.input1());
        buffer.writeFluidStack(msg.input2());
        buffer.writeFluidStack(msg.input3());
        buffer.writeFluidStack(msg.output());
        buffer.writeBoolean(msg.fluidAutoEject());
        buffer.writeBoolean(msg.itemAutoEject());
        buffer.writeVarInt(msg.redstoneMode());
        buffer.writeVarInt(msg.miningMode());
        for (int value : msg.fluidModes()) {
            buffer.writeVarInt(value);
        }
        for (int value : msg.itemModes()) {
            buffer.writeVarInt(value);
        }
        buffer.writeBoolean(msg.crafting());
    }

    public static PacketSyncMachine decode(FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        FluidStack input1 = buffer.readFluidStack();
        FluidStack input2 = buffer.readFluidStack();
        FluidStack input3 = buffer.readFluidStack();
        FluidStack output = buffer.readFluidStack();
        boolean fluidAutoEject = buffer.readBoolean();
        boolean itemAutoEject = buffer.readBoolean();
        int redstoneMode = buffer.readVarInt();
        int miningMode = buffer.readVarInt();
        int[] fluidModes = new int[6];
        int[] itemModes = new int[6];
        for (int i = 0; i < 6; i++) {
            fluidModes[i] = buffer.readVarInt();
        }
        for (int i = 0; i < 6; i++) {
            itemModes[i] = buffer.readVarInt();
        }
        boolean crafting = buffer.readBoolean();
        return new PacketSyncMachine(pos, input1, input2, input3, output,
              fluidAutoEject, itemAutoEject, redstoneMode, miningMode,
              fluidModes, itemModes, crafting);
    }

    public static void handle(PacketSyncMachine msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (Minecraft.getInstance().level != null
                  && Minecraft.getInstance().level.getBlockEntity(msg.pos()) instanceof BasicFluidMixerBlockEntity be) {
                be.applyClientTankData(msg.input1(), msg.input2(), msg.input3(), msg.output());
                be.applyClientSettings(msg.fluidAutoEject(), msg.itemAutoEject(),
                      msg.redstoneMode(), msg.miningMode(), msg.fluidModes(), msg.itemModes());
                be.applyClientWorking(msg.crafting());
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
