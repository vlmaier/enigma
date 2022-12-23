package de.yapp.enigma_test;

/**
 * some functions used anywhere in the application
 */
public class QwazyFunctions
{
    private QwazyFunctions () {}

    public static String preparePhonenumber (String phonenumber)
    {
        phonenumber = phonenumber.replace(" ", "");
        if (phonenumber.charAt(0) == '0')
        {
            return phonenumber.replaceFirst("0", "+49");
        }
        else
        {
            return phonenumber;
        }
    }
}
