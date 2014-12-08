package name.admitriev.keyvaluestorage;

public class IncorrectOperationException extends RuntimeException
{
    IncorrectOperationException(String message)
    {
        super(message);
    }
}
