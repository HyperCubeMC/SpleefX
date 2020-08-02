package io.github.spleefx.data.database.sql;

import io.github.spleefx.SpleefX;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@AllArgsConstructor
public enum StatementKey {

    CREATE_TABLE,

    SELECT_PLAYER,
    UPSERT_PLAYER,
    DELETE_PLAYER,

    BULK_SELECT_PLAYERS("select all");

    /**
     * The statement section name. Must match with the lowercase + space-instead-of-underscores name in the schema
     * file.
     */
    private final String name;

    /**
     * Creates a statement and fetches the name by following the default convention.
     */
    StatementKey() {
        name = name().toLowerCase().replace("_", " ");
    }

    /**
     * Parses the specified schema file (must be in /schemas/[name].sql) and returns a map of keys and statements
     * to use for that database type.
     *
     * @param name Name of the file
     * @return A map with keys and statements from the schema
     */
    @NotNull
    public static Map<StatementKey, String> parseSchema(String name) {
        InputStream schemaStream = SpleefX.getPlugin().getResource("schema/" + name + ".sql");
        Objects.requireNonNull(schemaStream, "Schema not found for " + name + ". That shouldn't happen.");
        return parseSchema(schemaStream);
    }

    /**
     * Parses the specified schema file (must be in /schemas/[name].sql) and returns a map of keys and statements
     * to use for that database type.
     *
     * @param schemaStream The InputStream to read from
     * @return A map with keys and statements from the schema
     */
    @NotNull
    public static Map<StatementKey, String> parseSchema(InputStream schemaStream) {
        Map<StatementKey, String> sections = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(schemaStream, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();

            String currentSection = "create table";
            String currentStatement;

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) continue;
                if (line.startsWith("--: ")) {
                    currentSection = line.substring(3);
                    continue;
                }

                sb.append(" ").append(line);

                // check for end of declaration
                if (line.endsWith(";")) {
                    sb.deleteCharAt(sb.length() - 1);

                    currentStatement = sb.toString();
                    sections.put(Objects.requireNonNull(BY_NAME.get(currentSection.trim()), "Invalid statement key: " + currentSection),
                            StringUtils.normalizeSpace(currentStatement.trim()));

                    // reset
                    sb = new StringBuilder();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sections;
    }

    private static final Map<String, StatementKey> BY_NAME = new HashMap<>();

    static {
        for (StatementKey key : values()) BY_NAME.put(key.name, key);
    }
}