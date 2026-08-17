package github.C2F4n.client.gui.util;

import net.minecraft.client.gui.Font;

/** 超长单行文字的左右循环滚动状态机。 */
public class ScrollingText {

    private static final int START_PAUSE = 30;
    private static final int END_PAUSE = 20;
    private static final int START = 0;
    private static final int FORWARD = 1;
    private static final int END = 2;
    private static final int BACKWARD = 3;

    private int phase = START;
    private int phaseTicks;
    private int offset;
    private int maxOffset;

    public void prepare(Font font, String text, int maxWidth) {
        maxOffset = Math.max(0, font.width(text) - maxWidth);
    }

    public void tick(Font font, String text, int maxWidth) {
        prepare(font, text, maxWidth);
        if (maxOffset <= 0) {
            phase = START;
            phaseTicks = 0;
            offset = 0;
            return;
        }
        switch (phase) {
            case START -> {
                phaseTicks++;
                if (phaseTicks >= START_PAUSE) {
                    phase = FORWARD;
                    phaseTicks = 0;
                }
            }
            case FORWARD -> {
                offset = Math.min(maxOffset, offset + 1);
                if (offset >= maxOffset) {
                    phase = END;
                    phaseTicks = 0;
                }
            }
            case END -> {
                phaseTicks++;
                if (phaseTicks >= END_PAUSE) {
                    phase = BACKWARD;
                    phaseTicks = 0;
                }
            }
            case BACKWARD -> {
                offset = Math.max(0, offset - 1);
                if (offset == 0) {
                    phase = START;
                    phaseTicks = 0;
                }
            }
            default -> {
                phase = START;
                offset = 0;
            }
        }
    }

    public int getOffset() {
        return offset;
    }
}
