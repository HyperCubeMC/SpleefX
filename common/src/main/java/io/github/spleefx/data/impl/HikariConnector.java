package io.github.spleefx.data.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.spleefx.SpleefX;
import io.github.spleefx.config.SpleefXConfig;
import io.github.spleefx.data.database.sql.StatementKey;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple utility class to allow easy establishing of connection using HikariCP.
 */
public abstract class HikariConnector {

    /**
     * A cache of all schemas
     */
    private static final Map<String, Map<StatementKey, String>> SCHEMAS = new HashMap<>();

    /**
     * The constant instance of {@link Connection}.
     */
    protected Connection connection;

    /**
     * The schemas for this database type
     */
    protected final Map<StatementKey, String> schemas;

    /**
     * The JDBC name
     */
    protected final String jdbcName;

    /**
     * The JDBC driver class name
     */
    protected final String driverClass;

    /**
     * Creates a new HikariCP-based connection
     *
     * @param name The database type name which will be used to handle database-type-specific queries.
     */
    public HikariConnector(@NotNull String name, @NotNull String driverClass) {
        schemas = SCHEMAS.computeIfAbsent(name, StatementKey::parseSchema);
        jdbcName = name;
        this.driverClass = driverClass;
    }

    protected void setProperties(HikariConfig config) {
    }

    protected void preConnect() {
    }

    protected String createJdbcURL() {
        return String.format("jdbc:" + jdbcName + "://%s/%s", SpleefXConfig.DB_HOST.get(), SpleefXConfig.DB_NAME.get());
    }

    protected void setCredentials(HikariConfig config) {
        config.setUsername(SpleefXConfig.DB_USER.get());
        config.setPassword(SpleefXConfig.DB_PASSWORD.get());
    }

    /**
     * Connects to the database and sets the {@link Connection} instance.
     */
    public void connect() {
        preConnect();

        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(createJdbcURL());
        setCredentials(config);

        config.setPoolName("SpleefX-Pool");
        config.setConnectionTestQuery("SELECT 1");
        config.setMaxLifetime(60000); // 60 seconds
        config.setMaximumPoolSize(50); // 50 connections (including idle connections)

        // statement cache
        config.addDataSourceProperty("cachePrepStmts", true);
        config.addDataSourceProperty("prepStmtCacheSize", 250);
        config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);

        config.setDriverClassName(driverClass);

        // other settings which i absolutely have no idea what they do but they're on HikariCP's wiki
        // for best performance soo
        config.addDataSourceProperty("useServerPrepStmts", true);
        config.addDataSourceProperty("useLocalSessionState", true);
        config.addDataSourceProperty("rewriteBatchedStatements", true);
        config.addDataSourceProperty("cacheResultSetMetadata", true);
        config.addDataSourceProperty("cacheServerConfiguration", true);
        config.addDataSourceProperty("elideSetAutoCommits", true);
        config.addDataSourceProperty("maintainTimeStats", false);

        setProperties(config);

        HikariDataSource dataSource = new HikariDataSource(config);
        try {
            connection = dataSource.getConnection();
            execute(StatementKey.CREATE_TABLE);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Prepares a statement from the schema
     *
     * @param statement Statement to create for
     * @return The prepared statement
     * @throws SQLException {@link Connection#prepareStatement(String)}.
     */
    protected PreparedStatement prepare(@NotNull StatementKey statement, Object... parameters) throws SQLException {
        PreparedStatement prep = connection.prepareStatement(schemas.get(statement));
        for (int i = 1; i <= parameters.length; i++) {
            prep.setObject(i, parameters[i - 1]);
        }
        return prep;
    }

    /**
     * Executes the specified query
     *
     * @param statementKey to execute
     */
    protected void execute(@NotNull StatementKey statementKey) {
        String query = schemas.get(statementKey);
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(query);
        } catch (SQLException e) {
            SpleefX.logger().severe("Cannot run query " + query + ".");
            e.printStackTrace();
        }
    }

    /**
     * Executes the specified query
     *
     * @param statementKey to execute
     */
    protected ResultSet executeQuery(@NotNull StatementKey statementKey) {
        String query = schemas.get(statementKey);
        try {
            Statement statement = connection.createStatement();
            return statement.executeQuery(query);
        } catch (SQLException e) {
            SpleefX.logger().severe("Cannot run query " + query + ".");
            throw new IllegalStateException(e);
        }
    }
}