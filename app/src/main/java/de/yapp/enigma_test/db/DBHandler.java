package de.yapp.enigma_test.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import org.spongycastle.openpgp.PGPException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.yapp.enigma_test.Chat;
import de.yapp.enigma_test.Contact;
import de.yapp.enigma_test.CryptoPGP;

public class DBHandler extends SQLiteOpenHelper
{
    private static final String DATENBANK_NAME = "db";
    private static final int DATENBANK_VERSION = 1;
    private Context context;

    public DBHandler (Context context)
    {
        super(context, DATENBANK_NAME, null, DATENBANK_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate (SQLiteDatabase db)
    {
        db.execSQL(KeysTable.SQL_CREATE);
        db.execSQL(ContactsTable.SQL_CREATE);
        db.execSQL(ChatsTable.SQL_CREATE);
    }

    @Override
    public void onUpgrade (SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL(KeysTable.SQL_DROP);
        db.execSQL(ContactsTable.SQL_DROP);
        db.execSQL(ChatsTable.SQL_DROP);
        onCreate(db);
    }

    public void addKey (CryptoPGP key) throws IOException
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KeysTable.KEY, key.ExportKeyRing());
        values.put(KeysTable.CURRENTKEYID, key.getCurrentKeyID());
        values.put(KeysTable.CURRENTSIGNKEYID, key.getCurrentSignKeyID());
        db.insert(KeysTable.KEYS_TABLE_NAME, null, values);
        db.close();
    }

    public void deleteKey (int ID)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        SQLiteStatement stmt = db.compileStatement(KeysTable.SQL_DELETE);
        stmt.bindLong(1, ID);
        stmt.execute();
        db.close();
    }

    public void addContact (Contact contact)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ContactsTable.GUID, contact.getGUID());
        values.put(ContactsTable.NAME, contact.getName());
        values.put(ContactsTable.PHONENUMBER, contact.getPhonenumber());
        values.put(ContactsTable.PICTURE, contact.getPictureAsByteArray());

        long id = db.insert(ContactsTable.CONTACTS_TABLE_NAME, null, values);
        if (id != -1) contact.setID((int) id);
        db.close();
    }

    public void deleteContact (int ID)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        SQLiteStatement stmtContact = db.compileStatement(ContactsTable.SQL_DELETE);
        SQLiteStatement stmtChat = db.compileStatement(ChatsTable.SQL_DELETE_CONTACT_CHATS);
        stmtContact.bindLong(1, ID);
        stmtChat.bindLong(1, ID);
        stmtContact.execute();
        stmtChat.execute();
        db.close();
    }

    public void addChat (Chat chat)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ChatsTable.CONTACTID, chat.getSender().getID());
        values.put(ChatsTable.NAME, chat.getName());
        values.put(ChatsTable.DURATION, chat.getDuration());
        values.put(ChatsTable.MESSAGES, chat.getMessagesAsByteArray());

        long id = db.insert(ChatsTable.CHATS_TABLE_NAME, null, values);
        if (id != -1) chat.setID((int) id);
        db.close();
    }

    public void deleteChat (int ID)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        SQLiteStatement stmt = db.compileStatement(ChatsTable.SQL_DELETE);
        stmt.bindLong(1, ID);
        stmt.execute();
        db.close();
    }

    public void addMessage (Chat chat, byte[] msg)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        SQLiteStatement stmt = db.compileStatement(ChatsTable.SQL_ADD);
        stmt.bindBlob(1, msg);
        stmt.bindLong(2, chat.getID());
        stmt.execute();
    }

    public List<CryptoPGP> getKeysTable ()
    {
        List<CryptoPGP> keys = new ArrayList<CryptoPGP>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(KeysTable.SQL_SELECT_ALL, null);

        try
        {
            if (cursor.moveToFirst())
            {
                do
                {
                    CryptoPGP key = new CryptoPGP();
                    key.ImportKeyRing(cursor.getBlob(1));
                    key.setCurrentKeyID(cursor.getLong(2));
                    key.setCurrentSignKeyID(cursor.getLong(3));
                    keys.add(key);
                } while (cursor.moveToNext());
            }
        }
        catch (PGPException e)
        {
            Log.e("", "PGPException: " + e.getMessage());
        }
        catch (IOException e)
        {
            Log.e("", "IOException: " + e.getMessage());
        }
        finally
        {
            cursor.close();
            db.close();
        }
        return keys;
    }

    public List<Contact> getContactsTable ()
    {
        List<Contact> contacts = new ArrayList<Contact>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(ContactsTable.SQL_SELECT_ALL, null);

        try
        {
            if (cursor.moveToFirst())
            {
                do
                {
                    Contact contact = new Contact();

                    contact.setID(cursor.getInt(0));
                    contact.setGUID(cursor.getString(1));
                    contact.setName(cursor.getString(2));
                    contact.setPhonenumber(cursor.getString(3));
                    contact.setPicture(context, cursor.getBlob(4));

                    contacts.add(contact);
                } while (cursor.moveToNext());
            }
            Collections.sort(contacts);
            return contacts;
        }
        finally
        {
            cursor.close();
            db.close();
        }
    }

    public List<Chat> getChatsTable (Contact contact)
    {
        List<Chat> chats = new ArrayList<Chat>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(ChatsTable.SQL_SELECT_ALL, null);

        try
        {
            if (cursor.moveToFirst())
            {
                do
                {
                    if (contact.getID() == cursor.getInt(1))
                    {
                        Chat chat = new Chat();

                        chat.setID(cursor.getInt(0));
                        chat.setSender(contact);
                        chat.setName(cursor.getString(2));
                        chat.setDuration(cursor.getInt(3));
                        chat.setMessages(cursor.getBlob(4));

                        chats.add(chat);
                    }
                } while (cursor.moveToNext());
            }
            Collections.sort(chats);
            return chats;
        }
        finally
        {
            cursor.close();
            db.close();
        }
    }

    public List<Chat> getChatsTable ()
    {
        List<Chat> chats = new ArrayList<Chat>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(ChatsTable.SQL_SELECT_ALL, null);

        try
        {
            if (cursor.moveToFirst())
            {
                do
                {
                    Chat chat = new Chat();

                    chat.setID(cursor.getInt(0));
                    chat.setSender(new ContactRepository(context).findContact(cursor.getInt(1)));
                    chat.setName(cursor.getString(2));
                    chat.setDuration(cursor.getInt(3));
                    chat.setMessages(cursor.getBlob(4));

                    chats.add(chat);
                } while (cursor.moveToNext());
            }
            Collections.sort(chats);
            return chats;
        }
        finally
        {
            cursor.close();
            db.close();
        }
    }
}