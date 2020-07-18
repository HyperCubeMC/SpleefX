package io.github.spleefx.util.message.message;

import com.google.gson.reflect.TypeToken;
import io.github.spleefx.SpleefX;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.moltenjson.configuration.direct.DirectConfiguration;
import org.moltenjson.json.JsonFile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class MessageImporter {

    public MessageImporter(SpleefX spleefX) {
        File messagesJSON = new File(spleefX.getDataFolder(), "messages.json");
        if (!messagesJSON.exists()) return;
        DirectConfiguration config = DirectConfiguration.of(JsonFile.of(messagesJSON));
        for (Entry<Message, String> m : Message.getMappings().entrySet()) {
            if (m.getValue().equals("prefix")) {
                m.getKey().setValue(config.getString("globalPrefix").replace(ChatColor.COLOR_CHAR, '&'));
                continue;
            }
            String[] paths = StringUtils.split(m.getValue(), ".", 2);
            Map<String, String> map = config.get(paths[0], new TypeToken<HashMap<String, String>>() {
            }.getType());
            m.getKey().setValue(map.get(paths[1]).replace(ChatColor.COLOR_CHAR, '&').replace("{countdown}", "{colored_number}"));
        }
        spleefX.getLogger().info("Successfully imported old messages from messages.json");
        spleefX.getMessageManager().save();
        messagesJSON.delete();
    }
}
