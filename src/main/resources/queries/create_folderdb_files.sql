CREATE TABLE IF NOT EXISTS "_folderdb_files" (
	filename TEXT NOT NULL,
	hash INTEGER NOT NULL,
	dirty INTEGER DEFAULT 0,
	loaded_data INTEGER DEFAULT 0
);
