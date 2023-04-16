package de.yapp.enigma_test.activity;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import de.yapp.enigma_test.Contact;
import de.yapp.enigma_test.QwazyFunctions;
import de.yapp.enigma_test.R;
import de.yapp.enigma_test.adapter.ContactItemArrayAdapter;
import de.yapp.enigma_test.db.ContactRepository;
import de.yapp.enigma_test.dialog.AddContactDialog;

public class ContactActivity extends AppCompatActivity
{
    private static final int SELECT_PICTURE = 1;

    private static Button b_addNewContact;
    private static ListView lv_contacts;

    private AddContactDialog addContactDialog;
    private ProgressBar pb_import_contacts;

    public static ContactRepository contactRep;

    /**
     * data to save in ContactActivity:
     * bitmapdata := picture (as byte-array) that user took from the gallery for his new contact
     * P.S. other stuff is already saved by using DialogFragment
     *
     * @see <a href="http://developer.android.com/reference/android/app/DialogFragment.html#Lifecycle">check it out</a>
     */
    byte[] bitmapdata;

    public static Button getButton ()
    {
        return b_addNewContact;
    }

    public static ListView getListView ()
    {
        return lv_contacts;
    }

    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        /**
         * get all the contacts that are in the database
         */
        contactRep = new ContactRepository(this);

        getSupportActionBar().setTitle(getString(R.string.ab_contactActivity));

        lv_contacts = (ListView) findViewById(R.id.lv_contacts);
        registerForContextMenu(lv_contacts);

        b_addNewContact = (Button) findViewById(R.id.b_addNewContact);
        b_addNewContact.setVisibility(View.GONE);

        pb_import_contacts = (ProgressBar) findViewById(R.id.pb_import_contacts);
        pb_import_contacts.setVisibility(View.GONE);

        /**
         * create instance of AddContactDialog already here even if the user does not use it
         * needed because reasons
         */
        addContactDialog = new AddContactDialog();

