package com.willcro.folderdb.jdbc;

import com.willcro.folderdb.files.DatabaseBuilder;
import lombok.SneakyThrows;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;

import java.sql.*;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.regex.Pattern;

public class FolderDbConnection implements Connection {

    public final Connection delegate;
    private final DatabaseBuilder databaseBuilder;

    public FolderDbConnection(Connection delegate, DatabaseBuilder databaseBuilder) {
        this.delegate = delegate;
        this.databaseBuilder = databaseBuilder;
    }

    @SneakyThrows
    private void lazyLoadForQuery(String sql) {
        var details = new QueryRunner().query(delegate, "explain query plan " + sql, new MapListHandler());
        details.stream()
                .map(map -> (String) map.get("detail"))
                .map(this::detailToTable)
                .filter(Objects::nonNull)
                .distinct()
                .forEach(table -> {
                    try {
                        databaseBuilder.loadTableDate(table);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
    }

    private static final Pattern pattern = Pattern.compile("^(SCAN|SEARCH|USING ROWID SEARCH ON TABLE) (.*?)($| .*$)");

    private String detailToTable(String detail) {
        var matcher = pattern.matcher(detail);
        if (matcher.matches()) {
            return matcher.group(2);
        } else {
            return null;
        }
    }

    public Statement createStatement() throws SQLException {
        return new StatementWrapper(delegate.createStatement(), this::lazyLoadForQuery);
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        lazyLoadForQuery(sql);
        return delegate.prepareStatement(sql);
    }

    public CallableStatement prepareCall(String sql) throws SQLException {
        lazyLoadForQuery(sql);
        return delegate.prepareCall(sql);
    }

    public String nativeSQL(String sql) throws SQLException {
        return delegate.nativeSQL(sql);
    }

    public void setAutoCommit(boolean autoCommit) throws SQLException {
        delegate.setAutoCommit(autoCommit);
    }

    public boolean getAutoCommit() throws SQLException {
        return delegate.getAutoCommit();
    }

    public void commit() throws SQLException {
        delegate.commit();
    }

    public void rollback() throws SQLException {
        delegate.rollback();
    }

    public void close() throws SQLException {
        delegate.close();
    }

    public boolean isClosed() throws SQLException {
        return delegate.isClosed();
    }

    public DatabaseMetaData getMetaData() throws SQLException {
        return delegate.getMetaData();
    }

    public void setReadOnly(boolean readOnly) throws SQLException {
        delegate.setReadOnly(readOnly);
    }

    public boolean isReadOnly() throws SQLException {
        return delegate.isReadOnly();
    }

    public void setCatalog(String catalog) throws SQLException {
        delegate.setCatalog(catalog);
    }

    public String getCatalog() throws SQLException {
        return delegate.getCatalog();
    }

    public void setTransactionIsolation(int level) throws SQLException {
        delegate.setTransactionIsolation(level);
    }

    public int getTransactionIsolation() throws SQLException {
        return delegate.getTransactionIsolation();
    }

    public SQLWarning getWarnings() throws SQLException {
        return delegate.getWarnings();
    }

    public void clearWarnings() throws SQLException {
        delegate.clearWarnings();
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        return new StatementWrapper(delegate.createStatement(resultSetType, resultSetConcurrency), this::lazyLoadForQuery);
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        lazyLoadForQuery(sql);
        return delegate.prepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        lazyLoadForQuery(sql);
        return delegate.prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return delegate.getTypeMap();
    }

    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        delegate.setTypeMap(map);
    }

    public void setHoldability(int holdability) throws SQLException {
        delegate.setHoldability(holdability);
    }

    public int getHoldability() throws SQLException {
        return delegate.getHoldability();
    }

    public Savepoint setSavepoint() throws SQLException {
        return delegate.setSavepoint();
    }

    public Savepoint setSavepoint(String name) throws SQLException {
        return delegate.setSavepoint(name);
    }

    public void rollback(Savepoint savepoint) throws SQLException {
        delegate.rollback(savepoint);
    }

    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        delegate.releaseSavepoint(savepoint);
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return new StatementWrapper(delegate.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability), this::lazyLoadForQuery);
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        lazyLoadForQuery(sql);
        return delegate.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        lazyLoadForQuery(sql);
        return delegate.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        lazyLoadForQuery(sql);
        return delegate.prepareStatement(sql, autoGeneratedKeys);
    }

    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        lazyLoadForQuery(sql);
        return delegate.prepareStatement(sql, columnIndexes);
    }

    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        lazyLoadForQuery(sql);
        return delegate.prepareStatement(sql, columnNames);
    }

    public Clob createClob() throws SQLException {
        return delegate.createClob();
    }

    public Blob createBlob() throws SQLException {
        return delegate.createBlob();
    }

    public NClob createNClob() throws SQLException {
        return delegate.createNClob();
    }

    public SQLXML createSQLXML() throws SQLException {
        return delegate.createSQLXML();
    }

    public boolean isValid(int timeout) throws SQLException {
        return delegate.isValid(timeout);
    }

    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        delegate.setClientInfo(name, value);
    }

    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        delegate.setClientInfo(properties);
    }

    public String getClientInfo(String name) throws SQLException {
        return delegate.getClientInfo(name);
    }

    public Properties getClientInfo() throws SQLException {
        return delegate.getClientInfo();
    }

    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        return delegate.createArrayOf(typeName, elements);
    }

    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        return delegate.createStruct(typeName, attributes);
    }

    public void setSchema(String schema) throws SQLException {
        delegate.setSchema(schema);
    }

    public String getSchema() throws SQLException {
        return delegate.getSchema();
    }

    public void abort(Executor executor) throws SQLException {
        delegate.abort(executor);
    }

    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        delegate.setNetworkTimeout(executor, milliseconds);
    }

    public int getNetworkTimeout() throws SQLException {
        return delegate.getNetworkTimeout();
    }

    public void beginRequest() throws SQLException {
        delegate.beginRequest();
    }

    public void endRequest() throws SQLException {
        delegate.endRequest();
    }

    public boolean setShardingKeyIfValid(ShardingKey shardingKey, ShardingKey superShardingKey, int timeout) throws SQLException {
        return delegate.setShardingKeyIfValid(shardingKey, superShardingKey, timeout);
    }

    public boolean setShardingKeyIfValid(ShardingKey shardingKey, int timeout) throws SQLException {
        return delegate.setShardingKeyIfValid(shardingKey, timeout);
    }

    public void setShardingKey(ShardingKey shardingKey, ShardingKey superShardingKey) throws SQLException {
        delegate.setShardingKey(shardingKey, superShardingKey);
    }

    public void setShardingKey(ShardingKey shardingKey) throws SQLException {
        delegate.setShardingKey(shardingKey);
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        return delegate.unwrap(iface);
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }
}
