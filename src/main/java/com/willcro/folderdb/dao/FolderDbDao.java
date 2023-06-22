package com.willcro.folderdb.dao;

import com.willcro.folderdb.entity.FolderDbFile;
import com.willcro.folderdb.entity.FolderDbTable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.RowProcessor;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;

@Slf4j
public class FolderDbDao {

  private static final String GET_FILE_QUERY = "SELECT * FROM \"_folderdb_files\" WHERE filename = ?";
  private static final String DELETE_FILE_QUERY = "DELETE FROM \"_folderdb_files\" WHERE filename = ?";
  private static final String GET_TABLE_QUERY = "SELECT * FROM \"_folderdb_tables\" WHERE table_name = ?";
  private static final String GET_TABLES_QUERY = "SELECT * FROM \"_folderdb_tables\" WHERE filename = ?";
  private static final String DELETE_TABLES_QUERY = "DELETE FROM \"_folderdb_tables\" WHERE filename = ?";

  private static final RowProcessor PROCESSOR = new BasicRowProcessor(new MyBeanProcessor());

  public final Connection connection;
  private final QueryRunner q = new QueryRunner();

  public FolderDbDao(Connection connection) {
    this.connection = connection;
  }

  public Optional<FolderDbFile> getFile(String filename) throws SQLException {
    return Optional.ofNullable(
        q.query(connection, GET_FILE_QUERY, new BeanHandler<>(FolderDbFile.class, PROCESSOR),
            filename));
  }

  public Optional<FolderDbTable> getTable(String tableName) throws SQLException {
    return Optional.ofNullable(
        q.query(connection, GET_TABLE_QUERY, new BeanHandler<>(FolderDbTable.class, PROCESSOR),
            tableName));
  }

  public List<FolderDbTable> getTablesForFile(String filename) throws SQLException {
    return q.query(connection, GET_TABLES_QUERY,
        new BeanListHandler<>(FolderDbTable.class, PROCESSOR), filename);
  }

  public void deleteFile(String filename) throws SQLException {
    dropTablesForFile(filename);
    deleteTableRecords(filename);
    deleteFileRow(filename);
  }

  private void deleteFileRow(String filename) throws SQLException {
    q.update(connection, DELETE_FILE_QUERY, filename);
  }

  private void deleteTableRecords(String filename) throws SQLException {
    q.update(connection, DELETE_TABLES_QUERY, filename);
  }

  private void dropTablesForFile(String filename) throws SQLException {
    var tables = getTablesForFile(filename);
    for (FolderDbTable table : tables) {
      log.info("Deleting table {}", table.getTableName());
      q.update(connection, "DROP TABLE IF EXISTS \"" + table.getTableName() + "\"");
    }
  }

}
