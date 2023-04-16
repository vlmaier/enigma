package de.yapp.enigma_test;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import BasicClasses.BasicUser;
import BasicClasses.hashGeneratorUtils;
import de.yapp.enigma_test.db.ContactRepository;
import de.yapp.enigma_test.db.DBHandler;

public class Contact extends BasicUser implements Comparable
{
    private int ID;
    private String GUID;
    private String name;
    private String phonenumber;
    private Drawable picture;
    private byte[] publicKey;
    private byte[] signKey;
    private String GCMRegID;

    public Contact () {}

    public Contact (BasicUser basicUser, Context context)
    {
        Contact contact = new ContactRepository(context).findContact(hashGeneratorUtils.generateSHA256(basicUser.PhoneNumber));
        if (contact != null)
        {
            this.ID = contact.getID();
            this.GUID = contact.getGUID();
            this.name = contact.getName();
            this.phonenumber = basicUser.PhoneNumber;
            this.picture = contact.getPicture();
            this.publicKey = basicUser.PublicKey;
            this.signKey = basicUser.SignKey;
            this.GCMRegID = basicUser.GcmRegId;
        }
    }

    /**
     * used for creating owner contact
     */
    public Contact (String phonenumber, byte[] publicKey, byte[] signKey, String GCMRegID)
    {
        this.phonenumber = phonenumber;
        this.publicKey = publicKey;
        this.signKey = signKey;
        this.GCMRegID = GCMRegID;
    }

    public Contact (String GUID, String name, String phonenumber, Drawable picture)
    {
        this.GUID = GUID;
        this.name = name;
        this.phonenumber = phonenumber;
        this.picture = picture;
    }

    public Contact (Bundle bundle, Context context)
    {
        this.setID(bundle.getInt("ID"));
        this.setGUID(bundle.getString("GUID"));
        this.setName(bundle.getString("name"));
        this.setPhonenumber(bundle.getString("phonenumber"));
        this.setPicture(context, bundle.getByteArray("picture"));
        this.setPublicKey(bundle.getByteArray("publicKey"));
        this.setSignKey(bundle.getByteArray("signKey"));
        this.setGCMRegID(bundle.getString("GCMRegID"));
    }

    public int getID ()
    {
        return this.ID;
    }

    public void setID (int ID)
    {
        this.ID = ID;
    }

    public String getGUID ()
    {
        return this.GUID;
    }

    public void setGUID (String GUID)
    {
        this.GUID = GUID;
    }

    public String getName ()
    {
        return this.name;
    }

    public void setName (String name)
    {
        this.name = name;
    }

    public String getPhonenumber ()
    {
        return this.phonenumber;
    }

    public void setPhonenumber (String phonenumber)
    {
        this.phonenumber = phonenumber;
    }

    public Drawable getPicture ()
    {
        return this.picture;
    }

    public byte[] getPictureAsByteArray ()
    {
        Bitmap bitmap = ((BitmapDrawable) getPicture()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public void setPicture (Drawable picture)
    {
        this.picture = picture;
    }

    public void setPicture (Context context, byte[] picture)
    {
        ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(picture);
        Bitmap bitmap = BitmapFactory.decodeStream(arrayInputStream);
        this.picture = new BitmapDrawable(context.getResources(), bitmap);
    }

    public byte[] getPublicKey ()
    {
        return this.publicKey;
    }

    public void setPublicKey (byte[] publicKey)
    {
        this.publicKey = publicKey;
    }

    public byte[] getSignKey ()
    {
        return this.signKey;
    }

    public void setSignKey (byte[] signKey)
    {
        this.signKey = signKey;
    }

    public String getGCMRegID ()
    {
        return this.GCMRegID;
    }

    public void setGCMRegID (String GCMRegID)
    {
        this.GCMRegID = GCMRegID;
    }

    @Override
    public int compareTo (@NonNull Object o)
    {
        return this.getName().compareToIgnoreCase(((Contact) o).getName());
    }

    public void putInBundle (Bundle bundle)
    {
        bundle.putInt("ID", this.getID());
        bundle.putString("GUID", this.getGUID());
        bundle.putString("name", this.getName());
        bundle.putString("phonenumber", this.getPhonenumber());
        bundle.putByteArray("picture", this.getPictureAsByteArray());
        bundle.putByteArray("publicKey", this.getPublicKey());
        bundle.putByteArray("signKey", this.getSignKey());
        bundle.putString("GCMRegID", this.getGCMRegID());
    }

    public Bundle createBundle ()
    {
        Bundle bundle = new Bundle();
        putInBundle(bundle);
        return bundle;
    }

    public static Contact getMe (Context context)
    {
        SharedPreferences SP = context.getSharedPreferences("de.yapp.enigma_test", Context.MODE_PRIVATE);
        CryptoPGP key = (CryptoPGP) new DBHandler(context).getKeysTable().get(0);
        Contact me = new Contact();
        try
        {
            // TODO: signKey & GCMRegID
            me = new Contact(SP.getString("myPhonenumber", ""), key.getCurrentPublicKey().getEncoded(), new byte[]{}, SP.getString("", ""));
        }
        catch (Exception e)
        {
            Log.e("", "Exception: " + e.getMessage());
        }
        return me;
    }

    public BasicUser toBasicUser ()
    {
        return new BasicUser(hashGeneratorUtils.generateSHA256(this.phonenumber), this.publicKey, this.signKey, this.GCMRegID);
    }
}