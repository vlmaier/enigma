package de.yapp.enigma_test.db;

public class KeysTable implements IKeysColumns
{
    public static final String SQL_CREATE =
            "CREATE TABLE IF NOT EXISTS " + KEYS_TABLE_NAME + "(" +
                    KEYID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KEY + " BLOB, " +
                    CURRENTKEYID + " INT, " +
                    CURRENTSIGNKEYID + " INT " + ");";

    public static final String SQL_DROP =
            "DROP TABLE IF EXISTS " + KEYS_TABLE_NAME;

    public static final String SQL_SELECT_ALL =
            "SELECT *" + "FROM " + KEYS_TABLE_NAME;

    public static final String SQL_DELETE =
            "DELETE FROM " + KeysTable.KEYS_TABLE_NAME + " WHERE " + KeysTable.KEYID + " = ?";
}
