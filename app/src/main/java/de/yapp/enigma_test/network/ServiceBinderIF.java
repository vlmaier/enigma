package de.yapp.enigma_test.network;

import java.util.List;

import BasicClasses.BasicChat;
import BasicClasses.BasicContactBook;
import BasicClasses.BasicMessage;
import BasicClasses.BasicUser;

/**
 * Created by MischCon on 31.05.2015.
 */
public interface ServiceBinderIF
{
    public List<BasicUser> getMessage ();

    public void sendMessage (BasicMessage msg);

    public BasicChat getNewChat ();

    public int sendNewChat (BasicChat chat);

    public void abortNewChat (int ChatID);

    public void login (BasicUser me);

    public void logout (BasicUser me);

    public BasicContactBook cbSync (BasicContactBook cb);

    public void updateUser (BasicUser me);
}

