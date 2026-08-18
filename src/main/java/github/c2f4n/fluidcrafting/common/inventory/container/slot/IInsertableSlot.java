package github.c2f4n.fluidcrafting.common.inventory.container.slot;

/** 容器槽的窗口感知能力：窗口未打开时对应的虚拟槽“不存在”。 */
public interface IInsertableSlot {

    boolean exists(SelectedWindowData window);
}
