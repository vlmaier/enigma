package de.yapp.enigma_test.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import de.yapp.enigma_test.Chat;
import de.yapp.enigma_test.R;

public class ChatListItemArrayAdapter extends ArrayAdapter<Chat>
{
    private Context context;
    private int layoutResourceID;
    private Chat data[];

    public ChatListItemArrayAdapter (Context context, int layoutResourceID, Chat[] data)
    {
        super(context, layoutResourceID, data);

        this.context = context;
        this.layoutResourceID = layoutResourceID;
        this.data = data;
    }

    @Override
    public View getView (int position, View convertView, ViewGroup parent)
    {
        if (convertView == null)
        {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceID, parent, false);
        }

        Chat chat = data[position];

        //Typeface tf = Typeface.createFromAsset(context.getAssets(), "fonts/RobotoCondensed-Regular.ttf");
        TextView tv_item_text = (TextView) convertView.findViewById(R.id.tv_item_text_cli);
        //tv_item_text.setTypeface(tf);
        tv_item_text.setText(chat.getName());

        TextView tv_item_subtext = (TextView) convertView.findViewById(R.id.tv_item_subtext_cli);
        //tv_item_subtext.setTypeface(tf);
        tv_item_subtext.setText(chat.getDuration() == -1 ? context.getString(R.string.noDuration) : String.valueOf(chat.getDuration()));

        return convertView;
    }
}