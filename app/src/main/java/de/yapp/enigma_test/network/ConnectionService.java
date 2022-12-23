package de.yapp.enigma_test.network;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import de.yapp.enigma_test.Message;

/**
 * Service for TCP connections
 * Created by Andreas on 21.05.2015.
 */
@Deprecated
public class ConnectionService extends Service
{

    private MyBinder mBinder = new MyBinder(); // Binder given to clients
    private String addr = "";
    private int port = 0;
    private TcpConnection conn = null;
    private ObjectInputStream in = null;
    private ObjectOutputStream out = null;
    private Context context = null;

    @Override
    public IBinder onBind (Intent intent)
    {
        return mBinder;
    }

    /**
     * Class used for the client Binder.
     * This service always runs in the same process as its clients, no need to deal with IPC.
     */
    public class MyBinder extends Binder
    {

        public ConnectionService getService ()
        {
            // Return this instance of LocalService so clients can call public methods
            return ConnectionService.this;
        }

        public void send (Message m)
        {
            sendMessage(m);
        }

        public void receive ()
        {
            receiveMessage();
        }
    }

    private void showToast (String msg)
    {
        Looper.prepare();
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        Looper.loop();
    }

    //Muss nur gestartet werden, wenn etwas gesendet werden soll
    private Runnable runnableSend = new Runnable()
    {
        @Override
        public void run ()
        {
            if (conn.isStreamsCreated())
            {
                out = conn.getOut();

                while (true)
                {

                    if (true)
                    { //noch �ndern...
                        System.out.println("Send message...");

                        // out.writeObject();
                        // out.flush();

                        // Thread sleeps before asking for another incoming message
                        try
                        {
                            Thread.sleep(10000);
                        }
                        catch (InterruptedException e)
                        {
                            System.out.println(e.getMessage());
                        }
                    }
                }
            }
            else
            {
                showToast("Error while sending!");
            }
        }
    };

    private Runnable runnableReceive = new Runnable()
    {
        final int waitTimeMilis = 1000;

        @Override
        public void run ()
        {
            if (conn.isStreamsCreated())
            {
                in = conn.getIn();

                while (true)
                {

                    // in.readObject();

                    try
                    {
                        Thread.sleep(waitTimeMilis);
                    }
                    catch (InterruptedException e)
                    {
                        System.out.println(e.getMessage());
                    }

                    System.out.println("Asking for received messages...");
                }
            }
            else
            {
                showToast("Error while receiving");
            }
        }
    };

    private Runnable startConnection = new Runnable()
    {
        @Override
        public void run ()
        {
            conn = new TcpConnection(addr, port);

            if (conn != null)
            {
                new Thread(runnableSend).start();
                new Thread(runnableReceive).start();
            }
            else
            {
                showToast("Error while creating socket connection");
            }
        }
    };

    @Override
    public int onStartCommand (Intent intent, int flags, int startId)
    {
        System.out.println(">>>>>>>>>Received start id " + startId + ": " + intent);

        if (intent != null)
        {
            addr = intent.getExtras().getString("addr");
            port = intent.getExtras().getInt("port");
        }

        // Needed to send Toast messages to GUI
        if (context != null)
        {
            context = getApplicationContext();
        }

        // Start new threads and the work there
        new Thread(startConnection).start();

        // Service has to continue running until it is explicitly stopped, so return sticky.
        return START_STICKY;
    }

    private void sendMessage (Message m)
    {
    }

    private void receiveMessage ()
    {

    }
}
