package io.github.spleefx.util.message.message;

import io.github.spleefx.util.game.Chat;
import io.github.spleefx.util.message.CommentedConfiguration;
import org.bukkit.ChatColor;
import org.bukkit.util.ChatPaginator;

/**
 * MessageBuilder class for {@link Message}
 */
public class MessageBuilder {

    private static final int DESCRIPTION_LENGTH = 37;

    private final String key;
    private String defaultValue;
    private String comment;
    private String[] description;

    public MessageBuilder(String key) {
        this.key = key;
    }

    public MessageBuilder defaultTo(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public MessageBuilder describe(String description) {
        description = Chat.colorize(description);
        comment = CommentedConfiguration.sanitize(key, new String[]{ChatColor.stripColor(description)});
        this.description = ChatPaginator.wordWrap(description, DESCRIPTION_LENGTH);
        return this;
    }

    public Message build() {
        return new Message(key, defaultValue, comment, description);
    }

}
