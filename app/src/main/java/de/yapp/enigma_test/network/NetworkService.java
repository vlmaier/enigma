package de.yapp.enigma_test.network;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.util.List;

import BasicClasses.BasicChat;
import BasicClasses.BasicContactBook;
import BasicClasses.BasicMessage;
import BasicClasses.BasicUser;
import de.yapp.enigma_test.network.DTO.NetworkConnection;
import de.yapp.enigma_test.network.DTO.toDTO;

/**
 * Created by MischCon on 30.05.2015.
 */
public class NetworkService extends Service
{
    NetworkConnection ns;
    final static String host = "";
    final static int port = 1337;

    ActivityBinder binder = new ActivityBinder();

    @Override
    public void onCreate ()
    {
        Log.i("DEBUG", "NetworkService -> onCreate was called");

        new AsyncTask<Void, Void, Void>()
        {

            @Override
            protected Void doInBackground (Void... params)
            {
                try
                {
                    //TODO <-- SERVICE STARTED!!!!! NETWORK MAINTHREAD EXCEPTION
                    if (ns == null)
                    {
                        ns = new NetworkConnection();
                    }
                    Log.i("DEBUG", "NetworkService -> NetworkConnection Object created!");
                    Log.i("DEBUG", "NetworkService -> connected: " + ns.connect(host, port));

                }
                catch (IOException e)
                {
            /*  TODO
            *   Network Exception Handling / IO Exception Handling
            * */
                    e.printStackTrace();
                }
                return null;
            }

            ;
        }.execute(null, null, null);

    }

    @Override
    public void onDestroy ()
    {
        Log.i("DEBUG", "NetworkService -> onDestroy() has been called");
        try
        {
            if (ns != null)
            {
                ns.disconnect();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId)
    {

        /*  If the Service has been started by the GcmMessageHandler:
                -> Perform the action specified in the intent
                -> Send Notification to the User
                -> (?) Send Data to the Activity
                -> return SERVICE.START_NOT_STICKY
        *   If the Service has been started by the Activity -> return SERVICE.START_STICKY (Activity closed -> stopService())
        * */
        Log.i("DEBUG", "NetworkService -> onStartCommand() has been called");
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind (Intent intent)
    {
        Log.i("DEBUG", "NetworkService -> onBind() has been called");
        return binder;
    }

    public class ActivityBinder extends Binder implements ServiceBinderIF
    {
        toDTO dto = new toDTO();

        //DEBUG //TODO
        public NetworkService getService ()
        {
            return NetworkService.this;
        }

        @Override
        public List<BasicUser> getMessage ()
        {
            try
            {
                ns.send(dto.getMessage());
                return (List<BasicUser>) ns.recv();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            catch (ClassNotFoundException e)
            {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public void sendMessage (BasicMessage msg)
        {
            try
            {
                ns.send(dto.sendMessage(msg));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        @Override
        public BasicChat getNewChat ()
        {
            try
            {
                ns.send(dto.getNewChat());
                return (BasicChat) ns.recv();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            catch (ClassNotFoundException e)
            {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public int sendNewChat (BasicChat chat)
        {
            try
            {
                ns.send(dto.sendNewChat(chat));
                return (int) ns.recv();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            catch (ClassNotFoundException e)
            {
                e.printStackTrace();
            }
            return 0;
        }

        @Override
        public void abortNewChat (int ChatID)
        {
            try
            {
                ns.send(dto.abortNewChat(ChatID));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        @Override
        public void login (BasicUser me)
        {
            try
            {
                ns.send(dto.login(me));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        @Override
        public void logout (BasicUser me)
        {
            try
            {
                ns.send(dto.logout(me));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        @Override
        public BasicContactBook cbSync (BasicContactBook cb)
        {
            try
            {
                ns.send(dto.cbSync(cb));
                return (BasicContactBook) ns.recv();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            catch (ClassNotFoundException e)
            {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        public void updateUser (BasicUser me)
        {
            try
            {
                ns.send(dto.updateUser(me));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
