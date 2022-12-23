package de.yapp.enigma_test.db;

public class ContactsTable implements IContactsColumns
{
    public static final String SQL_CREATE =
            "CREATE TABLE IF NOT EXISTS " + CONTACTS_TABLE_NAME + "(" +
                    ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    GUID + " TEXT , " +
                    NAME + " TEXT NOT NULL, " +
                    PHONENUMBER + " TEXT NOT NULL, " +
                    PICTURE + " BLOB " + ");";

    public static final String SQL_DROP =
            "DROP TABLE IF EXISTS " + CONTACTS_TABLE_NAME;

    public static final String SQL_SELECT_ALL =
            "SELECT *" + "FROM " + CONTACTS_TABLE_NAME;

    public static final String SQL_DELETE =
            "DELETE FROM " + ContactsTable.CONTACTS_TABLE_NAME + " WHERE " + ContactsTable.ID + " = ?";
}
