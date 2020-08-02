package io.github.spleefx.converter;

import io.github.spleefx.SpleefX;
import io.github.spleefx.arena.api.ArenaData;
import org.moltenjson.configuration.direct.DirectConfiguration;
import org.moltenjson.configuration.tree.TreeConfiguration;
import org.moltenjson.json.JsonFile;

import java.io.File;
import java.util.StringJoiner;

public class SpectatorFileConverter implements Runnable {

    private final File dataFolder;

    public SpectatorFileConverter(File dataFolder) {
        this.dataFolder = dataFolder;
    }

    @Override public void run() {
        if (!dataFolder.exists()) return; // There are no files to convert
        convert(new File(dataFolder, "spectator-settings.json"));
    }

    private void convert(File file) {
        if (file.isFile()) {
            if (!TreeConfiguration.getExtension(file).equals("json"))
                return; // Ignored file
            DirectConfiguration d = DirectConfiguration.of(JsonFile.of(file));
            StringJoiner changed = new StringJoiner(" / ").setEmptyValue("");
            if (!d.contains("enabled")) {
                d.set("enabled", true);
                changed.add("Added 'enabled' setting to control whether is spectating enabled or not");
            }
            String ch = changed.toString().trim();
            if (!ch.isEmpty()) {
                d.save(Throwable::printStackTrace, ArenaData.GSON);
                SpleefX.logger().info("[SpectatorFileConverter] Successfully converted old extension " + file.getName() + " to the newer format. (Changes: " + ch.trim() + ")");
            }
        }
    }
}
