package de.yapp.enigma_test.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import de.yapp.enigma_test.Chat;
import de.yapp.enigma_test.Contact;
import de.yapp.enigma_test.Message;
import de.yapp.enigma_test.R;
import de.yapp.enigma_test.adapter.ChatItemArrayAdapter;
import de.yapp.enigma_test.db.DBHandler;

public class ChatActivity extends AppCompatActivity
{
    public static DBHandler db;
    private static Chat chat;
    private static ChatItemArrayAdapter adapter;
    private EditText et_input;
    private ListView lv_chat;

    public static ChatItemArrayAdapter getAdapter ()
    {
        return adapter;
    }

    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        db = new DBHandler(this);

        lv_chat = (ListView) findViewById(R.id.lv_chat);

        Bundle bundle = getIntent().getExtras();

        if (chat == null)
        {
            chat = new Chat(getIntent().getExtras(), ChatActivity.this);
        }
        else if (bundle != null)
        {
            if (chat.getID() != bundle.getInt("chatID"))
            {
                chat = new Chat(getIntent().getExtras(), ChatActivity.this);
            }
        }

        getSupportActionBar().setTitle(chat.getName());

        adapter = new ChatItemArrayAdapter(getApplicationContext(), R.layout.chat_item);
        lv_chat.setAdapter(adapter);

        if (chat.getMessages() != null)
        {
            for (int i = 0; i < chat.getMessages().size(); i++)
            {
                adapter.add(chat.getMessages().get(i));
            }
        }

        et_input = (EditText) findViewById(R.id.et_input);
        et_input.setOnKeyListener(new View.OnKeyListener()
        {
            public boolean onKey (View v, int keyCode, KeyEvent event)
            {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER))
                {
                    /**
                     * new Contact() here is the owner
                     * saved with attributes of a BasicUser
                     */
                    Message msg = new Message(et_input.getText().toString(), false, Contact.getMe(getApplicationContext()));
                    chat.sendGUI(adapter, msg);
                    // TODO: rethink this one maybe, not that fancy to call for db after every message sent
                    db.addMessage(chat, chat.getMessagesAsByteArray());
                    et_input.setText("");
                    scrollListViewToBottom();
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * used for scrolling the Chat-Listview to the bottom for example after the user input
     */
    private void scrollListViewToBottom ()
    {
        lv_chat.post(new Runnable()
        {
            @Override
            public void run ()
            {
                lv_chat.setSelection(adapter.getCount() - 1);
            }
        });
    }

    @Override
    protected void onSaveInstanceState (Bundle savedInstanceState)
    {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putByteArray("messages", adapter.getMessages());
    }

    @Override
    protected void onRestoreInstanceState (@NonNull Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        adapter.setMessages(savedInstanceState.getByteArray("messages"));
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item)
    {
        int id = item.getItemId();

        switch (id)
        {
            case R.id.action_settings:
                return true;
            /**
             * go back to ChatListActivity
             * used for Notifications and shit
             * because you are going to open the ChatActivity right after the user touches the Notification
             */
            case android.R.id.home:
                Contact contact = chat.getSender();
                Intent intent = new Intent(ChatActivity.this, ChatListActivity.class);
                intent.putExtras(contact.createBundle());
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}