        if (!contactsImportDialog())
        {
            lv_contacts.setAdapter(new ContactItemArrayAdapter(this, R.layout.contact_item, contactRep.getAllContactsAsArray()));
            lv_contacts.setOnItemClickListener(new OnContactItemClickListener());

            if (contactRep.getAmountOfContacts() == 0)
            {
                b_addNewContact.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onCreateContextMenu (ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.contextmenu_contact, menu);
    }

    @Override
    public boolean onContextItemSelected (MenuItem item)
    {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Contact contact = ((Contact) lv_contacts.getItemAtPosition(info.position));
        switch (item.getItemId())
        {
            /**
             * called if the user "longtouches" the contactlistitem
             */
            case R.id.ca_action_delete_contact:
                contactRep.deleteContact(contact.getID());

                ContactItemArrayAdapter adapter = new ContactItemArrayAdapter(this, R.layout.contact_item, contactRep.getAllContactsAsArray());
                lv_contacts.setAdapter(adapter);
                adapter.notifyDataSetChanged();

                Toast.makeText(this, String.format(getString(R.string.action_delete_contact_toast), contact.getName()), Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState (Bundle savedInstanceState)
    {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putByteArray("contactImage", bitmapdata);
    }

    @Override
    protected void onRestoreInstanceState (Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        bitmapdata = savedInstanceState.getByteArray("contactImage");
        if (bitmapdata != null)
        {
            ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(bitmapdata);
            Bitmap bitmap = BitmapFactory.decodeStream(arrayInputStream);
            addContactDialog.setContactImage(new BitmapDrawable(getResources(), bitmap));
        }
    }

    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK)
        {
            if (requestCode == SELECT_PICTURE)
            {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null)
                {
                    InputStream inputStream = null;
                    Drawable picture = getResources().getDrawable(R.drawable.default_user);
                    try
                    {
                        inputStream = getContentResolver().openInputStream(selectedImageUri);
                        picture = Drawable.createFromStream(inputStream, selectedImageUri.toString());

                        Bitmap bitmap = ((BitmapDrawable) picture).getBitmap();
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        bitmapdata = stream.toByteArray();
                    }
                    catch (FileNotFoundException e)
                    {
                        Log.e("", "FileNotFoundException: " + e.getMessage());
                    }
                    finally
                    {
                        addContactDialog.setContactImage(picture);
                    }
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_contact, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item)
    {
        int id = item.getItemId();
        switch (id)
        {
            /**
             * button in the actionbar
             */
            case R.id.ca_action_add_contact:
                addContactDialog.show(getFragmentManager(), "AddContactDialogFragment");
                return true;
            case R.id.ca_action_synchronize:
                new ContactSynchronizeTask().execute();
                return true;
            case R.id.ca_action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * method used in onClick() for addNewContact button
     * appears only if there is no contacts yet
     */
    public void addNewContact (View v)
    {
        addContactDialog.show(getFragmentManager(), "AddContactDialogFragment");
    }

    /**
     * let the user take a picture from gallery
     * onClick() for the Imageview in AddContactDialog
     * SELECT_PICTURE is the requestCode (used in onActivityResult)
     */
    public void choosePicture (View v)
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    public boolean contactsImportDialog ()
    {
        SharedPreferences SP = getSharedPreferences("de.yapp.enigma_test", Context.MODE_PRIVATE);
        /**
         * default value: true, because there will be always a first time
         */
        boolean isFirstRun = SP.getBoolean("isFirstRunForImportContacts", true);
        if (isFirstRun)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setMessage(String.format(getString(R.string.importDialogMessage), getString(R.string.app_name)));

            builder.setPositiveButton(getString(R.string.importDialogPositive), new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick (DialogInterface dialog, int which)
                {
                    new ContactImportTask().execute();
                    dialog.dismiss();
                }
            });

            builder.setNegativeButton(getString(R.string.importDialogNegative), new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick (DialogInterface dialog, int which)
                {
                    b_addNewContact.setVisibility(View.VISIBLE);
                    dialog.dismiss();
                }
            });

            builder.create();
            // TODO: find a solution that works with API level 16 too
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
            builder.show();

            /**
             * no more first time, this code is unique unless you reinstall the app
             */
            SP.edit().putBoolean("isFirstRunForImportContacts", false).apply();

            return true;
        }
        else
        {
            return false;
        }
    }

    public void onPreExecuteTask ()
    {
        pb_import_contacts.setVisibility(View.VISIBLE);
        b_addNewContact.setVisibility(View.GONE);
    }

    public void doInBackgroundTask (String msg)
    {
        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        if (cursor.getCount() > 0)
        {
            while (cursor.moveToNext())
            {
                /**
                 * get all contacts with a phonenumber
                 */
                if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0)
                {
                    Contact contact = new Contact();

                    String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY));
                    String phonenumber = null;
                    String photoID = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_ID));
                    Uri photoUri = null;

                    if (photoID != null)
                    {
                        photoUri = ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, Long.parseLong(photoID));
                    }

                    Drawable picture = getResources().getDrawable(R.drawable.default_user);

                    Cursor phonecursor = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id},
                            null);

                    while (phonecursor.moveToNext())
                    {
                        phonenumber = phonecursor.getString(phonecursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    }
                    phonecursor.close();

                    contact.setName(name);
                    contact.setPhonenumber(QwazyFunctions.preparePhonenumber(phonenumber));

                    try
                    {
                        if (photoUri != null)
                        {
                            InputStream inputStream = getContentResolver().openInputStream(photoUri);
                            picture = Drawable.createFromStream(inputStream, photoUri.toString());
                        }
                    }
                    catch (FileNotFoundException e)
                    {
                        Log.e("", "FileNotFoundException: " + e.getMessage());
                    }

                    contact.setPicture(picture);

                    /**
                     * this function is used by both AsyncTasks
                     * saving on code and shit
                     */
                    switch (msg)
                    {
                        case "import":
                            contactRep.addContact(contact);
                            break;
                        case "synchronize":
                            boolean duplicate = false;

                            if (contactRep.getAllContacts() != null)
                            {
                                for (int i = 0; i < contactRep.getAmountOfContacts(); i++)
                                {
                                    if (contactRep.getAllContacts().get(i).getPhonenumber().equals(contact.getPhonenumber()))
                                    {
                                        duplicate = true;
                                    }
                                }
                            }
                            if (!duplicate) contactRep.addContact(contact);
                            break;
                    }
                }
            }
            cursor.close();
        }
    }

    public void onPostExecuteTask ()
    {
        if (contactRep.getAllContacts() != null)
        {
            lv_contacts.setAdapter(new ContactItemArrayAdapter(ContactActivity.this, R.layout.contact_item, contactRep.getAllContactsAsArray()));
            lv_contacts.setOnItemClickListener(new OnContactItemClickListener());
            pb_import_contacts.setVisibility(View.GONE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
        }
    }

    class ContactImportTask extends AsyncTask<String, Void, Void>
    {
        @Override
        protected void onPreExecute ()
        {
            onPreExecuteTask();
        }

        @Override
        protected Void doInBackground (String... params)
        {
            doInBackgroundTask("import");
            return null;
        }

        @Override
        protected void onPostExecute (Void result)
        {
            onPostExecuteTask();
        }
    }

    class ContactSynchronizeTask extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected void onPreExecute ()
        {
            onPreExecuteTask();
        }

        @Override
        protected Void doInBackground (Void... params)
        {
            doInBackgroundTask("synchronize");
            return null;
        }

        @Override
        protected void onPostExecute (Void result)
        {
            onPostExecuteTask();
        }
    }

    class OnContactItemClickListener implements AdapterView.OnItemClickListener
    {
        @Override
        public void onItemClick (AdapterView<?> parent, View view, int position, long id)
        {
            Contact contact = ((Contact) lv_contacts.getAdapter().getItem(position));

            Intent intent = new Intent(ContactActivity.this, ChatListActivity.class);
            intent.putExtras(contact.createBundle());
            startActivity(intent);
        }
    }
}