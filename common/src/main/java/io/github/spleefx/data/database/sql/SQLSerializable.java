package io.github.spleefx.data.database.sql;

import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

/**
 * A simple adapter class to allow secure serializing of objects and inserting into {@link PreparedStatement}s
 * without having to worry about injections.
 */
public interface SQLSerializable {

    /**
     * Returns the placeholders form of this object, such as '(?, ?, ?)'
     *
     * @return The placeholders
     */
    @NotNull
    String asPlaceholders();

    /**
     * Passes all placeholders to the statement
     *
     * @param uuid      UUID of the player
     * @param statement Statement to pass to
     * @param index     Current index
     * @return The finishing index, where the next statemetn will start
     * @throws SQLException Any SQL exception
     */
    int passToStatement(@NotNull UUID uuid, @NotNull PreparedStatement statement, final int index) throws SQLException;

}