package github.c2f4n.fluidcrafting.common.inventory.slot;

import github.c2f4n.fluidcrafting.api.IContentsListener;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

/** 机器侧的基础物品槽：谓词、上限、变化回调，容器槽只是它的展示层。 */
public class BasicInventorySlot {

    private final Predicate<ItemStack> validator;
    private final int limit;
    private final IContentsListener listener;
    private ItemStack stack = ItemStack.EMPTY;

    public BasicInventorySlot(IContentsListener listener, Predicate<ItemStack> validator, int limit) {
        this.listener = listener;
        this.validator = validator;
        this.limit = limit;
    }

    public static BasicInventorySlot at(IContentsListener listener, int limit) {
        return new BasicInventorySlot(listener, stack -> true, limit);
    }

    public ItemStack getStack() {
        return stack;
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public int getLimit() {
        return limit;
    }

    public boolean isItemValid(ItemStack stack) {
        return validator.test(stack);
    }

    public void setStack(ItemStack stack) {
        this.stack = stack;
        listener.onContentsChanged();
    }

    public void setStackUnchecked(ItemStack stack) {
        this.stack = stack;
    }

    /** @return 剩余未放入的部分 */
    public ItemStack insertItem(ItemStack insert, boolean simulate) {
        if (insert.isEmpty() || !isItemValid(insert)) {
            return insert;
        }
        return insertItemInternal(insert, simulate);
    }

    /** 机器内部的插入路径，跳过玩家/自动化校验。 */
    public ItemStack insertItemInternal(ItemStack insert, boolean simulate) {
        if (insert.isEmpty()) {
            return insert;
        }
        if (!stack.isEmpty()) {
            if (!ItemStack.isSameItemSameTags(stack, insert)) {
                return insert;
            }
            int space = Math.min(limit, stack.getMaxStackSize()) - stack.getCount();
            if (space <= 0) {
                return insert;
            }
            int toMove = Math.min(space, insert.getCount());
            if (!simulate) {
                stack.grow(toMove);
                listener.onContentsChanged();
            }
            ItemStack remainder = insert.copyWithCount(insert.getCount() - toMove);
            return remainder.isEmpty() ? ItemStack.EMPTY : remainder;
        }
        int toMove = Math.min(limit, Math.min(insert.getMaxStackSize(), insert.getCount()));
        if (!simulate) {
            stack = insert.copyWithCount(toMove);
            listener.onContentsChanged();
        }
        ItemStack remainder = insert.copyWithCount(insert.getCount() - toMove);
        return remainder.isEmpty() ? ItemStack.EMPTY : remainder;
    }

    public ItemStack extractItem(int amount, boolean simulate) {
        if (stack.isEmpty() || amount <= 0) {
            return ItemStack.EMPTY;
        }
        int toRemove = Math.min(amount, stack.getCount());
        ItemStack extracted = stack.copyWithCount(toRemove);
        if (!simulate) {
            stack = stack.copyWithCount(stack.getCount() - toRemove);
            if (stack.isEmpty()) {
                stack = ItemStack.EMPTY;
            }
            listener.onContentsChanged();
        }
        return extracted;
    }

    public void shrinkStack(int amount) {
        if (amount > 0 && !stack.isEmpty()) {
            stack = stack.copyWithCount(Math.max(0, stack.getCount() - amount));
            if (stack.isEmpty()) {
                stack = ItemStack.EMPTY;
            }
            listener.onContentsChanged();
        }
    }

    public void growStack(int amount) {
        if (amount > 0) {
            stack = stack.copyWithCount(Math.min(limit, stack.getCount() + amount));
            listener.onContentsChanged();
        }
    }
}
