package de.yapp.enigma_test.adapter;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import de.yapp.enigma_test.Message;
import de.yapp.enigma_test.R;

public class ChatItemArrayAdapter extends ArrayAdapter<Message>
{
    private ArrayList<Message> messages = new ArrayList<Message>();

    public ChatItemArrayAdapter (Context context, int layoutResourceID)
    {
        super(context, layoutResourceID);
    }

    @Override
    public void add (Message object)
    {
        messages.add(object);
        super.add(object);
    }

    public int getCount ()
    {
        return this.messages.size();
    }

    public Message getItem (int index)
    {
        return this.messages.get(index);
    }

    public byte[] getMessages ()
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

    public View getView (int position, View convertView, ViewGroup parent)
    {
        if (convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.chat_item, parent, false);
        }

        Message message = getItem(position);

        TextView tv_message = (TextView) convertView.findViewById(R.id.tv_message);
        tv_message.setText(message.getContent());
        tv_message.setBackgroundResource(message.getOrientation() ? R.drawable.bubble_odd : R.drawable.bubble);

        LinearLayout wrapper = (LinearLayout) convertView.findViewById(R.id.wrapper);
        wrapper.setGravity(message.getOrientation() ? Gravity.LEFT : Gravity.RIGHT);

        return convertView;
    }
}