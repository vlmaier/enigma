package de.yapp.enigma_test.GCM;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

import DTO.Types;

/**
 * Created by MischCon on 30.05.2015.
 */
public class GcmRegId
{
    public static final String PROPERTY_REG_ID = "registration_id";

    GoogleCloudMessaging gcm;

    /*  Call this to get the current RegistrationID / get a new RegistrationID
    *   if there is no;
    *   TODO
    *   IF YOU OBTAIN A NEW REGISTRATIONID YOU NEED TO INFORM THE BACKEND ABOUT THAT!
    * */
    public String getRegistrationId (Context context) throws IOException
    {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");

        if (registrationId.isEmpty())
        {
            if (gcm == null)
            {
                gcm = GoogleCloudMessaging.getInstance(context);
            }
            registrationId = gcm.register(Types.GCMType.GCM_PROJECT_ID);

            /*save in SharePreferences*/
            prefs.edit().putString(PROPERTY_REG_ID, registrationId).apply();

            //TODO: INFORM BACKEND! (e.g. via the "login() method")
        }

        Log.i("GcmRegId", registrationId);
        return registrationId;
    }

    /*  Returns the SharedPreferences (that contain the current RegId - if it exists)    *
    * */
    private SharedPreferences getGCMPreferences (Context context)
    {
        return context.getSharedPreferences(GcmRegId.class.getSimpleName(), Context.MODE_PRIVATE);
    }
}
