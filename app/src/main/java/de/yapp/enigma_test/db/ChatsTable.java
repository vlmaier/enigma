package de.yapp.enigma_test.db;

public class ChatsTable implements IChatsColumns, IContactsColumns
{
    public static final String SQL_CREATE =
            "CREATE TABLE IF NOT EXISTS " + CHATS_TABLE_NAME + "(" +
                    IChatsColumns.CHATID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    CONTACTID + " INTEGER , " +
                    CHATNAME + " TEXT NOT NULL, " +
                    DURATION + " INTEGER, " +
                    MESSAGES + " BLOB, " +
                    "FOREIGN KEY(" + CONTACTID + ")" + " REFERENCES " + CONTACTS_TABLE_NAME + "(" + IContactsColumns.ID + ")" + ");";

    public static final String SQL_DROP =
            "DROP TABLE IF EXISTS " + CHATS_TABLE_NAME;

    public static final String SQL_SELECT_ALL =
            "SELECT *" + "FROM " + CHATS_TABLE_NAME;

    public static final String SQL_ADD =
            "UPDATE " + IChatsColumns.CHATS_TABLE_NAME +
                    " SET " + IChatsColumns.MESSAGES + " = ? " +
                    " WHERE " + IChatsColumns.CHATID + " = ?";

    public static final String SQL_DELETE_CONTACT_CHATS =
            "DELETE FROM " + ChatsTable.CHATS_TABLE_NAME + " WHERE " + ChatsTable.CONTACTID + " = ?";

    public static final String SQL_DELETE =
            "DELETE FROM " + ChatsTable.CHATS_TABLE_NAME + " WHERE " + ChatsTable.CHATID + " = ?";
}