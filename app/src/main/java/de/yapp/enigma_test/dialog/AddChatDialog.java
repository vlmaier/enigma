package de.yapp.enigma_test.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import de.yapp.enigma_test.Chat;
import de.yapp.enigma_test.R;
import de.yapp.enigma_test.activity.ChatListActivity;
import de.yapp.enigma_test.adapter.ChatListItemArrayAdapter;

public class AddChatDialog extends DialogFragment
{
    private static CheckedTextView ctv_integrity_check;
    private AlertDialog dialog;
    private EditText et_chatName;
    private EditText et_duration;
    private RadioButton rb_default;
    private RadioButton rb_new;

    private boolean shown = false;

    public AddChatDialog () {}

    @Override
    public Dialog onCreateDialog (Bundle savedInstanceState)
    {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_newchat, null);

        TextView tv_dialog_header = (TextView) view.findViewById(R.id.tv_dialog_newchat_header);
        rb_default = (RadioButton) view.findViewById(R.id.rb_default);
        rb_new = (RadioButton) view.findViewById(R.id.rb_new);
        et_chatName = (EditText) view.findViewById(R.id.et_chatName);
        et_duration = (EditText) view.findViewById(R.id.et_duration);

        rb_default.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick (View v)
            {
                if (rb_default.isChecked())
                {
                    rb_new.setChecked(false);
                    // TODO:
                }
            }
        });

        rb_new.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick (View v)
            {
                if (rb_new.isChecked())
                {
                    rb_default.setChecked(false);
                    // TODO:
                }
            }
        });

        tv_dialog_header.setText(String.format(getString(R.string.newChatDialogMessage), ChatListActivity.getContact().getName()));

        ctv_integrity_check = (CheckedTextView) view.findViewById(R.id.ctv_integrity_check);
        ctv_integrity_check.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick (View v)
            {
                ((CheckedTextView) v).toggle();
                ChatListActivity.setIntegrityCheck(((CheckedTextView) v).isChecked());
            }
        });

        /**
         * ♪ ♫ ♩ ♬ .. you spin my head right round right round ... ♪ ♫ ♩ ♬
         */
        Spinner s_compressionAlgCrypt = (Spinner) view.findViewById(R.id.s_compressionAlgCrypt);
        Spinner s_hashAlgCrypt = (Spinner) view.findViewById(R.id.s_hashAlgCrypt);
        Spinner s_compressionAlgKey = (Spinner) view.findViewById(R.id.s_compressionAlgKey);
        Spinner s_hashAlgKey = (Spinner) view.findViewById(R.id.s_hashAlgKey);
        Spinner s_keyLength = (Spinner) view.findViewById(R.id.s_keyLength);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.spinner_test_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        s_compressionAlgCrypt.setAdapter(adapter);
        s_hashAlgCrypt.setAdapter(adapter);
        s_compressionAlgKey.setAdapter(adapter);
        s_hashAlgKey.setAdapter(adapter);
        s_keyLength.setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setView(view);

        builder.setPositiveButton(getString(R.string.newChatDialogPositive), new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick (DialogInterface dialog, int id)
            {
                Chat chat = new Chat(et_chatName.getText().toString().trim(),
                        null,
                        ChatListActivity.getContact(),
                        et_duration.getText().toString().trim().length() != 0 ? Integer.parseInt(et_duration.getText().toString().trim()) : -1);
                ChatListActivity.chatRep.addChat(chat);

                ChatListActivity.setAdapter(new ChatListItemArrayAdapter(getActivity(), R.layout.chatlist_item, ChatListActivity.chatRep.getChats(ChatListActivity.getContact())));
                ChatListActivity.getListView().setAdapter(ChatListActivity.getAdapter());
                chat.setChatPosition(ChatListActivity.getListView().getAdapter().getCount());
                ChatListActivity.getButton().setVisibility(View.GONE);
            }
        });

        builder.setNegativeButton(getString(R.string.newChatDialogNegative), new DialogInterface.OnClickListener()
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

        if (et_chatName.getText().toString().trim().length() > 0)
        {

        }
        else
        {
            dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
        }

        et_chatName.addTextChangedListener(new TextWatcher()
        {
            public void afterTextChanged (Editable s)
            {
                if (s.length() > 0)
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

        et_duration.setOnKeyListener(new View.OnKeyListener()
        {
            public boolean onKey (View v, int keyCode, KeyEvent event)
            {
                if (keyCode == KeyEvent.KEYCODE_0)
                {
                    if (et_duration.getText().toString().trim().equals("0"))
                    {
                        et_duration.setText("");
                        return true;
                    }
                }
                return false;
            }
        });
    }

    @Override
    public void show (@NonNull FragmentManager manager, String tag)
    {
        if (shown) return;
        super.show(manager, tag);
        shown = true;
    }

    @Override
    public void onDismiss (DialogInterface dialog)
    {
        shown = false;
        super.onDismiss(dialog);
    }

    public boolean isShown ()
    {
        return shown;
    }

    public boolean getIntegrity_Check ()
    {
        return ctv_integrity_check.isChecked();
    }

    public void setIntegrity_Check (boolean value)
    {
        ctv_integrity_check.setChecked(value);
    }
}