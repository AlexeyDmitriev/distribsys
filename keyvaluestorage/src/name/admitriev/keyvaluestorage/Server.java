package name.admitriev.keyvaluestorage;


import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class Server extends UnicastRemoteObject implements ServerInterface {

	private CoordinatorInterface coordinator;
	private String name;

	private int lastSeenView = 0;
	private boolean isMaster = false;
	private boolean isBackup = false;
	private ServerInterface backup = null; // not null if we are master AND backup exists
	public HashMap<String, String> data = null; // not null iff we are backup or master
	private boolean backupingInProgress = false; // may be true only if we are master

	public Server(String name, String coordinator) throws RemoteException {
		this.name = name;
		try {
			this.coordinator = (CoordinatorInterface) Naming.lookup(coordinator);
		}
		catch (NotBoundException | MalformedURLException e) {
			throw new RemoteException("Bad server name", e);
		}
	}

	@Override
	public void put(String key, String value) throws RemoteException {
		if (!isMaster) {
			throw new IncorrectOperationException("I'm not a master");
		}
		if(backupingInProgress)
			throw new IncorrectOperationException("It's safer not to change until we have a valid backup");
		data.put(key, value);
		if (backup != null) {
			backup.putBackup(key, value);
		}
	}

	@Override
	public void putBackup(String key, String value) throws RemoteException {
		if (!isBackup) {
			throw new IncorrectOperationException("I am not a backup");
		}
		data.put(key, value);
	}

	@Override
	public String get(String key) throws RemoteException {
		if (!isMaster) {
			throw new IncorrectOperationException("I don't know, I'm not a master");
		}
		return data.get(key);
	}

	public void tick() {
		if(backupingInProgress) {
			return; // Don't confirm until we have valid backup
		}
		try {
			ViewInfo viewInfo = coordinator.ping(lastSeenView, name);
			if(viewInfo.view != lastSeenView) {
				boolean nowMaster = viewInfo.primary.equals(name);
				boolean nowBackup = viewInfo.backup.equals(name);
				if (isMaster && !nowMaster) {
					data = null;
					backup = null;
				}
				else if (nowMaster) {
					if(!isMaster && !isBackup) {
						data = new HashMap<>();
					}
					backupingInProgress = true;
					if(!viewInfo.backup.isEmpty()) {
						backup = getBackupByName(viewInfo.backup);
						for (Map.Entry<String, String> entry : data.entrySet()) {
							backup.putBackup(entry.getKey(), entry.getValue());
						}
					}
					backupingInProgress = false;
				}
				if (nowBackup) {
					//something has changed, and we are backup. Master is going to send valid data now.
					data = new HashMap<>();
				}

				isMaster = nowMaster;
				isBackup = nowBackup;
				lastSeenView = viewInfo.view;
			}
		}
		catch (RemoteException e) {
			// can't do anything useful.
		}
	}

	ServerInterface getBackupByName(String name) throws RemoteException {
		try {
			return (ServerInterface) Naming.lookup(name);
		}
		catch (NotBoundException | MalformedURLException e) {
			throw new RemoteException("Bad server name", e);
		}
	}
}
