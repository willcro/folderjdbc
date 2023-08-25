package com.willcro.folderdb.integration

import com.willcro.folderdb.jdbc.FolderDbDriver
import groovy.sql.Sql
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.sql.SQLException

class CsvITSpec extends Specification {

    Sql sql

    String folder = FolderDbITSpec.class.getResource("/csvs").toURI().toString().substring(6)

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

    def 'can read file that contains a line with an invalid number of columns'() {
        when: 'querying a valid file delimited by a custom character'
        def rows = sql.rows('select * from "invalid_line.csv"')

        then: 'all rows to be returned'
        rows.size() == 2
        with(rows[0]) {
            get("test1") == "foo"
            get("test2") == "bar"
            get("test3") == "baz"
        }

        with(rows[1]) {
            get("test1") == "one"
            get("test2") == "two"
            get("test3") == "three"
        }
    }

    def 'can read csv with linux line endings'() {
        given: 'file with linux line endings'
        // I am writing this from the code because I don't trust git to leave them alone
        Files.writeString(Path.of(folder, "linux.csv"), "test1,test2,test3\n" +
                "foo,bar,baz\n" +
                "test,test,test\n" +
                "one,two,three")

        when: 'querying a valid file'
        def conditions = new PollingConditions()

        then: 'all rows to be returned'
        conditions.eventually {
            def rows = sql.rows('select * from "linux.csv"')
            rows.size() == 3
            with(rows[0]) {
                assert get("test1") == "foo"
                assert get("test2") == "bar"
                assert get("test3") == "baz"
            }
            with(rows[1]) {
                assert get("test1") == "test"
                assert get("test2") == "test"
                assert get("test3") == "test"
            }
            with(rows[2]) {
                assert get("test1") == "one"
                assert get("test2") == "two"
                assert get("test3") == "three"
            }
        }

        cleanup: 'delete created file'
        Files.deleteIfExists(Path.of(folder, "linux.csv"))
    }

    def 'can read csv with windows line endings'() {
        given: 'file with linux line endings'
        // I am writing this from the code because I don't trust git to leave them alone
        Files.writeString(Path.of(folder, "windows.csv"), "test1,test2,test3\n" +
                "foo,bar,baz\r\n" +
                "test,test,test\r\n" +
                "one,two,three")

        when: 'querying a valid file'
        def conditions = new PollingConditions()

        then: 'all rows to be returned'
        conditions.eventually {
            def rows = sql.rows('select * from "windows.csv"')
            assert rows.size() == 3
            with(rows[0]) {
                assert get("test1") == "foo"
                assert get("test2") == "bar"
                assert get("test3") == "baz"
            }
            with(rows[1]) {
                assert get("test1") == "test"
                assert get("test2") == "test"
                assert get("test3") == "test"
            }
            with(rows[2]) {
                assert get("test1") == "one"
                assert get("test2") == "two"
                assert get("test3") == "three"
            }
        }

        cleanup: 'delete created file'
        Files.deleteIfExists(Path.of(folder, "windows.csv"))
    }

    def 'can not read a file with an unterminated quote'() {
        when: 'querying an invalid file'
        def rows = sql.rows('select * from "unterminated_quote.csv"')

        then: 'all rows to be returned'
        def ex = thrown(SQLException)
    }

    def 'can read csv with duplicate column names'() {
        when: 'querying a valid file'
        def rows = sql.rows('select * from "csv_duplicate_headers.csv"')

        then: 'all rows to be returned'
        rows.size() == 3
        with(rows[0]) {
            get("test") == "foo"
            get("test (1)") == "bar"
            get("test (2)") == "baz"
        }
        with(rows[1]) {
            get("test") == "test"
            get("test (1)") == "test"
            get("test (2)") == "test"
        }
        with(rows[2]) {
            get("test") == "one"
            get("test (1)") == "two"
            get("test (2)") == "three"
        }
    }

}
