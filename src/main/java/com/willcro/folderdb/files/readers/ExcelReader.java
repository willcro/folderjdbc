package com.willcro.folderdb.files.readers;

import com.willcro.folderdb.config.FileConfiguration;
import com.willcro.folderdb.exception.ConfigurationException;
import com.willcro.folderdb.sql.Table;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFTable;
import org.apache.poi.xssf.usermodel.XSSFTableColumn;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Reads excel (xlsx) files.
 *
 * Supports two different types of tables:
 * - Normal sheets
 * - Excel tables (https://support.microsoft.com/en-us/office/overview-of-excel-tables-7ab0bb7d-3a9e-4b56-a3c9-6c94334e492c)
 *
 * WARNING: this reader currently does not support streaming, so large files may cause issues
 */
public class ExcelReader extends BaseReader {

    private static final List<String> EXCEL_COLUMNS = Arrays.asList("A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z");

    @Override
    public String getId() {
        return "excel";
    }

    @Override
    public List<Table> readFile(File file, FileConfiguration config) throws ConfigurationException {
        XSSFWorkbook workbook;
        try (FileInputStream fip = new FileInputStream(file)) {
            workbook = new XSSFWorkbook(fip);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        var tables = new ArrayList<Table>();
        var sheetCount = workbook.getNumberOfSheets();
        for (int i = 0; i < sheetCount; i++) {
            var sheet = workbook.getSheetAt(i);
            tables.add(createSheetTable(sheet, file.getName()));
            var sheetTables = sheet.getTables().stream()
                    .map(t -> createTableTable(t, file.getName()))
                    .collect(Collectors.toList());
            tables.addAll(sheetTables);
        }

        return tables;
    }

    public Table createSheetTable(XSSFSheet sheet, String filename) {
        var lastRow = sheet.getLastRowNum();
        var lastColumn = sheet.getRow(lastRow).getLastCellNum();

        var name = filename + "_" + sheet.getSheetName();
        var columns = IntStream.range(0, lastColumn).boxed()
                .map(this::getColumnName)
                .collect(Collectors.toList());
        List<List<String>> rows = new ArrayList<>();

        for (int i = 0; i <= lastRow; i++) {
            var row = sheet.getRow(i);
            var data = new ArrayList<String>();
            for (int j = 0; j < lastColumn; j++) {
                data.add(row.getCell(j).getRawValue());
            }
            rows.add(data);
        }
        return Table.builder()
                .name(name)
                .columns(columns)
                .rows(rows.stream())
                .build();
    }

    public Table createTableTable(XSSFTable table, String filename) {
        var columns = table.getColumns().stream().map(XSSFTableColumn::getName).collect(Collectors.toList());
        var range = table.getArea();
        var name = filename + "_" + table.getName();

        var sheet = table.getXSSFSheet();
        List<List<String>> rows = new ArrayList<>();

        for (int i = range.getFirstCell().getRow() + table.getHeaderRowCount(); i <= range.getLastCell().getRow(); i++) {
            var row = sheet.getRow(i);
            var data = new ArrayList<String>();
            for (int j = range.getFirstCell().getCol(); j <= range.getLastCell().getCol(); j++) {
                data.add(row.getCell(j).getRawValue());
            }
            rows.add(data);
        }

        return Table.builder()
                .name(name)
                .columns(columns)
                .rows(rows.stream())
                .build();
    }

    /**
     * Convert an integer column (zero indexed) to Excel-style letter inputs (A, B, C .. AA ... IV)
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
