package com.willcro.folderdb.config;

import lombok.Data;

import java.util.List;

@Data
public class FileConfiguration {

    /**
     * ID of reader to use
     */
    private String reader;

    /**
     * Delimiter to use with CSV files.
     *
     * Default: ',' for `csv`, '|' for `psv`, '\t' for `tsv`
     *
     * Only applicable to `csv`, `psv`, `tsv`
     */
    private String csvDelimiter;

    /**
     * Regex used to extract columns from lines in a file
     *
     * TODO: more detail in these docs
     */
    private String pattern;

    /**
     * Columns to ignore, by name
     *
     * Only one of ignoreColumns and ignoreColumnIndexes should be provided
     */
    private List<String> ignoreColumns;

    /**
     * Columns to ignore, by index
     *
     * Only one of ignoreColumns and ignoreColumnIndexes should be provided
     */
    private List<Integer> ignoreColumnIndexes;



}
