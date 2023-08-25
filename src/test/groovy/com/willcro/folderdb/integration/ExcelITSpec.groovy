package com.willcro.folderdb.integration

import com.willcro.folderdb.jdbc.FolderDbDriver
import groovy.sql.Sql
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path

class ExcelITSpec extends Specification {

    Sql sql

    String folder = FolderDbITSpec.class.getResource("/excel").toURI().toString().substring(6)

    def setup() {
        deleteDirectory(Path.of("$folder/.folderdb"))
        def connection = new FolderDbDriver().connect("jdbc:folderdb:$folder", new Properties())
        sql = new Sql(connection)
    }

    def deleteDirectory(Path pathToBeDeleted) {
        try {
            Files.walk(pathToBeDeleted)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete)
        } catch (NoSuchFileException ex) {
            // do nothing
        }
    }

    def 'can read spreadsheet that has a duplicated name'() {
        when: 'querying a valid file with quotation marks'
        def rows = sql.rows('select * from "duplicate_name.xlsx/Table1"')
        def rows2 = sql.rows('select * from "duplicate_name.xlsx/Table1 (1)"')

        then: 'all rows to be returned'
        rows.size() == 3
        with(rows[0]) {
            get("A") == "a"
            get("B") == "b"
            get("C") == "c"
        }
        with(rows[1]) {
            get("A") == "d"
            get("B") == "e"
            get("C") == "f"
        }
        with(rows[2]) {
            get("A") == "g"
            get("B") == "h"
            get("C") == "i"
        }

        rows2.size() == 2
        with(rows2[0]) {
            get("foo") == "1"
            get("bar") == "2"
            get("baz") == "3"
        }
        with(rows2[1]) {
            get("foo") == "4"
            get("bar") == "5"
            get("baz") == "6"
        }
    }
}
