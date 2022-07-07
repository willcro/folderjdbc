package com.willcro.folderdb.sql;

public enum ColumnTypeEnum {

    INTEGER("INTEGER", Integer.class),
    TEXT("TEXT", String.class),
    BLOB("BLOB", byte[].class),
    REAL("REAL", Double.class),
    NUMERIC("NUMERIC", Number.class)
    ;

    private final String sqliteType;
    private Class javaClass;

    ColumnTypeEnum(String sqliteType, Class javaClass) {
        this.sqliteType = sqliteType;
        this.javaClass = javaClass;
    }

    public String getSqliteType() {
        return sqliteType;
    }

    public Class getJavaClass() {
        return javaClass;
    }
}
