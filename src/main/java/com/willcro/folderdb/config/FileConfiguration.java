package com.willcro.folderdb.config;

import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class FileConfiguration {

  /**
   * ID of reader to use
   *
   * One of: - `csv` - `tsv` - `psv` - `line` - `fixedwidth` - `regex` - `excel - `json` - `yaml` -
   * `xml`
   */
  private String reader;

  /**
   * Delimiter to use with CSV files.
   *
   * Default: ',' for `csv`, '|' for `psv`, '\t' for `tsv`
   *
   * Only applicable to `csv`, `psv`, `tsv`
   */
  private Character csvDelimiter;

  /**
   * Regex used to extract columns from lines in a file Each capture group will represent a single
   * column.
   *
   * Default: none
   *
   * Required for `regex` reader
   */
  private String pattern;

  /**
   * Columns to ignore, by name
   *
   * Default: none
   *
   * Only one of ignoreColumns and ignoreColumnIndexes should be provided
   *
   * NOT FUNCTIONAL
   */
  private List<String> ignoreColumns;

  /**
   * Columns to ignore, by index
   *
   * Only one of ignoreColumns and ignoreColumnIndexes should be provided
   *
   * NOT FUNCTIONAL
   */
  private List<Integer> ignoreColumnIndexes;

  /**
   * XPath to fetch data in XML docs
   *
   * Default: none
   *
   * Required for `xml` reader
   */
  private String xpath;

  /**
   * Path to the array of data. If null, array should be at the root of the file.
   *
   * Default: none (root of file)
   *
   * Only applicable to the `json` and `yaml` reader
   *
   * Example: path = "foo.bar" { "foo": { "bar": [...] } }
   */
  private String path;

  /**
   * Column widths for fixed width files. Indicates the widths of each column
   *
   * Default: automatically calculated
   *
   * Only applicable to the `fixedwidth` reader
   */
  private List<Integer> fixedWidthColumns;

  /**
   * Determines if the first row of the file should be treated as the column headers
   *
   * Default: true
   *
   * Only applicable to the `csv`, `psv`, `tsv` file types. A shortcut for setting this to false
   * exists by setting the file extension to `csvnh`, `psvnh`, or `tsvnh`
   */
  private Boolean hasHeaders;

  /**
   * Custom configurations for plugins
   */
  private Map<String, Object> custom;

}
