package github.C2F4n.common.network.to_client;

import github.C2F4n.common.inventory.container.ModContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

/** 类型化容器同步：把发生变化的 tracked data 批量发给客户端。 */
public class PacketUpdateContainer {

    private final short containerId;
    private final List<PropertyData> data;

    public PacketUpdateContainer(short containerId, List<PropertyData> data) {
        this.containerId = containerId;
        this.data = data;
    }

    public static void encode(PacketUpdateContainer msg, FriendlyByteBuf buffer) {
        buffer.writeShort(msg.containerId);
        buffer.writeVarInt(msg.data.size());
        for (PropertyData data : msg.data) {
            buffer.writeShort(data.getIndex());
            buffer.writeEnum(PropertyType.of(data));
            data.write(buffer);
        }
    }

    public static PacketUpdateContainer decode(FriendlyByteBuf buffer) {
        short containerId = buffer.readShort();
        int count = buffer.readVarInt();
        java.util.ArrayList<PropertyData> data = new java.util.ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            short index = buffer.readShort();
            data.add(PropertyType.read(buffer.readEnum(PropertyType.class), index, buffer));
        }
        return new PacketUpdateContainer(containerId, data);
    }

    public static void handle(PacketUpdateContainer msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (Minecraft.getInstance().player == null) {
                return;
            }
            AbstractContainerMenu menu = Minecraft.getInstance().player.containerMenu;
            if (menu == null || menu.containerId != msg.containerId || !(menu instanceof ModContainer container)) {
                return;
            }
            for (PropertyData data : msg.data) {
                if (data instanceof BooleanPropertyData p) {
                    container.handleWindowProperty(p.getIndex(), p.value);
                } else if (data instanceof IntPropertyData p) {
                    container.handleWindowProperty(p.getIndex(), p.value);
                } else if (data instanceof FluidStackPropertyData p) {
                    container.handleWindowProperty(p.getIndex(), p.value);
                } else if (data instanceof ItemStackPropertyData p) {
                    container.handleWindowProperty(p.getIndex(), p.value);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public enum PropertyType {
        BOOLEAN,
        INT,
        FLUID_STACK,
        ITEM_STACK;

        public static PropertyType of(PropertyData data) {
            if (data instanceof BooleanPropertyData) {
                return BOOLEAN;
            } else if (data instanceof IntPropertyData) {
                return INT;
            } else if (data instanceof FluidStackPropertyData) {
                return FLUID_STACK;
            }
            return ITEM_STACK;
        }

        public static PropertyData read(PropertyType type, short index, FriendlyByteBuf buffer) {
            return switch (type) {
                case BOOLEAN -> BooleanPropertyData.read(index, buffer);
                case INT -> IntPropertyData.read(index, buffer);
                case FLUID_STACK -> FluidStackPropertyData.read(index, buffer);
                case ITEM_STACK -> ItemStackPropertyData.read(index, buffer);
            };
        }
    }

    public abstract static class PropertyData {

        private final short index;

        public PropertyData(short index) {
            this.index = index;
        }

        public short getIndex() {
            return index;
        }

        public abstract void write(FriendlyByteBuf buffer);
    }

    public static class BooleanPropertyData extends PropertyData {

        public final boolean value;

        public BooleanPropertyData(short index, boolean value) {
            super(index);
            this.value = value;
        }

        public static BooleanPropertyData read(short index, FriendlyByteBuf buffer) {
            return new BooleanPropertyData(index, buffer.readBoolean());
        }

        @Override
        public void write(FriendlyByteBuf buffer) {
            buffer.writeBoolean(value);
        }
    }

    public static class IntPropertyData extends PropertyData {

        public final int value;

        public IntPropertyData(short index, int value) {
            super(index);
            this.value = value;
        }

        public static IntPropertyData read(short index, FriendlyByteBuf buffer) {
            return new IntPropertyData(index, buffer.readVarInt());
        }

        @Override
        public void write(FriendlyByteBuf buffer) {
            buffer.writeVarInt(value);
        }
    }

    public static class FluidStackPropertyData extends PropertyData {

        public final FluidStack value;

        public FluidStackPropertyData(short index, FluidStack value) {
            super(index);
            this.value = value;
        }

        public static FluidStackPropertyData read(short index, FriendlyByteBuf buffer) {
            return new FluidStackPropertyData(index, buffer.readFluidStack());
        }

        @Override
        public void write(FriendlyByteBuf buffer) {
            buffer.writeFluidStack(value);
        }
    }

    public static class ItemStackPropertyData extends PropertyData {

        public final ItemStack value;

        public ItemStackPropertyData(short index, ItemStack value) {
            super(index);
            this.value = value;
        }

        public static ItemStackPropertyData read(short index, FriendlyByteBuf buffer) {
            return new ItemStackPropertyData(index, buffer.readItem());
        }

        @Override
        public void write(FriendlyByteBuf buffer) {
            buffer.writeItem(value);
        }
    }
}
