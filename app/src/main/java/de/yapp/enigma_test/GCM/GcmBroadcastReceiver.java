package de.yapp.enigma_test.GCM;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Created by MischCon on 29.05.2015.
 */

/*  Only purpose is to receive GCM notifications
*   Handling the notification itsself and starting
*   other tasks based on the content of the notification
*   is the purpose of the GcmMessageHandler class
* */
public class GcmBroadcastReceiver extends WakefulBroadcastReceiver
{
    @Override
    public void onReceive (Context context, Intent intent)
    {

        ComponentName comp = new ComponentName(context.getPackageName(), GcmMessageHandler.class.getName());

        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
    }
}
