package com.willcro.folderdb.jdbc;

import com.willcro.folderdb.files.DatabaseBuilder;
import org.sqlite.JDBC;

import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

public class FolderDbDriver implements Driver {

    private final Driver delegate;

    public FolderDbDriver() {
        this.delegate = new JDBC();
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        var dir = url.substring(14);
        var builder = new DatabaseBuilder(dir);
        var sqliteConnection = builder.build();
        return new FolderDbConnection(sqliteConnection, builder);
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        return isValidURL(url);
    }

    public static boolean isValidURL(String url) {
        return url != null && url.toLowerCase().startsWith("jdbc:folderdb:");
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return delegate.getPropertyInfo(url, info);
    }

    @Override
    public int getMajorVersion() {
        return delegate.getMajorVersion();
    }

    @Override
    public int getMinorVersion() {
        return delegate.getMinorVersion();
    }

    @Override
    public boolean jdbcCompliant() {
        return delegate.jdbcCompliant();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return delegate.getParentLogger();
    }
}
