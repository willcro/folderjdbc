# FolderJDBC

Query files in a folder using SQL via JDBC. Compatible with all applications that support JDBC.
Powered by SQLite.

**This project is still early in its development. Expect bugs and drastic changes from one version
to another. This project was designed mainly to be used by me, but maybe someone else will find
this interesting.**

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

| Reader       | Extensions    | Streaming? |
|--------------|---------------|------------|
| `csv`        | .csv          | Yes        |
| `tsv`        | .tsv          | No         |
| `psv`        | .psv          | No         |
| `line`       | .txt          | Yes        |
| `fixedwidth` |               | Yes        |
| `regex`      |               | Yes        |
| `excel`      | .xlsx         | No         |
| `json`       | .json, .jsonc | Yes        |
| `yaml`       | .yaml, .yml   | Yes        |
| `xml`        | .xml, .html   | No         |
| `jsonflat`   | .jsonflat     | Yes        |

**Streaming** means that the entire file does not need to be loaded into memory. This allows for
better support of
large files.

### `csv`/`tsv`/`psv`

This class of delimited readers all work the same. The difference is the character used for
delimiting. `csv` uses
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
select *
from "test.csv"
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
select *
from "test.txt"
```

| line                             |
|----------------------------------|
| Lorem ipsum dolor sit amet,      |
| consectetur adipiscing elit,     |
| sed do eiusmod tempor incididunt |
| ut labore et dolore magna aliqua |

### `fixedwidth`

Reader to support flat files where each column is a set number of characters wide. By default, it
will attempt to guess
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
select *
from "test.txt"
```

| column1 | column2 | column3 |
|---------|---------|---------|
| foo     | bar     | baz     |
| one     | two     | three   |
| 1       | 2       | 3       |

### `regex`

Reader to support flat files where each line should be checked against a regex. Each capture group
will result in a separate column.

> `regex` has no default file extensions. `reader` configuration must be set to `regex`

#### Configuration

`pattern`: regex pattern to match against. Must contain capture groups. **Required**

#### Example

regex.txt

```
foo-001 test
bar-002 test
baz-003 test
```

Folderdbfile

```json
{
  "files": {
    "regex1.txt": {
      "reader": "regex",
      "pattern": "^(.*)-(.*) (.*)$"
    }
  }
}
```

```sql
select *
from "regex.txt"
```

| group1 | group2 | group3 |
|--------|--------|--------|
| foo    | 001    | test   |
| bar    | 002    | test   |
| baz    | 003    | test   |


### `jsonflat`

Reader that takes a JSON file and flattens it into just 2 columns: path and value. The benefit of
this is that it can read any JSON file regardless of structure. Extremely nested arrays or objects
can still be handled.

#### Configuration

none

#### Example

test.jsonflat

```json
{
  "foo": {
    "bar": "baz",
    "arr": [
      1,
      {
        "dog": "cat"
      }
    ]
  }
}
```

```sql
select *
from "test.jsonflat"
```

| path             | value |
|------------------|-------|
| $.foo.bar        | baz   |
| $.foo.arr[0]     | 1     |
| $.foo.arr[1].dog | cat   |
