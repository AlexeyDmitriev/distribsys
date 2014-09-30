package name.admitriev;

import name.admitriev.reliablenet.ReliablePacket;
import name.admitriev.reliablenet.ReliableReceiverSocket;

import java.io.IOException;
import java.net.InetAddress;

public class Server {

	public static final int SERVER_PORT = 53415;

	public void run() throws IOException {

		InetAddress host = InetAddress.getLocalHost();

		ReliableReceiverSocket socket = new ReliableReceiverSocket(host, SERVER_PORT);
		while (true) {
			ReliablePacket packet = socket.receive();
			System.out.println("got message:\n" + new String(packet.getData(), packet.getOffset(), packet.getLength(), "UTF-8") + "\n");
		}
	}

	public static void main(String[] args) throws IOException {
		Server server = new Server();
		server.run();
	}
}
