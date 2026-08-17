package github.C2F4n.common.upgrade;

import org.jetbrains.annotations.Nullable;

/** 升级类别。 */
public enum UpgradeKind {
    SPEED("speed"),
    CREATIVE("creative");

    private final String name;

    UpgradeKind(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public static UpgradeKind fromName(String name) {
        for (UpgradeKind kind : values()) {
            if (kind.name.equals(name)) {
                return kind;
            }
        }
        return null;
    }
}
