package de.yapp.enigma_test.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.spongycastle.openpgp.PGPException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import BasicClasses.BasicUser;
import de.yapp.enigma_test.Contact;
import de.yapp.enigma_test.CryptoPGP;
import de.yapp.enigma_test.GCM.GcmRegId;
import de.yapp.enigma_test.QwazyFunctions;
import de.yapp.enigma_test.R;
import de.yapp.enigma_test.db.DBHandler;
import de.yapp.enigma_test.network.NetworkService;

public class SplashScreenActivity extends Activity
{
    /**
     * time out for connection
     */
    private final static int TIME_OUT = 5000;
    /**
     * go to build.gradle to change the version
     */
    private static String versionName;

    public boolean serviceConnected = false;
    public boolean regId = false;
    public Intent NetworkServiceIntent;

    /**
     * creates service for TCP connection
     */
    private NetworkService.ActivityBinder binder;

    public NetworkService.ActivityBinder getBinder ()
    {
        return this.binder;
    }

    private ServiceConnection connection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected (ComponentName name, IBinder service)
        {
            binder = (NetworkService.ActivityBinder) service;
            Log.i("DEBUG", "onServiceConnected was called");
            serviceConnected = true;
        }

        @Override
        public void onServiceDisconnected (ComponentName name)
        {
            // binder = null;
            Log.i("DEBUG", "onServiceDisconnected was called"); //??
            serviceConnected = false;
        }
    };

    /**
     * start the network service and bind it to activity
     */
    private void startService ()
    {
        // TCP Intent
        new AsyncTask<Void, Void, Void>()
        {
            @Override
            protected Void doInBackground (Void... params)
            {
                //Intent i = new Intent(SplashScreenActivity.this, NetworkService.class);
                NetworkServiceIntent = new Intent(getApplicationContext(), NetworkService.class);
                Log.i("DEBUG", "Starting binding...");
                //startService(NetworkServiceIntent);
                bindService(NetworkServiceIntent, connection, Context.BIND_AUTO_CREATE);
                //startService(i);
                Log.i("DEBUG", "Bound to Service");

//                try
//                {
//                    Thread.sleep(3000);
//                }
//                catch (InterruptedException e)
//                {
//                    e.printStackTrace();
//                }

                return null;
            }
        }.execute();
    }

    /**
     * get GCMRegId
     */
    private void getRegId ()
    {

        new AsyncTask<Void, Void, String>()
        {

            @Override
            protected String doInBackground (Void... params)
            {
                String regid = "";

                try
                {
                    GcmRegId gcm = new GcmRegId();
                    regid = gcm.getRegistrationId(getApplicationContext());

                    regId = true;
                }
                catch (IOException e)
                {
                    e.printStackTrace();

                    regId = false;
                }

                return regid;
            }
        }.execute(null, null, null);

        Log.i("DEBUG", "SSA onCreate() finished!");
    }

    /**
     * Wait for the gcm reg id and that the service has been started connected
     * TIME_OUT := max wait time for getting gcm reg id and starting the service
     * If max wait time is reached, app will be closed
     */
    private void startContactActivity ()
    {

        new AsyncTask<Void, Void, Void>()
        {
            long start = System.currentTimeMillis();
            long stop = start;

            @Override
            protected Void doInBackground (Void... params)
            {

                while ((stop - start) <= TIME_OUT)
                {
                    if (serviceConnected && regId)
                    {
                        Intent i = new Intent(SplashScreenActivity.this, ContactActivity.class);

                        startActivity(i);

                        /**
                         * for test use only
                         */
                        Log.i("DEBUG", "Successfully created --> new Activity started");
                        System.out.println("<<<<<<<<<<Success");
                        System.out.println(">>>>>SC" + serviceConnected);
                        System.out.println(">>>>>REG" + regId);

                        finish();

                        break;
                    }

                    stop = System.currentTimeMillis();
                }

                if (!regId || !serviceConnected)
                {
                    finish();

                    Log.i("DEBUG", "Error while starting service");
                    Toast.makeText(getApplicationContext(), "Application Error", Toast.LENGTH_LONG).show();
                }

                /**
                 * for test use only
                 */
                BasicUser me = new Contact("+491711267233", new byte[1], new byte[1], "APA91bGhNcoUpXurfeCBxPtA6i-pKfueZaiQwdPoscNGGMxlotKyBo9-mZfNXuP3XTb0bFGTiHYEq7GiDqD_El0bclOUjLmimpgnGWIy0UlvXzApWG54pRqsynHeR6p12Nkx-WgGsM--8uzNm8P-MTZRrUh96XRo9g").toBasicUser();

                /**
                 * for test use only
                 */
                binder.login(me);
                Log.i("DEBUG", "USER LOGIN SENT!");

                return null;
            }

            @Override
            protected void onPostExecute (Void result)
            {
                ProgressBar pb_load = (ProgressBar) findViewById(R.id.pb_load);
                pb_load.setVisibility(View.GONE);
            }
        }.execute();
    }

    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/RobotoCondensed-Light.ttf");

        TextView tv_header = (TextView) findViewById(R.id.tv_header);
        TextView tv_footer = (TextView) findViewById(R.id.tv_footer);

        tv_header.setTypeface(tf);
        tv_footer.setTypeface(tf);

        try
        {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            Log.e("", "NameNotFoundException: " + e.getMessage());
            /**
             * default_version: 1.0
             */
            versionName = getString(R.string.default_version);
        }

        tv_header.setText(versionName);
        tv_footer.setText(getString(R.string.developer));

        //startService();
        getRegId();
        //startContactActivity();
    }

    @Override
    protected void onResume ()
    {
        super.onResume();
        new FirstTimeRunnerTask().execute();
    }

    class FirstTimeRunnerTask extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected void onPreExecute ()
        {

        }

        @Override
        protected Void doInBackground (Void... params)
        {
            SharedPreferences SP = getSharedPreferences("de.yapp.enigma_test", Context.MODE_PRIVATE);
            /**
             * default value: true, because there will be always a first time
             */
            boolean isFirstRun = SP.getBoolean("isFirstRunForGeneratingKeyRing", true);
            if (isFirstRun)
            {
                final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
                try
                {
                    /**
                     * save own phonenumber for BasicClasses and so on
                     * format 0 to +49 and delete blanks
                     */
                    SP.edit().putString("myPhonenumber", QwazyFunctions.preparePhonenumber(tm.getLine1Number())).apply();
                    Log.e("", "own phonenumber: " + SP.getString("myPhonenumber", ""));

                    CryptoPGP key = new CryptoPGP(tm.getLine1Number(), tm.getDeviceId().toCharArray(), 0xc0, 1024);
                    DBHandler db = new DBHandler(getApplicationContext());
                    Log.e("", "before adding to DB: \n" + Base64.encodeToString(key.getCurrentPublicKey().getEncoded(), Base64.DEFAULT));
                    db.addKey(key);
                    Log.e("", "after adding to DB: \n" + Base64.encodeToString(((CryptoPGP) db.getKeysTable().get(0)).getCurrentPublicKey().getEncoded(), Base64.DEFAULT));
                }
                catch (PGPException e)
                {
                    e.printStackTrace();
                }
                catch (NoSuchAlgorithmException e)
                {
                    Log.e("", "NoSuchAlgorithmException: \n" + e.getMessage());
                }
                catch (NoSuchProviderException e)
                {
                    Log.e("", "NoSuchProviderException: \n" + e.getMessage());
                }
                catch (IOException e)
                {
                    Log.e("", "IOException: \n" + e.getMessage());
                }
                catch (Exception e)
                {
                    Log.e("", "Exception: \n" + e.getMessage());
                }
                /**
                 * no more first time, this code is unique unless you reinstall the app
                 */
                SP.edit().putBoolean("isFirstRunForGeneratingKeyRing", false).apply();
            }
            else
            {
                try
                {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e)
                {
                    Log.e("", "InterruptedException: " + e.getMessage());
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute (Void result)
        {
            Intent i = new Intent(SplashScreenActivity.this, ContactActivity.class);
            startActivity(i);
        }
    }

    @Override
    protected void onDestroy ()
    {
        super.onDestroy();
        //unbindService(connection);
        //stopService(NetworkServiceIntent);
    }
}