package de.yapp.enigma_test.db;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import BasicClasses.BasicContactBook;
import de.yapp.enigma_test.Contact;

public class ContactRepository extends BasicContactBook
{
    private static DBHandler db;
    private static List<Contact> allContacts;

    public ContactRepository (Context context)
    {
        db = new DBHandler(context);
        allContacts = db.getContactsTable();
    }

    public ContactRepository (BasicContactBook basicContactBook, Context context)
    {
        for (int i = 0; i < basicContactBook.ContactBook.size(); i++)
        {
            allContacts = new ArrayList<Contact>();
            // TODO: test if it really works, because the constructor of Contact will use ContactRepository to check for existing contact
            allContacts.add(new Contact(basicContactBook.ContactBook.get(i), context));
        }
    }

    public List<Contact> getAllContacts ()
    {
        return allContacts;
    }

    public Contact[] getAllContactsAsArray ()
    {
        if (allContacts == null)
        {
            return new Contact[]{};
        }
        else
        {
            return allContacts.toArray(new Contact[allContacts.size()]);
        }
    }

    public int getAmountOfContacts ()
    {
        return allContacts.size();
    }

    public void addContact (Contact contact)
    {
        db.addContact(contact);
        allContacts.add(contact);
        Collections.sort(allContacts);
    }

    public Contact findContact (int ID)
    {
        for (int i = 0; i < allContacts.size(); i++)
        {
            if (allContacts.get(i).getID() == ID) return allContacts.get(i);
        }
        return null;
    }

    public Contact findContact (String phonenumber)
    {
        for (int i = 0; i < allContacts.size(); i++)
        {
            if (allContacts.get(i).getPhonenumber() == phonenumber) return allContacts.get(i);
        }
        return null;
    }

    public boolean deleteContact (int ID)
    {
        Contact contact = findContact(ID);
        if (contact != null)
        {
            allContacts.remove(contact);
            db.deleteContact(ID);
            return true;
        }
        else
        {
            return false;
        }
    }

    public BasicContactBook toBasicContactBook (Context context)
    {
        allContacts = db.getContactsTable();
        BasicContactBook basicContactBook = new BasicContactBook();
        for (int i = 0; i < allContacts.size(); i++)
        {
            basicContactBook.ContactBook.add(allContacts.get(i).toBasicUser());
        }
        basicContactBook.User = Contact.getMe(context).toBasicUser();
        return basicContactBook;
    }
}