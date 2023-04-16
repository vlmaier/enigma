package de.yapp.enigma_test.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import de.yapp.enigma_test.Chat;
import de.yapp.enigma_test.Contact;
import de.yapp.enigma_test.R;
import de.yapp.enigma_test.RoundedAvatarDrawable;
import de.yapp.enigma_test.adapter.ChatListItemArrayAdapter;
import de.yapp.enigma_test.db.ChatRepository;
import de.yapp.enigma_test.dialog.AddChatDialog;

public class ChatListActivity extends AppCompatActivity
{
    private static ChatListItemArrayAdapter adapter;
    private static Button b_addNewChat;
    private static ListView lv_chats;
    private static Contact contact;

    private AddChatDialog addChatDialog;

    public static ChatRepository chatRep;

    /**
     * data to save in ChatListActivity:
     * integrityCheck := user checked the CheckedTextView or not
     * P.S. other stuff is already saved by using DialogFragment
     *
     * @see <a href="http://developer.android.com/reference/android/app/DialogFragment.html#Lifecycle">check it out</a>
     */
    static boolean integrityCheck = false;

    public static Contact getContact ()
    {
        return contact;
    }

    public static ChatListItemArrayAdapter getAdapter ()
    {
        return adapter;
    }

    public static void setAdapter (ChatListItemArrayAdapter a)
    {
        adapter = a;
    }

    public static Button getButton ()
    {
        return b_addNewChat;
    }

    public static ListView getListView ()
    {
        return lv_chats;
    }

    public static void setIntegrityCheck (boolean value)
    {
        integrityCheck = value;
    }

    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatlist);

        chatRep = new ChatRepository(this);

        lv_chats = (ListView) findViewById(R.id.lv_chats);
        registerForContextMenu(lv_chats);

        b_addNewChat = (Button) findViewById(R.id.b_addNewChat);
        b_addNewChat.setVisibility(View.GONE);

        Bundle bundle = getIntent().getExtras();

        if (contact == null)
        {
            contact = new Contact(bundle, ChatListActivity.this);
        }
        else if (bundle != null)
        {
            if (contact.getID() != bundle.getInt("ID"))
            {
                contact = new Contact(bundle, ChatListActivity.this);
            }
        }

        /**
         * put the picture of contact as Home/Back button
         * fancy stuff right there
         */
        getSupportActionBar().setTitle(contact.getName());
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bitmap b = BitmapFactory.decodeByteArray(contact.getPictureAsByteArray(), 0, contact.getPictureAsByteArray().length);

        /**
         * resize the picture of contact to 128x128 pixels
         * might be big for some smartphones but who cares
         */
        b = Bitmap.createScaledBitmap(b, 128, 128, false);

        getSupportActionBar().setHomeAsUpIndicator(new RoundedAvatarDrawable(b));

        adapter = new ChatListItemArrayAdapter(this, R.layout.chatlist_item, chatRep.getChats(contact));
        lv_chats.setAdapter(adapter);
        lv_chats.setOnItemClickListener(new OnChatListItemClickListener());

        /**
         * create instance of AddChatDialog already here even if the user does not use it
         * needed because reasons
         */
        addChatDialog = new AddChatDialog();

        if (chatRep.getAmountOfChats(contact) == 0)
        {
            b_addNewChat.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCreateContextMenu (ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.contextmenu_chat, menu);
    }

    @Override
    public boolean onContextItemSelected (MenuItem item)
    {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Chat chat = ((Chat) lv_chats.getItemAtPosition(info.position));
        switch (item.getItemId())
        {
            /**
             * called if the user "longtouches" the chatlistitem
             */
            case R.id.cla_action_delete_chat:

                chatRep.deleteChat(chat.getID());

                adapter = new ChatListItemArrayAdapter(this, R.layout.chatlist_item, chatRep.getChats(contact));
                lv_chats.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                if (adapter.getCount() == 0)
                {
                    b_addNewChat.setVisibility(View.VISIBLE);
                }

                Toast.makeText(this, String.format(getString(R.string.action_delete_contact_toast), chat.getName()), Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState (Bundle savedInstanceState)
    {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean("integrityCheck", integrityCheck);
    }

    @Override
    protected void onRestoreInstanceState (Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        integrityCheck = savedInstanceState.getBoolean("integrityCheck");
        /**
         * check only if true:
         * because after the restart the value of CheckedTextView goes to false
         * or the user does not want to do integrityCheck
         */
        if (integrityCheck)
        {
            addChatDialog.setIntegrity_Check(integrityCheck);
        }
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_chatlist, menu);
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
            case R.id.cla_action_add_chat:
                createNewChatDialog();
                return true;
            case R.id.cla_action_settings:
                return true;
            case android.R.id.home:
                Intent i = new Intent(ChatListActivity.this, ContactActivity.class);
                startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * method used in onClick() for addNewChat button
     * appears only if there is no chats yet
     */
    public void createNewChatDialog ()
    {
        addChatDialog.show(getFragmentManager(), "AddChatDialogFragment");
    }

    public void addNewChat (View v)
    {
        createNewChatDialog();
    }

    class OnChatListItemClickListener implements AdapterView.OnItemClickListener
    {
        @Override
        public void onItemClick (AdapterView<?> parent, View view, int position, long id)
        {
            Chat chat = ((Chat) lv_chats.getAdapter().getItem(position));

            Intent intent = new Intent(ChatListActivity.this, ChatActivity.class);
            intent.putExtras(chat.createBundle(contact));
            startActivity(intent);
        }
    }
}