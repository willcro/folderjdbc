package com.willcro.folderdb.integration

import com.willcro.folderdb.jdbc.FolderDbDriver
import groovy.sql.Sql
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Files
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
        Files.walk(pathToBeDeleted)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
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
        type   | filename
        'csv'  | 'test1.csv'
        'psv'  | 'test1.psv'
        'tsv'  | 'test1.tsv'
        'json' | 'test1.json'
    }

    def 'can read regex with no errors'() {
        when: 'querying a valid file'
        def rows = sql.rows('select * from "test1.txt"')

        then: 'all rows to be returned'
        rows.size() == 3
        with(rows[0]) {
            get("group1") == "foo"
            get("group2") == "bar"
            get("group3") == "baz"
        }
        with(rows[1]) {
            get("group1") == "test"
            get("group2") == "test"
            get("group3") == "test"
        }
        with(rows[2]) {
            get("group1") == "one"
            get("group2") == "two"
            get("group3") == "three"
        }
    }

    def 'can read a txt file with no errors'() {
        when: 'querying a txt file'
        def rows = sql.rows('select * from "test1.txt"')

        then: 'all rows are returned'
        rows.size() == 3
        rows[0].get("line") == "line1"
        rows[0].get("line") == "foo,bar,baz"
        rows[0].get("line") == "line3"
    }

}
