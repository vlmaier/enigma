package de.yapp.enigma_test.db;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.yapp.enigma_test.Chat;
import de.yapp.enigma_test.Contact;
import de.yapp.enigma_test.Message;

public class ChatRepository
{
    private static DBHandler db;
    private static List<Chat> allChats;

    public ChatRepository (Context context)
    {
        db = new DBHandler(context);
        allChats = db.getChatsTable();
    }

    public List<Chat> getAllChats ()
    {
        return allChats;
    }

    public Chat[] getAllChatsAsArray ()
    {
        if (allChats == null)
        {
            return new Chat[]{};
        }
        else
        {
            return allChats.toArray(new Chat[allChats.size()]);
        }
    }

    public Chat[] getChats (Contact contact)
    {
        List<Chat> chatList = new ArrayList<Chat>();
        for (int i = 0; i < allChats.size(); i++)
        {
            if (allChats.get(i).getSender() != null && allChats.get(i).getSender().getID() == contact.getID())
            {
                chatList.add(allChats.get(i));
            }
        }
        return chatList.toArray(new Chat[chatList.size()]);
    }

    public int getAmountOfChats (Contact contact)
    {
        int amount = 0;
        for (int i = 0; i < allChats.size(); i++)
        {
            if (allChats.get(i).getSender() != null && allChats.get(i).getSender().getID() == contact.getID())
            {
                amount++;
            }
        }
        return amount;
    }

    public Chat findChat (int ID)
    {
        for (int i = 0; i < allChats.size(); i++)
        {
            if (allChats.get(i).getID() == ID) return allChats.get(i);
        }
        return null;
    }

    public Chat findChatWithServerChatID (int ID)
    {
        for (int i = 0; i < allChats.size(); i++)
        {
            if (allChats.get(i).getServerChatID() == ID) return allChats.get(i);
        }
        return null;
    }

    public void addChat (Chat chat)
    {
        db.addChat(chat);
        allChats.add(chat);
        Collections.sort(allChats);
    }

    public boolean deleteChat (int ID)
    {
        Chat chat = findChat(ID);
        if (chat != null)
        {
            allChats.remove(chat);
            db.deleteChat(ID);
            return true;
        }
        else
        {
            return false;
        }
    }

    public void addMessage (Chat chat, Message msg)
    {
        chat.getMessages().add(msg);
        for (int i = 0; i < allChats.size(); i++)
        {
            if (allChats.get(i).getID() == chat.getID())
            {
                allChats.get(i).getMessages().add(msg);
            }
        }
        db.addMessage(chat, chat.getMessagesAsByteArray());
    }
}