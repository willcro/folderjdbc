package com.willcro.folderdb.jdbc;

import com.willcro.folderdb.files.DatabaseBuilder;
import com.willcro.folderdb.utils.SqliteUtils;
import java.io.IOException;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.ShardingKey;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.regex.Pattern;
import lombok.SneakyThrows;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;

public class FolderDbConnection implements Connection {

  public final Connection delegate;
  private final DatabaseBuilder databaseBuilder;

  public FolderDbConnection(Connection delegate, DatabaseBuilder databaseBuilder) {
    this.delegate = delegate;
    this.databaseBuilder = databaseBuilder;
  }

  @SneakyThrows
  private void lazyLoadForQuery(String sql) {
    var tables = SqliteUtils.getTablesFromQuery(sql);
    for (String table : tables) {
      try {
        databaseBuilder.loadTableData(table);
      } catch (Exception e) {
        throw new SQLException(e);
      }
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

  public boolean getAutoCommit() throws SQLException {
    return delegate.getAutoCommit();
  }

  public void setAutoCommit(boolean autoCommit) throws SQLException {
    delegate.setAutoCommit(autoCommit);
  }

  public void commit() throws SQLException {
    delegate.commit();
  }

  public void rollback() throws SQLException {
    delegate.rollback();
  }

  public void close() throws SQLException {
    try {
      databaseBuilder.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    delegate.close();
  }

  public boolean isClosed() throws SQLException {
    return delegate.isClosed();
  }

  public DatabaseMetaData getMetaData() throws SQLException {
    return delegate.getMetaData();
  }

  public boolean isReadOnly() throws SQLException {
    return delegate.isReadOnly();
  }

  public void setReadOnly(boolean readOnly) throws SQLException {
    delegate.setReadOnly(readOnly);
  }

  public String getCatalog() throws SQLException {
    return delegate.getCatalog();
  }

  public void setCatalog(String catalog) throws SQLException {
    delegate.setCatalog(catalog);
  }

  public int getTransactionIsolation() throws SQLException {
    return delegate.getTransactionIsolation();
  }

  public void setTransactionIsolation(int level) throws SQLException {
    delegate.setTransactionIsolation(level);
  }

  public SQLWarning getWarnings() throws SQLException {
    return delegate.getWarnings();
  }

  public void clearWarnings() throws SQLException {
    delegate.clearWarnings();
  }

  public Statement createStatement(int resultSetType, int resultSetConcurrency)
      throws SQLException {
    return new StatementWrapper(delegate.createStatement(resultSetType, resultSetConcurrency),
        this::lazyLoadForQuery);
  }

  public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
      throws SQLException {
    lazyLoadForQuery(sql);
    return delegate.prepareStatement(sql, resultSetType, resultSetConcurrency);
  }

  public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency)
      throws SQLException {
    lazyLoadForQuery(sql);
    return delegate.prepareCall(sql, resultSetType, resultSetConcurrency);
  }

  public Map<String, Class<?>> getTypeMap() throws SQLException {
    return delegate.getTypeMap();
  }

  public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
    delegate.setTypeMap(map);
  }

  public int getHoldability() throws SQLException {
    return delegate.getHoldability();
  }

  public void setHoldability(int holdability) throws SQLException {
    delegate.setHoldability(holdability);
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

  public Statement createStatement(int resultSetType, int resultSetConcurrency,
      int resultSetHoldability) throws SQLException {
    return new StatementWrapper(
        delegate.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability),
        this::lazyLoadForQuery);
  }

  public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
      int resultSetHoldability) throws SQLException {
    lazyLoadForQuery(sql);
    return delegate.prepareStatement(sql, resultSetType, resultSetConcurrency,
        resultSetHoldability);
  }

  public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
      int resultSetHoldability) throws SQLException {
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

  public String getClientInfo(String name) throws SQLException {
    return delegate.getClientInfo(name);
  }

  public Properties getClientInfo() throws SQLException {
    return delegate.getClientInfo();
  }

  public void setClientInfo(Properties properties) throws SQLClientInfoException {
    delegate.setClientInfo(properties);
  }

  public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
    return delegate.createArrayOf(typeName, elements);
  }

  public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
    return delegate.createStruct(typeName, attributes);
  }

  public String getSchema() throws SQLException {
    return delegate.getSchema();
  }

  public void setSchema(String schema) throws SQLException {
    delegate.setSchema(schema);
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

  public boolean setShardingKeyIfValid(ShardingKey shardingKey, ShardingKey superShardingKey,
      int timeout) throws SQLException {
    return delegate.setShardingKeyIfValid(shardingKey, superShardingKey, timeout);
  }

  public boolean setShardingKeyIfValid(ShardingKey shardingKey, int timeout) throws SQLException {
    return delegate.setShardingKeyIfValid(shardingKey, timeout);
  }

  public void setShardingKey(ShardingKey shardingKey, ShardingKey superShardingKey)
      throws SQLException {
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
