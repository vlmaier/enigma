package de.yapp.enigma_test;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import BasicClasses.BasicChat;
import BasicClasses.BasicEncryptionSettings;
import de.yapp.enigma_test.adapter.ChatItemArrayAdapter;
import de.yapp.enigma_test.db.ChatRepository;

public class Chat extends BasicChat implements Comparable
{
    private int ID;
    private int serverChatID;
    private String name;
    private ArrayList<Message> messages;
    private Contact sender;
    private int duration;
    private BasicEncryptionSettings encryptionSettings;
    private int chatPosition;

    public Chat () {}

    public Chat (BasicChat basicChat, Context context)
    {
        Chat chat = new ChatRepository(context).findChatWithServerChatID(basicChat.ChatId);
        if (chat != null)
        {
            this.ID = chat.getID();
            this.serverChatID = basicChat.ChatId;
            this.name = chat.getName();
            this.messages = chat.getMessages();
            this.sender = new Contact(basicChat.User2, context);
            this.duration = chat.getDuration();
            this.encryptionSettings = basicChat.EncryptionSettings;
        }
    }

    public Chat (String name, ArrayList<Message> messages, Contact sender)
    {
        this.name = name;
        if (messages != null)
        {
            this.messages = messages;
        }
        else
        {
            this.messages = new ArrayList<>();
        }
        this.sender = sender;
        this.duration = 0;
    }

    public Chat (String name, ArrayList<Message> messages, Contact sender, int duration)
    {
        this.name = name;
        if (messages != null)
        {
            this.messages = messages;
        }
        else
        {
            this.messages = new ArrayList<>();
        }
        this.sender = sender;
        this.duration = duration;
    }

    public Chat (Bundle bundle, Context context)
    {
        this.setID(bundle.getInt("chatID"));
        this.setServerChatID(bundle.getInt("serverChatID"));
        this.setName(bundle.getString("chatname"));
        this.setMessages(bundle.getByteArray("messages"));
        this.setSender(new Contact(bundle, context));
        this.setDuration(bundle.getInt("duration"));
        // TODO: set encryptionssettings
        this.setChatPosition(bundle.getInt("chatPosition"));
    }

    public int getID ()
    {
        return this.ID;
    }

    public void setID (int ID)
    {
        this.ID = ID;
    }

    public int getServerChatID ()
    {
        return this.serverChatID;
    }

    public void setServerChatID (int chatID)
    {
        this.serverChatID = chatID;
    }

    public String getName ()
    {
        return this.name;
    }

    public void setName (String name)
    {
        this.name = name;
    }

    public ArrayList<Message> getMessages ()
    {
        return this.messages;
    }

    public void setMessages (byte[] messages)
    {
        ByteArrayInputStream in = new ByteArrayInputStream(messages);
        ObjectInputStream is = null;
        try
        {
            is = new ObjectInputStream(in);
            this.messages = (ArrayList<Message>) is.readObject();
        }
        catch (IOException e)
        {
            Log.e("", "IOException: " + e.getMessage());
        }
        catch (ClassNotFoundException e)
        {
            Log.e("", "ClassNotFoundException: " + e.getMessage());
        }
        catch (ClassCastException e)
        {
            Log.e("", "ClassCastException: " + e.getMessage());
        }
    }

    public byte[] getMessagesAsByteArray ()
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = null;
        try
        {
            os = new ObjectOutputStream(out);
            os.writeObject(this.messages);
        }
        catch (IOException e)
        {
            Log.e("", "IOException: " + e.getMessage());
        }
        return out.toByteArray();
    }

    public Contact getSender ()
    {
        return this.sender;
    }

    public void setSender (Contact sender)
    {
        this.sender = sender;
    }

    public int getDuration ()
    {
        return this.duration;
    }

    public void setDuration (int duration)
    {
        this.duration = duration;
    }

    public void setMessages (ArrayList<Message> messages)
    {
        this.messages = messages;
    }

    public int getChatPosition ()
    {
        return this.chatPosition;
    }

    public void setChatPosition (int chatPosition)
    {
        this.chatPosition = chatPosition;
    }

    public BasicEncryptionSettings getEncryptionSettings ()
    {
        return this.encryptionSettings;
    }

    public void setEncryptionSettings (BasicEncryptionSettings encryptionSettings)
    {
        this.encryptionSettings = encryptionSettings;
    }

    @Override
    public int compareTo (Object o)
    {
        return this.getName().compareToIgnoreCase(((Chat) o).getName());
    }

    public void sendGUI (ChatItemArrayAdapter adapter, Message msg)
    {
        this.getMessages().add(msg);
        adapter.add(msg);
        // TODO:
    }

    public void receiveGUI (Message msg)
    {
        this.getMessages().add(msg);
        // TODO:
    }

    public void putInBundle (Bundle bundle, Contact contact)
    {
        bundle.putInt("chatID", this.getID());
        bundle.putInt("serverChatID", this.getServerChatID());
        bundle.putString("chatname", this.getName());
        bundle.putByteArray("messages", this.getMessagesAsByteArray());
        contact.putInBundle(bundle);
        bundle.putInt("duration", this.getDuration());
        // TODO: put encryptionsettings
        bundle.putInt("chatPosition", this.getChatPosition());
    }

    public Bundle createBundle (Contact contact)
    {
        Bundle bundle = new Bundle();
        putInBundle(bundle, contact);
        return bundle;
    }

    public BasicChat toBasicChat (Context context)
    {
        return new BasicChat(this.serverChatID,
                Contact.getMe(context).toBasicUser(),
                this.sender.toBasicUser(),
                this.encryptionSettings);
    }
}