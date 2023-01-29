package rinitech.tcp.handlers;

import rinitech.tcp.Client;
import rinitech.tcp.Server;
import rinitech.tcp.Utils;
import rinitech.tcp.packets.MCPPacket;
import rinitech.tcp.packets.json.Handshake;
import rinitech.tcp.types.*;

import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class HandleIncomingPacket
{
	public static void handle(MCPPacket packet, Client client, Server server) throws NoSuchAlgorithmException, InvalidKeySpecException
	{
		switch (packet.getMajorPacketType()) {
			case Handshake -> handleHandshake(packet, client, server);
			case Authentication -> handleAuthentication(packet, client, server);
			case Message -> handleMessage(packet, client, server);
			case Room -> handleRoom(packet, client, server);
			case Error -> handleError(packet, client, server);
		}
	}

	private static void handleHandshake(MCPPacket packet, Client client, Server server) throws NoSuchAlgorithmException, InvalidKeySpecException
	{
		if (packet.getMinorPacketType().equals(HandshakePacketType.Handshake)) {
			if (client.status == ClientStatus.Awaiting) {
				client.status = ClientStatus.Handshake;
				Handshake handshake = (Handshake) packet.getData();
				String hexPublicKey = handshake.data.publicKey;
				byte[] publicKey = Utils.hexStringToByteArray(hexPublicKey);
				byte[] sharedKey = server.DH.generateSharedSecret(publicKey);
				String base64SharedKey = Base64.getEncoder().encodeToString(sharedKey);
				if (base64SharedKey.length() > 32) base64SharedKey = base64SharedKey.substring(0, 32);
				SecretKey secretKey = Utils.generateSecretKey(base64SharedKey);

				client.setSecretKey(secretKey);
			}
		}
	}

	private static void handleAuthentication(MCPPacket packet, Client client, Server server)
	{
		// TODO: Implement
	}

	private static void handleMessage(MCPPacket packet, Client client, Server server)
	{
		// TODO: Implement
	}

	private static void handleRoom(MCPPacket packet, Client client, Server server)
	{
		// TODO: Implement
	}

	private static void handleError(MCPPacket packet, Client client, Server server)
	{
		// TODO: Implement
	}
}
