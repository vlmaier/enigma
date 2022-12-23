package de.yapp.enigma_test.GCM;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import DTO.Types;
import de.yapp.enigma_test.network.NetworkService;

/**
 * Created by MischCon on 29.05.2015.
 */

/*  Purpose is to handle incoming Messages from the GcmBroadcastReceiver
*   Based on the content of the messages the GcmMessageHandler
*   starts other operations inside the actual "main network service"
* */
public class GcmMessageHandler extends IntentService
{

    //DEBUG
    Handler handler;

    public GcmMessageHandler ()
    {
        super("GcmMessageHandler");
    }

    @Override
    public void onCreate ()
    {
        super.onCreate();
        handler = new Handler();
    }

    @Override
    protected void onHandleIntent (Intent intent)
    {
        final Bundle extras = intent.getExtras();

        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        gcm.getMessageType(intent);

        boolean getChat = (extras.get(Types.GCMType.NOTIFICATION_GETNEWCHAT) == "true") ? true : false; //should be changed based on the content of the notification
        boolean getMsg = (extras.get(Types.GCMType.NOTIFICATION_GETMESSAGE) == "true") ? true : false;
        boolean sync = (extras.get(Types.GCMType.NOTIFICATION_CBSYNC) == "true") ? true : false;

        Intent ConnectionServiceIntent = new Intent(getApplicationContext(), NetworkService.class); //Target of the intent (in this case: the ConnectionService service)
        ConnectionServiceIntent.putExtra(Types.GCMType.NOTIFICATION_GETNEWCHAT, getChat); //Contains the server a new Chat?
        ConnectionServiceIntent.putExtra(Types.GCMType.NOTIFICATION_GETMESSAGE, getMsg);  //Contains the server a new Message?
        ConnectionServiceIntent.putExtra(Types.GCMType.NOTIFICATION_CBSYNC, sync);

        //Starts the service and passes the intent to the onStartCommand() method
        //if the Service is already running the intent is still passes to the onStartCommand() method

        startService(ConnectionServiceIntent);

        handler.post(new Runnable()
        {
            public void run ()
            {
                Toast.makeText(getApplicationContext(), extras.getString("title"), Toast.LENGTH_LONG).show();
            }
        });

        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }
}
