package de.yapp.enigma_test;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.io.Serializable;

import BasicClasses.BasicMessage;

public class Message extends BasicMessage implements Serializable
{
    private int serverChatID;
    private Contact userTo;
    private String content;
    /**
     * true   ---> left side of the ListView (receive)
     * false  ---> right side of the ListView (send)
     */
    private boolean orientation;

    public Message () {}

    public Message (BasicMessage msg, Context context)
    {
        SharedPreferences SP = context.getSharedPreferences("de.yapp.enigma_test", Context.MODE_PRIVATE);

        this.serverChatID = msg.ChatId;
        this.userTo = new Contact(msg.UserTo, context);
        this.content = Base64.encodeToString(msg.Message, Base64.DEFAULT);
        if (userTo.PhoneNumber.equals(SP.getString("myPhonenumber", "")))
        {
            this.orientation = false;
        }
        else
        {
            this.orientation = true;
        }
    }

    public Message (String content, boolean orientation, Contact userTo)
    {
        this.content = content;
        this.orientation = orientation;
        this.userTo = userTo;
    }

    public String getContent ()
    {
        return this.content;
    }

    public void setContent (String content)
    {
        this.content = content;
    }

    public boolean getOrientation ()
    {
        return this.orientation;
    }

    public void setOrientation (boolean orientation)
    {
        this.orientation = orientation;
    }

    public BasicMessage toBasicMessage ()
    {
        return new BasicMessage(this.serverChatID, this.userTo.toBasicUser(), this.content.getBytes());
    }
}