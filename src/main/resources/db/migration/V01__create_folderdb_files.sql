CREATE TABLE IF NOT EXISTS "_folderdb_files" (
	filename TEXT NOT NULL,
	update_type TEXT NOT NULL,
	update_value TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS "_folderdb_tables" (
    filename TEXT NOT NULL,
	table_name TEXT NOT NULL,
	loaded_data INTEGER NOT NULL DEFAULT 0,
	dirty INTEGER NOT NULL DEFAULT 0
);
