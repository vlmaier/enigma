package de.yapp.enigma_test.network.DTO;

import BasicClasses.BasicChat;
import BasicClasses.BasicContactBook;
import BasicClasses.BasicMessage;
import BasicClasses.BasicUser;
import DTO.DTO;

/**
 * Created by MischCon on 30.05.2015.
 */
public interface ClientRequestIF
{
    public DTO getMessage ();

    public DTO sendMessage (BasicMessage msg);

    public DTO getNewChat ();

    public DTO sendNewChat (BasicChat chat);

    public DTO abortNewChat (int ChatID);

    public DTO login (BasicUser me);

    public DTO logout (BasicUser me);

    public DTO cbSync (BasicContactBook contactBook);

    public DTO updateUser (BasicUser me);
}
