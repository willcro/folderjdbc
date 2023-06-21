package com.willcro.folderdb.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.dbutils.ColumnHandler;
import org.apache.commons.dbutils.GenerousBeanProcessor;
import org.apache.commons.dbutils.handlers.columns.BooleanColumnHandler;
import org.apache.commons.dbutils.handlers.columns.ByteColumnHandler;
import org.apache.commons.dbutils.handlers.columns.DoubleColumnHandler;
import org.apache.commons.dbutils.handlers.columns.FloatColumnHandler;
import org.apache.commons.dbutils.handlers.columns.IntegerColumnHandler;
import org.apache.commons.dbutils.handlers.columns.LongColumnHandler;
import org.apache.commons.dbutils.handlers.columns.SQLXMLColumnHandler;
import org.apache.commons.dbutils.handlers.columns.ShortColumnHandler;
import org.apache.commons.dbutils.handlers.columns.StringColumnHandler;
import org.apache.commons.dbutils.handlers.columns.TimestampColumnHandler;

public class MyBeanProcessor extends GenerousBeanProcessor {

  public MyBeanProcessor() {
    super();
  }

  // Workaround to deal with issues with the ServiceLoader, which is how these would normally be
  // brought in
  private final List<ColumnHandler> columnHandlers = Arrays.asList(
      new BooleanColumnHandler(),
      new ByteColumnHandler(),
      new DoubleColumnHandler(),
      new FloatColumnHandler(),
      new IntegerColumnHandler(),
      new LongColumnHandler(),
      new ShortColumnHandler(),
      new SQLXMLColumnHandler(),
      new StringColumnHandler(),
      new TimestampColumnHandler()
  );

  @Override
  protected Object processColumn(ResultSet rs, int index, Class<?> propType)
      throws SQLException {

    Object retval = rs.getObject(index);

    if ( !propType.isPrimitive() && retval == null ) {
      return null;
    }

    for (ColumnHandler handler : columnHandlers) {
      if (handler.match(propType)) {
        retval = handler.apply(rs, index);
        break;
      }
    }

    return retval;

  }

}
