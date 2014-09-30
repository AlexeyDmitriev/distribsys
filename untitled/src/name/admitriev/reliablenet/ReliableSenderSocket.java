package name.admitriev.reliablenet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Random;

public class ReliableSenderSocket {
	private static final Random RANDOM = new Random();
	public static final int KEY_LENGTH = 8;
	public static final int MAX_MESSAGE_LEN = 1000;

	private DatagramSocket socket;

	public ReliableSenderSocket() throws SocketException {
		socket = new DatagramSocket();
	}

	public void send(ReliablePacket packet) throws IOException {
		if (packet.length > MAX_MESSAGE_LEN)
			throw new IllegalArgumentException("Too long packet");

		int length = packet.length;
		byte[] buffer = new byte[1 + KEY_LENGTH + length];

		byte[] key = randomKey();
		System.arraycopy(key, 0, buffer, 1, KEY_LENGTH);
		System.arraycopy(packet.buffer, 0, buffer, 1 + KEY_LENGTH, length);

		DatagramPacket datagram = new DatagramPacket(buffer, 0, buffer.length, packet.address, packet.port);

		socket.setSoTimeout(1000);

		DatagramPacket response = new DatagramPacket(
				new byte[1 + KEY_LENGTH],
				1 + KEY_LENGTH
		);

		while (true) {

			socket.send(datagram);
			System.err.println("sent");

			while (true) {
				try {
					socket.receive(response);
					if (response.getLength() < 1 + KEY_LENGTH) {
						System.err.println("ignore too small");
						continue;
					}
					byte[] responseData = response.getData();
					if (responseData[response.getOffset()] != 1) {
						System.err.println("get not responce");
						continue;
					}

					boolean sameKey = true;
					for (int i = 0; i < KEY_LENGTH; ++i) {
						if (responseData[response.getOffset() + 1 + i] != key[i]) {
							System.err.println("get wrong key");
							sameKey = false;
							break;
						}
					}
					if (sameKey)
						return;
				}
				catch (SocketTimeoutException e) {
					break;
				}
			}
		}
	}

	private static byte[] randomKey() {
		byte[] key = new byte[8];
		RANDOM.nextBytes(key);
		return key;
	}
}
