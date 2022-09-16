package dao;

public class DvdLibraryDaoException extends Exception {
    //This class inherits all of the capabilities of Exception and then can add in any special features that need to be added.
    //In this case, we want our exception to act just like Exception
    public DvdLibraryDaoException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
