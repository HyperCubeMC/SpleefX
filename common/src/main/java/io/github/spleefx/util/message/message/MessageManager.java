package io.github.spleefx.util.message.message;

import io.github.spleefx.SpleefX;
import io.github.spleefx.util.message.CommentedConfiguration;
import org.bukkit.ChatColor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for managing all messages
 */
public class MessageManager {

    private final List<Message> messages = new ArrayList<>();
    private final File messagesFile;
    private CommentedConfiguration config;

    public MessageManager(SpleefX plugin) {
        messagesFile = plugin.getFileManager().createFile("messages.yml");
        config = CommentedConfiguration.loadConfiguration(messagesFile);
    }

    /**
     * Registers the specified message.
     *
     * @param message Message to register
     */
    public void registerMessage(Message message) {
        messages.add(message);
    }

    /**
     * Loads all messages
     */
    public void load(boolean reload) {
        if (!reload) {
            Message.load(); // a simple invokation to load all messages and force-register them.
            save(); // add any missing messages
        } else
            config = CommentedConfiguration.loadConfiguration(messagesFile);
        for (Message message : messages) {
            message.setValue(config.getString(message.getKey(), message.getDefaultValue()));
        }
    }

    /**
     * Saves all messages
     */
    public void save() {
        try {
            for (Message message : messages) {
                config.set(message.getKey(), message.getValue().replace(ChatColor.COLOR_CHAR, '&'));
                config.setComment(message.getKey(), message.getComment());
            }
            config.save(messagesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
