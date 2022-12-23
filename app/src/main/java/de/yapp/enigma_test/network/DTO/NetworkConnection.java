package de.yapp.enigma_test.network.DTO;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import DTO.DTO;

/**
 * Created by MischCon on 31.05.2015.
 */
public class NetworkConnection
{
    Socket s;
    ObjectInputStream in;
    ObjectOutputStream out;

    public NetworkConnection () {}

    public boolean connect (String host, int port) throws IOException
    {
        if (s == null)
        {
            s = new Socket(host, port);

            out = new ObjectOutputStream(s.getOutputStream());
            out.flush();
            in = new ObjectInputStream(s.getInputStream());
        }
        return s.isConnected();
    }

    public void send (DTO dto) throws IOException
    {
        out.writeObject(dto);
        out.flush();

    }

    public Object recv () throws IOException, ClassNotFoundException
    {
        Object obj = in.readObject();

        return obj;
    }

    public boolean disconnect () throws IOException
    {
        in.close();
        out.flush();
        out.close();
        s.close();

        return s.isClosed();
    }
}
