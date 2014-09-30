package name.admitriev.reliablenet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import static name.admitriev.reliablenet.ReliableSenderSocket.KEY_LENGTH;
import static name.admitriev.reliablenet.ReliableSenderSocket.MAX_MESSAGE_LEN;

public class ReliableReceiverSocket {
	private DatagramSocket socket;

	public ReliableReceiverSocket(InetAddress address, int port) throws SocketException {
		socket = new DatagramSocket(port, address);
	}

	public ReliablePacket receive() throws IOException {
		DatagramPacket datagram = new DatagramPacket(
				new byte[1 + KEY_LENGTH + MAX_MESSAGE_LEN],
				1 + KEY_LENGTH + MAX_MESSAGE_LEN
		);
		while (true) {
			socket.receive(datagram);
			if (datagram.getData()[0] != 0) {
				continue;
			}
			if (datagram.getLength() < 1 + KEY_LENGTH) {
				continue;
			}
			datagram.getData()[0] = 1;
			DatagramPacket responce = new DatagramPacket(datagram.getData(), 0, 1 + KEY_LENGTH, datagram.getSocketAddress());
			socket.send(responce);
			return new ReliablePacket(
					datagram.getData(),
					1 + KEY_LENGTH,
					datagram.getLength() - 1 - KEY_LENGTH,
					datagram.getAddress(),
					datagram.getPort()
			);
		}

	}

}
