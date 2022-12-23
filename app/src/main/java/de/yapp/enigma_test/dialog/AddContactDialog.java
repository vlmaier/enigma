package de.yapp.enigma_test.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.DialogInterface;
import android.content.OperationApplicationException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import de.yapp.enigma_test.Contact;
import de.yapp.enigma_test.QwazyFunctions;
import de.yapp.enigma_test.R;
import de.yapp.enigma_test.activity.ContactActivity;
import de.yapp.enigma_test.adapter.ContactItemArrayAdapter;

public class AddContactDialog extends DialogFragment
{
    private static ImageView iv_contactImage;
    private AlertDialog dialog;
    private EditText et_contactName;
    private EditText et_phonenumber;

    public AddContactDialog () {}

    @Override
    public Dialog onCreateDialog (Bundle savedInstanceState)
    {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_newcontact, null);

        TextView tv_dialog_header = (TextView) view.findViewById(R.id.tv_dialog_newcontact_header);
        et_contactName = (EditText) view.findViewById(R.id.et_contactName);
        et_phonenumber = (EditText) view.findViewById(R.id.et_phonenumber);
        iv_contactImage = (ImageView) view.findViewById(R.id.iv_contactImage);

        tv_dialog_header.setText(getString(R.string.newContactDialogMessage));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setView(view);

        builder.setPositiveButton(getString(R.string.newContactDialogPositive), new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick (DialogInterface dialog, int id)
            {
                Contact contact = new Contact("",
                        et_contactName.getText().toString().trim(),
                        QwazyFunctions.preparePhonenumber(et_phonenumber.getText().toString().trim()),
                        iv_contactImage.getDrawable());

                ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
                int rawContactInsertIndex = ops.size();

                ops.add(ContentProviderOperation
                        .newInsert(ContactsContract.RawContacts.CONTENT_URI)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                        .build());
                ops.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contact.getName())
                        .build());
                ops.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contact.getPhonenumber())
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                        .build());
                ops.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, contact.getPictureAsByteArray())
                        .build());
                try
                {
                    ContentProviderResult[] res = getActivity().getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                }
                catch (RemoteException e)
                {
                    Log.e("", "RemoteException: " + e.getMessage());
                }
                catch (OperationApplicationException e)
                {
                    Log.e("", "OperationApplicationException: " + e.getMessage());
                }

                ContactActivity.contactRep.addContact(contact);
                ContactActivity.getListView().setAdapter(new ContactItemArrayAdapter(getActivity(), R.layout.contact_item, ContactActivity.contactRep.getAllContactsAsArray()));
                ContactActivity.getButton().setVisibility(View.GONE);

                /**
                 * scroll to the contact that was added
                 * for userbility and shit
                 */
                for (int i = 0; i < ContactActivity.getListView().getAdapter().getCount(); i++)
                {
                    if (((Contact) ContactActivity.getListView().getAdapter().getItem(i)).getID() == contact.getID())
                    {
                        final int contactPosition = i;
                        ContactActivity.getListView().post(new Runnable()
                        {
                            @Override
                            public void run ()
                            {
                                ContactActivity.getListView().setSelection(contactPosition);
                            }
                        });
                        break;
                    }
                }
            }
        });

        builder.setNegativeButton(getString(R.string.newContactDialogNegative), new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick (DialogInterface dialog, int id)
            {
                dialog.dismiss();
            }
        });

        dialog = builder.create();
        return dialog;
    }

    @Override
    public void onStart ()
    {
        super.onStart();

        if (et_contactName.getText().toString().trim().length() > 0 && et_phonenumber.getText().toString().trim().length() > 0)
        {

        }
        else
        {
            dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
        }

        et_contactName.addTextChangedListener(new TextWatcher()
        {
            public void afterTextChanged (Editable s)
            {
                if (s.length() > 0 && et_phonenumber.getText().toString().trim().length() > 0)
                {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
                else
                {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }
            }

            /**
             * these two methods need to be here
             * the TextWatcher seems to get bitchy if you just delete them
             */
            public void beforeTextChanged (CharSequence s, int start, int count, int after) { }

            public void onTextChanged (CharSequence s, int start, int before, int count) { }
        });

        et_phonenumber.addTextChangedListener(new TextWatcher()
        {
            public void afterTextChanged (Editable s)
            {
                if (s.length() > 0 && et_contactName.getText().toString().trim().length() > 0)
                {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
                else
                {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }
            }

            /**
             * these two methods need to be here
             * the TextWatcher seems to get bitchy if you just delete them
             */
            public void beforeTextChanged (CharSequence s, int start, int count, int after) { }

            public void onTextChanged (CharSequence s, int start, int before, int count) { }
        });
    }

    public void setContactImage (Drawable picture)
    {
        iv_contactImage.setImageDrawable(picture);
    }
}