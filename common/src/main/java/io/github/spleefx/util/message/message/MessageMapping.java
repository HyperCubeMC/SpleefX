package io.github.spleefx.util.message.message;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation added to messages to allow them to be automatically imported by the {@link MessageImporter}
 * from their old messages.json.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MessageMapping {

    /**
     * The legacy mapping value
     *
     * @return The old mapping
     */
    String value();

}
