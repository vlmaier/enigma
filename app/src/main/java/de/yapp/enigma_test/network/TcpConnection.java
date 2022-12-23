package de.yapp.enigma_test.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * TCP connection class
 * Created by Andreas on 21.05.2015.
 */
@Deprecated
public class TcpConnection
{

    private Socket socket = null;
    private String addr = "";
    private int port = 0;
    private ObjectInputStream in = null;
    private ObjectOutputStream out = null;
    private boolean socketCreated = false;
    private boolean streamsCreated = false;

    public TcpConnection (String addr, int port)
    {
        this.addr = addr;
        this.port = port;

        System.out.println("<<addr: " + addr + ">> <<port: " + port + ">>");

        init();
    }

    public TcpConnection (Socket socket)
    {
        this.socket = socket;

        init();
    }

    private void init ()
    {
        if (createSocket())
        {
            socketCreated = true;
        }

        if (createStreams())
        {
            streamsCreated = true;
        }
    }

    private boolean createSocket ()
    {
        try
        {
            socket = new Socket(addr, port);

            return true;
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());

            return false;
        }
    }

    private boolean createStreams ()
    {
        try
        {
            if (socket != null)
            {
                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());

                return true;
            }
            else
            {
                return false;
            }
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());

            return false;
        }
    }

    public ObjectInputStream getIn ()
    {
        return in;
    }

    public ObjectOutputStream getOut ()
    {
        return out;
    }

    public boolean isSocketCreated ()
    {
        return socketCreated;
    }

    public boolean isStreamsCreated ()
    {
        return streamsCreated;
    }
}
