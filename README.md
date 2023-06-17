# FolderJDBC

Query files in a folder using SQL via JDBC. Compatible with all applications that support JDBC. Powered by SQLite.

**This project is still early in its development. Expect bugs and drastic changes from one version to another.**

## Usage
These instructions are for querying data in DBeaver.

1. Download the fat JAR from the releases page
2. In DBeaver, click Database > Driver Manager
3. Add `FolderJDBC` with the following parameters

![dbeaver](/docs/img/dbeaver-install.png)

3. On the Libraries tab, add the JAR file
4. Create a new DB connection. Only required configuration is the folder path
5. Browse files as databases

## Supported Files

### Comparison

| Reader        | Extensions    | Streaming? |
|---------------|------------   |------------|
| `csv`         | .csv          | Yes        |
| `tsv`         | .tsv          | Yes        |
| `psv`         | .psv          | Yes        |
| `line`        | .txt          | Yes        |
| `fixedwidth`  |               | Yes        |
| `regex`       |               | Yes        |
| `excel`       | .xlsx         | No         |
| `json`        | .json, .jsonc | Yes        |
| `yaml`        | .yaml, .yml   | Yes        |
| `xml`         | .xml, .html   | No         |

**Streaming** means that the entire file does not need to be loaded into memory. This allows for better support of
large files.

### `csv`/`tsv`/`psv`

This class of delimited readers all work the same. The difference is the character used for delimiting. `csv` uses
commas (`,`), `tsv` uses tabs (`\t`), `psv` uses pipes (`|`).

#### Configuration
`csvDelimiter`: customize the character(s) that are used to delimit the file

#### Example

test.csv
```
foo,bar,baz
one,two,three
1,2,3
```

```sql
select * from "test.csv"
```

| foo | bar | baz   |
|-----|-----|-------|
| one | two | three |
| 1   | 2   | 3     |

### `line`

Each line of a plaintext file becomes a row in a table with a single column `line`.

#### Configuration
none

#### Example

test.txt
```
Lorem ipsum dolor sit amet,
consectetur adipiscing elit,
sed do eiusmod tempor incididunt
ut labore et dolore magna aliqua
```

```sql
select * from "test.txt"
```

| line                             |
|----------------------------------|
| Lorem ipsum dolor sit amet,      |
| consectetur adipiscing elit,     |
| sed do eiusmod tempor incididunt |
| ut labore et dolore magna aliqua |

### `fixedwidth`

Reader to support flat files where each column is a set number of characters wide. By default, it will attempt to guess
where the boundaries between files are.

> `fixedwidth` has no default file extensions. `reader` configuration must be set to `fixedwidth`

#### Configuration
TODO

#### Example

test.csv
```
foo  bar baz
one  two three
1    2   3
```

```sql
select * from "test.txt"
```

| column1 | column2 | column3 |
|---------|---------|---------|
| foo     | bar     | baz     |
| one     | two     | three   |
| 1       | 2       | 3       |

