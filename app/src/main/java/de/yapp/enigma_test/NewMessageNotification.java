package de.yapp.enigma_test;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;

import de.yapp.enigma_test.activity.ChatActivity;
import de.yapp.enigma_test.db.ChatRepository;
import de.yapp.enigma_test.db.ContactRepository;
import de.yapp.enigma_test.db.DBHandler;

/**
 * ignore this Notification class
 * works only if you are going to fire Notification from an activity
 * you do not have an activity if the app is closed but the service got some messages for ya
 */
public class NewMessageNotification extends Notification
{
    public static DBHandler db;
    public static ContactRepository contactRep;
    public static ChatRepository chatRep;
    private int notificationID;
    private Context fromActivity;
    private Contact contact;
    private Chat chat;
    private Message msg;

    public NewMessageNotification (int notificationID, Context fromActivity, String msg, String phonenumber, int chatPosition)
    {
        db = new DBHandler(fromActivity);
        contactRep = new ContactRepository(fromActivity);
        chatRep = new ChatRepository(fromActivity);

        this.notificationID = notificationID;
        this.fromActivity = fromActivity;

        for (int i = 0; i < contactRep.getAmountOfContacts(); i++)
        {
            if (contactRep.getAllContacts().get(i).getPhonenumber().contains(phonenumber))
            {
                this.contact = contactRep.getAllContacts().get(i);
            }
        }
        this.chat = chatRep.getChats(contact)[chatPosition];
        this.msg = new Message(msg, true, contact);
    }

    public void showNotification ()
    {
        chat.receiveGUI(msg);
        db.addMessage(chat, chat.getMessagesAsByteArray());

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Intent intent = new Intent(this.fromActivity, ChatActivity.class);
        intent.putExtras(chat.createBundle(contact));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(fromActivity);
        stackBuilder.addParentStack(ChatActivity.class);
        stackBuilder.addNextIntent(intent);

        PendingIntent pIntent = PendingIntent.getActivity(this.fromActivity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Bitmap b = BitmapFactory.decodeByteArray(contact.getPictureAsByteArray(), 0, contact.getPictureAsByteArray().length);
        b = Bitmap.createScaledBitmap(b, 128, 128, false);
        b = new RoundedAvatarDrawable(b).getBitmap();

        // TODO: b should be a rounded avatar image, however it shows up as a square

        Notification mNotification = new Notification.Builder(this.fromActivity)
                .setContentTitle(String.format(fromActivity.getString(R.string.notificationMessage), this.contact.getName()))
                .setContentText(this.msg.getContent())
                .setLargeIcon(b)
                .setSmallIcon(R.drawable.secret)
                .setContentIntent(pIntent)
                .setSound(soundUri)
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager = (NotificationManager) this.fromActivity.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(this.notificationID, mNotification);
    }
}
