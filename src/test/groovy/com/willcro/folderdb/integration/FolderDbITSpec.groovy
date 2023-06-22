package com.willcro.folderdb.integration

import com.willcro.folderdb.jdbc.FolderDbDriver
import groovy.sql.Sql
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path

class FolderDbITSpec extends Specification {

    Sql sql

    def setup() {
        def folder = FolderDbITSpec.class.getResource("/test1").toURI().toString().substring(6)
        deleteDirectory(Path.of("$folder/.folderdb"))
        def connection = new FolderDbDriver().connect("jdbc:folderdb:$folder", new Properties())
        sql = new Sql(connection)
    }

    def deleteDirectory(Path pathToBeDeleted) {
        try {
            Files.walk(pathToBeDeleted)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (NoSuchFileException ex) {
            // ignore
        }
    }

    @Unroll
    def 'can read #type with no errors'() {
        when: 'querying a valid file'
        def rows = sql.rows("select * from \"$filename\"")

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

        where: 'different file types are tried'
        type  | filename
        'csv' | 'test1.csv'
        'psv' | 'test1.psv'
        'tsv' | 'test1.tsv'
    }

    def 'can read regex with no errors'() {
        when: 'querying a valid file'
        def rows = sql.rows('select * from "regex1.txt"')

        then: 'all rows to be returned'
        rows.size() == 3
        with(rows[0]) {
            get("group1") == "foo"
            get("group2") == "001"
            get("group3") == "test"
        }
        with(rows[1]) {
            get("group1") == "bar"
            get("group2") == "002"
            get("group3") == "test"
        }
        with(rows[2]) {
            get("group1") == "baz"
            get("group2") == "003"
            get("group3") == "test"
        }
    }

    def 'can read a txt file with no errors'() {
        when: 'querying a txt file'
        def rows = sql.rows('select * from "test1.txt"')

        then: 'all rows are returned'
        rows.size() == 3
        rows[0].get("line") == "line1"
        rows[1].get("line") == "foo,bar,baz"
        rows[2].get("line") == '"line3"'
    }

    def 'can read a excel sheet with no errors'() {
        when: 'querying a txt file'
        def rows = sql.rows('select * from "test1.xlsx_Sheet1"')

        then: 'all rows are returned'
        rows.size() == 3
        with(rows[0]) {
            get("a") == "foo"
            get("b") == "bar"
            get("c") == "baz"
        }
        with(rows[1]) {
            get("a") == "one"
            get("b") == "two"
            get("c") == "three"
        }
        with(rows[2]) {
            get("a") == "1"
            get("b") == "2"
            get("c") == "3"
        }
    }

    def 'can read a excel table with no errors'() {
        when: 'querying a txt file'
        def rows = sql.rows('select * from "test1.xlsx_Table1"')

        then: 'all rows are returned'
        rows.size() == 2
        with(rows[0]) {
            get("foo") == "one"
            get("bar") == "two"
            get("baz") == "three"
        }
        with(rows[1]) {
            get("foo") == "1"
            get("bar") == "2"
            get("baz") == "3"
        }
    }

    def 'can read a yaml file with no errors'() {
        when: 'querying a txt file'
        def rows = sql.rows('select * from "test1.yml"')

        then: 'all rows are returned'
        rows.size() == 2
        with(rows[0]) {
            get("foo") == "one"
            get("bar") == "1"
            get("baz") == "true"
        }
        with(rows[1]) {
            get("foo") == "two"
            get("bar") == "2"
            get("baz") == "false"
        }
    }

    def 'can read a json file with no errors'() {
        when: 'querying a txt file'
        def rows = sql.rows('select * from "test1.json"')

        then: 'all rows are returned'
        rows.size() == 2
        with(rows[0]) {
            get("foo") == "one"
            get("bar") == "1"
            get("baz") == "true"
        }
        with(rows[1]) {
            get("foo") == "two"
            get("bar") == "2"
            get("baz") == "false"
        }
    }

    def 'can read a xml file with no errors'() {
        when: 'querying a txt file'
        def rows = sql.rows('select * from "test1.xml"')

        then: 'all rows are returned'
        rows.size() == 2
        with(rows[0]) {
            get("foo") == "one"
            get("bar") == "1"
            get("baz") == "true"
        }
        with(rows[1]) {
            get("foo") == "two"
            get("bar") == "2"
            get("baz") == "false"
        }
    }

    def 'can read a fixed width file with no errors'() {
        when: 'querying a txt file'
        def rows = sql.rows('select * from "fixedwidth1.txt"')

        then: 'all rows are returned'
        rows.size() == 3
        with(rows[0]) {
            get("column0") == "foo"
            get("column1") == "bar"
            get("column2") == "baz"
        }
        with(rows[1]) {
            get("column0") == "one"
            get("column1") == "two"
            get("column2") == "three"
        }
        with(rows[2]) {
            get("column0") == "1"
            get("column1") == "2"
            get("column2") == "3"
        }
    }

}
