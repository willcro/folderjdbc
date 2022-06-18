# TODO

## General

* Added logging to a database table
* Investigate doing hashing step in parallel
* Investigate using OS file update timestamp instead of hashing
* Support for subdirectories
* Create Github repository
* Add error handling
* Push JAR to Maven Central
* Handling Windows line endings (CRLF)
* Handle invalid column names
* Handle duplicate column names

## Configuration

* Read `defaults` from global configs
* Allow templated renaming of files
* Override column names
* Override column types
* Allow overrides by file type
* Allow overrides by regex
* Limit logging
* Add strict mode for error handling
* Allow configuration of lazy vs eager loading

## Documentation

* Document config file
* Project website

## JSON

* Support for second-level arrays
* Support for JSONPath selector
* Error handling
* Column types

## Delimited files

* Handle quoted contents
* Auto type handling
* Configuration for first row as column names
* Handle empty file
* Handle trailing newline

## Excel

* Support excel
* Handling of proper tables
* Handling of data outside proper tables

## Fixed-width file

* Support fixed width files
* Configuration to manually specify column widths
* Configuration to trim trailing whitespace or not

## XML

* Support for XML
* XPath support

## YAML Support

* Support for YAML
* JSONPath?

## Writing

* Support for creating new tables
* Support for adding rows to existing tables
* Support for deleting rows from tables
* Support for updating rows in existing tables

## Cloud support

* Support for Google Drive
* Support for Onedrive
* Support for Dropbox
* Support for Amazon S3
