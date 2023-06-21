package com.willcro.folderdb.integration

import com.willcro.folderdb.jdbc.FolderDbDriver
import groovy.sql.Sql
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path

class CsvITSpec extends Specification {

    Sql sql

    def setup() {
        def folder = FolderDbITSpec.class.getResource("/csvs").toURI().toString().substring(6)
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

    def 'can read csv with no quotes'() {
        when: 'querying a valid file'
        def rows = sql.rows('select * from "basic.csv"')

        then: 'all rows to be returned'
        rows.size() == 3
        with(rows[0]) {
            get("test1") == "foo"
            get("test2") == "bar"
            get("test3") == "baz"
        }
        with(rows[1]) {
            get("test1") == "test"
            get("test2") == "test"
            get("test3") == "test"
        }
        with(rows[2]) {
            get("test1") == "one"
            get("test2") == "two"
            get("test3") == "three"
        }
    }

    def 'can read csv with quotes'() {
        when: 'querying a valid file with quotation marks'
        def rows = sql.rows('select * from "quotes.csv"')

        then: 'all rows to be returned'
        rows.size() == 1
        with(rows[0]) {
            get("test1") == "foo,bar,baz"
            get("test2") == "one,two,three"
            get("test3") == "1,2,3"
        }
    }

    def 'can read csv with quotes containing newlines'() {
        when: 'querying a valid file with quotation marks'
        def rows = sql.rows('select * from "quotes_newline.csv"')

        then: 'all rows to be returned'
        rows.size() == 1
        with(rows[0]) {
            get("test1") == "foo,bar,baz"
            get("test2") == "one,two,\nthree" || get("test2") == "one,two,\r\nthree"
            get("test3") == "1,2,3"
        }
    }

    def 'can read csv with commas in column names'() {
        when: 'querying a valid file with quotation marks'
        def rows = sql.rows('select * from "quotes_in_columns.csv"')

        then: 'all rows to be returned'
        rows.size() == 1
        with(rows[0]) {
            get("test,1") == "foo"
            get("test,2") == "bar"
            get("test,3") == "baz"
        }
    }

    def 'can read custom delimited file'() {
        when: 'querying a valid file delimited by a custom character'
        def rows = sql.rows('select * from "custom_delimiter.csv"')

        then: 'all rows to be returned'
        rows.size() == 1
        with(rows[0]) {
            get("test1") == "foo"
            get("test2") == "bar"
            get("test3") == "baz"
        }
    }

}
