package de.yapp.enigma_test.network.DTO;

import BasicClasses.BasicChat;
import BasicClasses.BasicContactBook;
import BasicClasses.BasicMessage;
import BasicClasses.BasicUser;
import DTO.DTO;
import DTO.Types;

/**
 * Created by MischCon on 30.05.2015.
 */
public class toDTO implements ClientRequestIF
{
    /* METHODES */
    @Override
    public DTO getMessage ()
    {
        DTO ret = new DTO();
        ret.RequestType = Types.RequestType.getMessage;
        return ret;
    }

    @Override
    public DTO sendMessage (BasicMessage msg)
    {
        DTO ret = new DTO();
        ret.RequestType = Types.RequestType.sendMessage;

        //Cast subclass to superclass;
        ret.param.put(Types.DataType.Message, (Object) msg);
        return ret;
    }

    @Override
    public DTO getNewChat ()
    {
        DTO ret = new DTO();
        ret.RequestType = Types.RequestType.getNewChat;
        /*
        //Cast subclass to superclass
        BasicChat ChatBasic = Chat;
        ret.param.put(Types.DataType.Chat, (Object) ChatBasic);
        */
        return ret;
    }

    @Override
    public DTO sendNewChat (BasicChat chat)
    {
        DTO ret = new DTO();
        ret.RequestType = Types.RequestType.sendNewChat;
        ret.param.put(Types.DataType.Chat, (Object) chat);
        return ret;
    }

    @Override
    public DTO abortNewChat (int ChatID)
    {
        DTO ret = new DTO();
        ret.RequestType = Types.RequestType.abortNewChat;
        ret.param.put(Types.DataType.ChatID, (Object) ChatID);
        return ret;
    }

    @Override
    public DTO login (BasicUser me)
    {
        DTO ret = new DTO();
        ret.RequestType = Types.RequestType.login;
        ret.param.put(Types.DataType.User, (Object) me);
        return ret;
    }

    @Override
    public DTO logout (BasicUser me)
    {
        DTO ret = new DTO();
        ret.RequestType = Types.RequestType.logout;
        ret.param.put(Types.DataType.User, (Object) me);
        return ret;
    }

    @Override
    public DTO cbSync (BasicContactBook cb)
    {
        DTO ret = new DTO();
        ret.RequestType = Types.RequestType.cbSync;
        ret.param.put(Types.DataType.ContactBook, (Object) cb);
        return ret;
    }

    @Override
    public DTO updateUser (BasicUser me)
    {
        DTO ret = new DTO();
        ret.RequestType = Types.RequestType.updateUser;
        ret.param.put(Types.DataType.User, (Object) me);
        return ret;
    }
}
