package com.willcro.folderdb.files.readers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ReaderRegistry {

    private static final Set<FileTableReader> registeredReaders = new HashSet<>();
    private static final Map<String, FileTableReader> idToReader = new HashMap<>();

    static {
        register(new CsvReader());
        register(new TsvReader());
        register(new PsvReader());
        register(new JsonReader());
        register(new ExcelReader());
        register(new RegexReader());
    }

    public static void register(FileTableReader reader) {
        registeredReaders.add(reader);
        idToReader.put(reader.getId().toLowerCase(), reader);
    }

    /**
     * Gets a registered reader by its ID
     * @param id ID to look up
     * @return the reader registered with this ID
     */
    public static FileTableReader getById(String id) {
        return idToReader.get(id.toLowerCase());
    }

}
