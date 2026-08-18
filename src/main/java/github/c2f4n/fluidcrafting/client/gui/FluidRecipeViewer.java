package github.c2f4n.fluidcrafting.client.gui;

import net.minecraftforge.fluids.FluidStack;

/**
 * 流体槽“左键查看配方”的桥接点。
 * 核心 GUI 不依赖任何查看器，查看器插件（例如 JEI）在运行时注册实现；
 * 没有安装查看器时保持 no-op，不会影响原版倒液体/取液体逻辑。
 */
public final class FluidRecipeViewer {

    private static Opener opener = fluid -> {
    };

    public static void setOpener(Opener opener) {
        FluidRecipeViewer.opener = opener == null ? fluid -> {
        } : opener;
    }

    public static void clearOpener() {
        opener = fluid -> {
        };
    }

    public static void openRecipes(FluidStack fluid) {
        opener.open(fluid);
    }

    private FluidRecipeViewer() {
    }

    @FunctionalInterface
    public interface Opener {
        void open(FluidStack fluid);
    }
}
