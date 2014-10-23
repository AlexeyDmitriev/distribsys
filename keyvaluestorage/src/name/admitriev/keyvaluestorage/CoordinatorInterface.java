package name.admitriev.keyvaluestorage;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CoordinatorInterface extends Remote
{
	ViewInfo ping(int view, String serverName) throws RemoteException;
	String primary() throws RemoteException;
}
