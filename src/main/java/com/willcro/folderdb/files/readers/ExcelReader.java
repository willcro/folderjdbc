package com.willcro.folderdb.files.readers;

import com.willcro.folderdb.config.FileConfiguration;
import com.willcro.folderdb.exception.ConfigurationException;
import com.willcro.folderdb.exception.FileProcessingException;
import com.willcro.folderdb.exception.FolderDbException;
import com.willcro.folderdb.sql.Table;
import com.willcro.folderdb.sql.TableV2;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFTable;
import org.apache.poi.xssf.usermodel.XSSFTableColumn;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Reads excel (xlsx) files.
 *
 * Supports two different types of tables: - Normal sheets - Excel tables
 * (https://support.microsoft.com/en-us/office/overview-of-excel-tables-7ab0bb7d-3a9e-4b56-a3c9-6c94334e492c)
 *
 * WARNING: this reader currently does not support streaming, so large files may cause issues
 */
public class ExcelReader extends BaseReader {

  private static final List<String> EXCEL_COLUMNS = Arrays.asList("A", "B", "C", "D", "E", "F", "G",
      "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y",
      "Z");

  @Override
  public String getId() {
    return "excel";
  }

  @Override
  public List<TableV2> readFile(File file, FileConfiguration config) throws FolderDbException {
    XSSFWorkbook workbook = getWorkbook(file);

    var tables = new ArrayList<TableV2>();
    var sheetCount = workbook.getNumberOfSheets();
    for (int i = 0; i < sheetCount; i++) {
      var sheet = workbook.getSheetAt(i);
      tables.add(createSheetTable(sheet));
      var tableTables = sheet.getTables().stream()
          .map(this::createTableTable)
          .collect(Collectors.toList());
      tables.addAll(tableTables);
    }

    return tables;
  }

  @Override
  public Stream<List<String>> getData(File file, TableV2 table, FileConfiguration configuration)
      throws FolderDbException {
    XSSFWorkbook workbook = getWorkbook(file);

    var type = (String) table.getMetadata().get("type");
    if ("SHEET".equals(type)) {
      var sheet = workbook.getSheet(table.getSubName());
      return createSheetTableData(sheet);
    } else if ("TABLE".equals(type)) {
      var excelTable = workbook.getTable(table.getSubName());
      return createTableTableData(excelTable);
    } else {
      throw new FolderDbException("Table had invalid type " + type);
    }
  }

  private XSSFWorkbook getWorkbook(File file) throws FileProcessingException {
    XSSFWorkbook workbook;
    try (FileInputStream fip = new FileInputStream(file)) {
      workbook = new XSSFWorkbook(fip);
    } catch (IOException e) {
      throw new FileProcessingException(file.getName(), e);
    }
    return workbook;
  }

  public TableV2 createSheetTable(XSSFSheet sheet) {
    var lastColumn = getLastColumn(sheet);

    var columns = IntStream.range(0, lastColumn).boxed()
        .map(this::getColumnName)
        .collect(Collectors.toList());

    return TableV2.builder()
        .subName(sheet.getSheetName())
        .columns(columns)
        .metadata(Map.of("type", "SHEET"))
        .build();
  }

  public Stream<List<String>> createSheetTableData(XSSFSheet sheet) {
    var lastRow = sheet.getLastRowNum();
    var lastColumn = getLastColumn(sheet);

    List<List<String>> rows = new ArrayList<>();

    for (int i = 0; i <= lastRow; i++) {
      var row = sheet.getRow(i);
      var data = new ArrayList<String>();
      for (int j = 0; j < lastColumn; j++) {
        if (row != null) {
          var cell = row.getCell(j);
          data.add(getStringValue(cell));
        } else {
          data.add(null);
        }
      }
      rows.add(data);
    }

    return rows.stream();
  }

  private Integer getLastColumn(XSSFSheet sheet) {
    var lastRow = sheet.getLastRowNum();
    var max = 0;

    for (int i = 0; i <= lastRow; i++) {
      var row = sheet.getRow(i);
      if (row == null) {
        break;
      }
      max = Math.max(max, row.getLastCellNum());
    }
    return max;
  }

  private String getStringValue(XSSFCell cell) {
    if (cell == null) {
      return null;
    }

    NumberFormat nf = DecimalFormat.getInstance();
    nf.setGroupingUsed(false);
    nf.setMinimumFractionDigits(0);

    switch (cell.getCellType()) {
      case BLANK:
        return "";
      case BOOLEAN:
        return cell.getBooleanCellValue() ? "TRUE" : "FALSE";
      case FORMULA:
        return cell.getCellFormula();
      case NUMERIC:
        if (DateUtil.isCellDateFormatted(cell)) {
          DateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy", LocaleUtil.getUserLocale());
          sdf.setTimeZone(LocaleUtil.getUserTimeZone());
          return sdf.format(cell.getDateCellValue());
        }

        return nf.format(cell.getNumericCellValue());
      case STRING:
        return cell.getRichStringCellValue().toString();
      case ERROR:
        return ErrorEval.getText(cell.getErrorCellValue());
      default:
        return "Unknown Cell Type: " + cell.getCellType();
    }
  }

  public TableV2 createTableTable(XSSFTable table) {
    var columns = table.getColumns().stream().map(XSSFTableColumn::getName)
        .collect(Collectors.toList());

    return TableV2.builder()
        .subName(table.getName())
        .columns(columns)
        .metadata(Map.of("type", "TABLE"))
        .build();
  }

  public Stream<List<String>> createTableTableData(XSSFTable table) {
    var range = table.getArea();

    var sheet = table.getXSSFSheet();
    List<List<String>> rows = new ArrayList<>();

    for (int i = range.getFirstCell().getRow() + table.getHeaderRowCount();
        i <= range.getLastCell().getRow(); i++) {
      var row = sheet.getRow(i);
      var data = new ArrayList<String>();
      for (int j = range.getFirstCell().getCol(); j <= range.getLastCell().getCol(); j++) {
        data.add(getStringValue(row.getCell(j)));
      }
      rows.add(data);
    }

    return rows.stream();
  }

  /**
   * Convert an integer column (zero indexed) to Excel-style letter inputs (A, B, C .. AA ... IV)
   *
   * @param index integer index
   * @return letter column name
   */
  private String getColumnName(int index) {
    if (index < 25) {
      return EXCEL_COLUMNS.get(index);
    } else {
      var first = EXCEL_COLUMNS.get(index / 26);
      var second = EXCEL_COLUMNS.get(index % 26);
      return first + second;
    }
  }
}
