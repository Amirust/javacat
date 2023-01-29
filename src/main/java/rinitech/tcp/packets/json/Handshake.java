package rinitech.tcp.packets.json;

/**
 * Handshake packet sends to the server when the client wants to connect to the server.
 * Data contains the version of the client and the public key of the client.
 * @see HandshakeData
 */
public class Handshake extends BasePackage
{
	public HandshakeData data;
}

