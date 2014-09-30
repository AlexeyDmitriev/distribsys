package name.admitriev;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface Noter extends Remote {
	void add(String note) throws RemoteException;
	List<String> allNotes() throws RemoteException;
}
