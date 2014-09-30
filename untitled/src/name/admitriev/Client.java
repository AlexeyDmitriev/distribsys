package name.admitriev;

import name.admitriev.reliablenet.ReliablePacket;
import name.admitriev.reliablenet.ReliableSenderSocket;

import java.io.IOException;
import java.net.InetAddress;

public class Client {
	public static void main(String[] args) throws IOException {
		InetAddress localHost = InetAddress.getLocalHost();

		ReliableSenderSocket socket = new ReliableSenderSocket();



		for(int i = 0; i < 10; ++i) {
			String s = "it's random message number " + i;
			byte[] bytes = s.getBytes("UTF-8");
			socket.send(new ReliablePacket(bytes, 0, s.length(), localHost, Server.SERVER_PORT));
		}
	}
}
