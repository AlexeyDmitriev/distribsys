package name.admitriev;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;

public class NoterServer {
	public static void main(String[] args) throws RemoteException, MalformedURLException {
		NoterImpl noterImpl = new NoterImpl();
		//LocateRegistry.createRegistry(80);
		Naming.rebind("noter", noterImpl);
	}
}
