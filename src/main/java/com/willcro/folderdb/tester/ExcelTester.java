package com.willcro.folderdb.tester;

import com.willcro.folderdb.sql.Table;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFTable;
import org.apache.poi.xssf.usermodel.XSSFTableColumn;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ExcelTester {

    public static final List<String> EXCEL_COLUMNS = Arrays.asList("A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z");

    public static void main(String[] args) throws IOException {
        File file = new File("C:\\Users\\Will\\Documents\\folderdbtest\\testfiles\\test.xlsx");
        FileInputStream fip = new FileInputStream(file);
        XSSFWorkbook workbook = new XSSFWorkbook(fip);

        var sheet1 = workbook.getSheetAt(0);
        var sheet1Tables = sheet1.getTables();

        var sheet2 = workbook.getSheetAt(1);
        var sheet2Tables = sheet2.getTables();

        var sheet3 = workbook.getSheetAt(2);
        var sheet3Tables = sheet3.getTables();

        var table1 = createTableTable(sheet1Tables.get(0));
        var table2 = createSheetTable(sheet2);
        var table3 = createTableTable(sheet3Tables.get(0));

        System.out.println("");
    }

    public static Table createSheetTable(XSSFSheet sheet) {
        var lastRow = sheet.getLastRowNum();
        var lastColumn = sheet.getRow(lastRow).getLastCellNum();

        var name = sheet.getSheetName();
        var columns = EXCEL_COLUMNS.subList(0, lastColumn);
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

    public static Table createTableTable(XSSFTable table) {
        var columns = table.getColumns().stream().map(XSSFTableColumn::getName).collect(Collectors.toList());
        var range = table.getArea();
        var name = table.getSheetName() + "_" + table.getName();

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
}
