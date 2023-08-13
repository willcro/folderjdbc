CREATE TABLE IF NOT EXISTS "_folderdb_files" (
	filename TEXT NOT NULL,
	error TEXT NULL,
	update_type TEXT NULL,
	update_value TEXT NULL
);

CREATE TABLE IF NOT EXISTS "_folderdb_tables" (
    filename TEXT NOT NULL,
	table_name TEXT NOT NULL,
	sub_name TEXT NULL,
	columns TEXT NOT NULL,
	metadata TEXT NULL,
	loaded_data INTEGER NOT NULL DEFAULT 0,
	dirty INTEGER NOT NULL DEFAULT 0
);