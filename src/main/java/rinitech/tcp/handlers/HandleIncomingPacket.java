package rinitech.tcp.handlers;

import rinitech.database.types.Room;
import rinitech.database.types.User;
import rinitech.tcp.Client;
import rinitech.tcp.Server;
import rinitech.tcp.Utils;
import rinitech.tcp.errors.PacketDataIncorrect;
import rinitech.tcp.errors.UnsupportedVersion;
import rinitech.tcp.packets.MCPPacket;
import rinitech.tcp.packets.json.*;

import static rinitech.tcp.types.AuthenticationPacketType.*;

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

	private static void handleHandshake(MCPPacket packet, Client client, Server server)
	{
		if (packet.getMinorPacketType().equals(HandshakePacketType.Handshake)) {
			if (client.status == ClientStatus.Awaiting) {
				client.status = ClientStatus.Handshake;
				Handshake handshake = (Handshake) packet.getData();
				if (handshake.data == null || handshake.data.version == null || handshake.data.publicKey == null) {
					client.send(new PacketDataIncorrect().toPacket(), false);
					return;
				}
				if (!handshake.data.version.equals("2.0.0")) {
					client.send(new UnsupportedVersion().toPacket(), false);
					return;
				}
				String hexPublicKey = handshake.data.publicKey;
				byte[] publicKey = Utils.hexStringToByteArray(hexPublicKey);
				byte[] sharedKey = server.DH.generateSharedSecret(publicKey);
				String base64SharedKey = Base64.getEncoder().encodeToString(sharedKey);
				if (base64SharedKey.length() > 32) base64SharedKey = base64SharedKey.substring(0, 32);
				SecretKey secretKey = Utils.generateSecretKey(base64SharedKey);
				Handshake handshakeResponse = new Handshake();
				handshakeResponse.data = new HandshakeData();
				handshakeResponse.data.version = "2.0.0";
				handshakeResponse.data.publicKey = Utils.byteArrayToHexString(server.DH.generatePublicKey());

				client.setSecretKey(secretKey);
				client.send(
						new MCPPacket(
								MajorPacketType.Handshake,
								HandshakePacketType.Handshake,
								handshakeResponse
						), false
				);
			}
		}
	}

	private static void handleAuthentication(MCPPacket packet, Client client, Server server)
	{
		switch ((AuthenticationPacketType) packet.getMinorPacketType()) {
			case Login -> {
				if (client.status == ClientStatus.Handshake) {
					client.status = ClientStatus.AwaitingLogin;
					rinitech.tcp.packets.json.Login login = (Login) packet.getData();
					User user = server.database.getUser(login.data.username);
					if (user == null) {
						client.send(new rinitech.tcp.errors.UserNotFound().toPacket(), true);
						return;
					}
					if (user.password == null) {
						client.send(new rinitech.tcp.errors.UserPasswordRequired().toPacket(), true);
						return;
					}
					if (user.password.equals(login.data.password)) {
						client.username = login.data.username;
						client.setAccessToken(Utils.generateAccessToken(client.username));
						client.status = ClientStatus.Connected;
						rinitech.tcp.packets.json.Accepted accepted = new rinitech.tcp.packets.json.Accepted();
						accepted.data = new AcceptedData();
						Room[] rooms = server.database.getRooms();
						rinitech.tcp.Room[] r = new rinitech.tcp.Room[rooms.length];
						for (int i = 0; i < rooms.length; i++) {
							r[i] = rooms[i].toMcpRoom();
						}

						accepted.data.rooms = r;
						accepted.data.http = server.config.http;
						client.send(
								new MCPPacket(
										MajorPacketType.Authentication,
										Accepted,
										accepted
								), true
						);

						rinitech.tcp.packets.json.UpdateAccessToken updateAccessToken = new rinitech.tcp.packets.json.UpdateAccessToken();
						updateAccessToken.data = new UpdateAccessTokenData();
						updateAccessToken.data.accessToken = client.getAccessToken();
						client.send(
								new MCPPacket(
										MajorPacketType.Authentication,
										UpdateAccessToken,
										updateAccessToken
								), true
						);
					}
					else {
						client.send(new rinitech.tcp.errors.UserPasswordInvalid().toPacket(), true);
					}
				}
			}
			case Accepted -> {
				if (client.status == ClientStatus.AwaitingLogin) {
					client.status = ClientStatus.Connected;
				}
			}
			case UpdateAccessToken -> {
				if (client.status == ClientStatus.Connected) {
					rinitech.tcp.packets.json.UpdateAccessToken updateAccessToken = (UpdateAccessToken) packet.getData();
					client.setAccessToken(updateAccessToken.data.accessToken);
				}
			}
		}
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
