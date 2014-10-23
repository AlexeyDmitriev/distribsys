package name.admitriev.keyvaluestorage;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CoordinatorImplementation extends UnicastRemoteObject implements CoordinatorInterface {
	private long currentTime = 1;

	public static final int DEAD_PINGS = 3;

	private Map<String, Long> aliveServers = new HashMap<String, Long>();
	private boolean masterAcknowledged = false;

	private ViewInfo viewInfo;

	public CoordinatorImplementation() throws RemoteException {
		viewInfo = new ViewInfo();
	}

	@Override
	public ViewInfo ping(int view, String serverName) {

		if(serverName.isEmpty()) {
			throw new IllegalArgumentException("serverName should not be null");
		}

		aliveServers.put(serverName, currentTime);

		if(serverName.equals(viewInfo.primary) && view < viewInfo.view) {
			resetPrimaryServer();
			return viewInfo;
		}



		if(viewInfo.primary.isEmpty()) {
			viewInfo.primary = serverName;
			viewInfo.view += 1;
			masterAcknowledged = false;
		}
		if(viewInfo.backup.isEmpty() && masterAcknowledged && !viewInfo.primary.equals(serverName)) {
			changeBackup();
		}
		if (view == viewInfo.view && serverName.equals(viewInfo.primary))
			masterAcknowledged = true;

		return viewInfo;
	}

	@Override
	public String primary() throws RemoteException {
		return viewInfo.primary;
	}

	public void tick() {

		Iterator<Map.Entry<String, Long>> entries = aliveServers.entrySet().iterator();
		while (entries.hasNext()) {
			Map.Entry<String, Long> entry = entries.next();
			if (entry.getValue() + DEAD_PINGS <= currentTime) {
				entries.remove();
			}
		}
		boolean primaryAlive = aliveServers.containsKey(viewInfo.primary);
		boolean backupAlive = aliveServers.containsKey(viewInfo.backup);
		if(!primaryAlive && !backupAlive) {
			throw new RuntimeException("We are dead");
		}
		if(!primaryAlive)
			resetPrimaryServer();
		if(!viewInfo.backup.isEmpty() && !backupAlive)
			changeBackup();

		++currentTime;
	}

	private void changeBackup() {
		if(masterAcknowledged) {
			viewInfo.backup = "";
			for (String server : aliveServers.keySet()) {
				if (!server.equals(viewInfo.primary)) {
					viewInfo.backup = server;
					break;
				}
			}
			masterAcknowledged = false;
			viewInfo.view += 1;
		}
	}

	private void resetPrimaryServer() {
		if(masterAcknowledged) {
			viewInfo.primary = viewInfo.backup;
			changeBackup();
		}
	}


	// your private members here
}
