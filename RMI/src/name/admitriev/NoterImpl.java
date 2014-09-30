package name.admitriev;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class NoterImpl extends UnicastRemoteObject implements Noter {

	List<String> strings = new ArrayList<String>();
	public NoterImpl() throws RemoteException {

	}


	@Override
	public void add(String note) throws RemoteException {
		strings.add(note);
	}

	@Override
	public List<String> allNotes() throws RemoteException {
		return strings;
	}
}
