package xyz.agmstudio.neobots.index;

import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.data.loading.DatagenModLoader;
import xyz.agmstudio.neobots.NeoBots;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static xyz.agmstudio.neobots.NeoBots.REGISTRATE;

public final class CNBLang {
    public static void register() {
        if (!DatagenModLoader.isRunningDataGen()) return;
        try (InputStream stream = NeoBots.class.getClassLoader().getResourceAsStream("assets/" + NeoBots.MOD_ID + "/lang/en_us.manual.json")) {
            if (stream == null) return;

            JsonObject json = GsonHelper.parse(new InputStreamReader(stream)).getAsJsonObject();
            json.entrySet().forEach(entry ->
                    REGISTRATE.addRawLang(entry.getKey(), entry.getValue().getAsString())
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}