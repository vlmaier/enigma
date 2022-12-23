package de.yapp.enigma_test.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import de.yapp.enigma_test.Contact;
import de.yapp.enigma_test.R;
import de.yapp.enigma_test.RoundedAvatarDrawable;

public class ContactItemArrayAdapter extends ArrayAdapter<Contact>
{
    private Context context;
    private int layoutResourceID;
    private Contact data[];

    public ContactItemArrayAdapter (Context context, int layoutResourceID, Contact[] data)
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

        Contact contact = data[position];

        //Typeface tf = Typeface.createFromAsset(context.getAssets(), "fonts/RobotoCondensed-Regular.ttf");
        TextView tv_item_text = (TextView) convertView.findViewById(R.id.tv_item_text_ci);
        //tv_item_text.setTypeface(tf);

        /**
         * cut the name of contact if it's too long (too long := > 25)
         */
        if (contact.getName().length() > 25)
        {
            tv_item_text.setText(contact.getName().substring(0, 20) + " ...");
        }
        else
        {
            tv_item_text.setText(contact.getName());
        }

        TextView tv_item_subtext = (TextView) convertView.findViewById(R.id.tv_item_subtext_ci);
        //tv_item_subtext.setTypeface(tf);
        tv_item_subtext.setText(contact.getPhonenumber());

        ImageView iv_item_picture = (ImageView) convertView.findViewById(R.id.iv_item_picture_ci);
        iv_item_picture.setImageDrawable(new RoundedAvatarDrawable(((BitmapDrawable) contact.getPicture()).getBitmap()));

        return convertView;
    }
}