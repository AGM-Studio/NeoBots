package xyz.agmstudio.neobots.robos;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class NeoBotCrash extends RuntimeException {
    private static final HashMap<Integer, NeoBotCrash> crashes = new HashMap<>();
    public static NeoBotCrash findById(int id) {
        return crashes.get(id);
    }

    public final Component message;
    public final int id;

    public NeoBotCrash(int id, @NotNull Component message) {
        this.id = id;
        this.message = message;
        crashes.put(id, this);
    }

    public static NeoBotCrash OUT_OF_CHARGE = new NeoBotCrash(-3, Component.empty());
    public static NeoBotCrash TARGET_INACCESSIBLE = new NeoBotCrash(0, Component.translatable("crash.create_neobots.target_inaccessible"));
    public static NeoBotCrash INVENTORY_INACCESSIBLE = new NeoBotCrash(1, Component.translatable("crash.create_neobots.inventory_inaccessible"));
    public static NeoBotCrash CHARGER_NOT_FOUND = new NeoBotCrash(2, Component.translatable("crash.create_neobots.not_on_charger"));
}