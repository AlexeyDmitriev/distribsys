package name.admitriev.keyvaluestorage;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInterface extends Remote
{
    public void put(String key, String value) throws RemoteException;
    public void putBackup(String key, String value) throws RemoteException;
    public String get(String key) throws RemoteException;
}
