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
    public static NeoBotCrash INVENTORY_NOT_ACCESSIBLE = new NeoBotCrash(0, Component.translatable("crash.neobots.inventory_not_accessible"));
}