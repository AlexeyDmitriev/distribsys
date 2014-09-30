package name.admitriev.reliablenet;

import java.net.InetAddress;

public final class ReliablePacket {
	byte[] buffer;
	int offset;
	int length;
	InetAddress address;
	int port;
	public ReliablePacket(byte[] buffer, int offset, int length, InetAddress address, int port) {
		this.buffer = buffer;
		this.offset = offset;
		this.length = length;
		this.address = address;
		this.port = port;
	}

	public byte[] getData() {
		return buffer;
	}

	public int getOffset() {
		return offset;
	}

	public int getLength() {
		return length;
	}

}
