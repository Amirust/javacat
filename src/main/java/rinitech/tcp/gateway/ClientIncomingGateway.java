package rinitech.tcp.gateway;

import rinitech.tcp.Client;
import rinitech.tcp.Utils;
import rinitech.tcp.errors.*;
import rinitech.tcp.packets.MCPPacket;
import rinitech.tcp.packets.json.*;
import rinitech.tcp.types.*;

import javax.crypto.SecretKey;
import java.util.Base64;

public class ClientIncomingGateway
{
	public static void handle(Client client, MCPPacket packet)
	{
		switch (packet.getMajorPacketType()) {
			case Handshake -> handleHandshake(client, packet);
			case Authentication -> handleAuthentication(client, packet);
			case Room -> handleRoom(client, packet);
			case Message -> handleMessage(client, packet);
			case Error -> handleError(client, packet);
		}
	}

	private static void handleHandshake(Client client, MCPPacket packet)
	{
		switch ((HandshakePacketType) packet.getMinorPacketType()) {
			case Handshake -> {
				Handshake handshake = (Handshake) packet.getData();
				if (handshake.data == null || handshake.data.version == null || handshake.data.publicKey == null) throw new PacketDataIncorrect();
				if (!handshake.data.version.equals("2.0.0")) throw new UnsupportedVersion();

				byte[] publicKey = Utils.hexStringToByteArray(handshake.data.publicKey);
				byte[] sharedKey = client.getDH().generateSharedSecret(publicKey);
				String base64SharedKey = Base64.getEncoder().encodeToString(sharedKey);
				if (base64SharedKey.length() > 32) base64SharedKey = base64SharedKey.substring(0, 32);
				SecretKey secretKey = Utils.generateSecretKey(base64SharedKey);

				client.setSecretKey(secretKey);
				client.getEvents().emit(ClientEvent.HandshakeSuccess, null);
			}
		}
	}

	private static void handleAuthentication(Client client, MCPPacket packet)
	{
		switch ((AuthenticationPacketType) packet.getMinorPacketType()) {
			case Accepted -> {
				Accepted accepted = (Accepted) packet.getData();
				if (accepted.data == null || accepted.data.http == null || accepted.data.rooms == null) throw new PacketDataIncorrect();
				client.getEvents().emit(ClientEvent.AuthenticationSuccess, packet);
			}
			case UpdateAccessToken -> {
				UpdateAccessToken updateAccessToken = (UpdateAccessToken) packet.getData();
				if (updateAccessToken.data == null || updateAccessToken.data.accessToken == null) throw new PacketDataIncorrect();
				client.setAccessToken(updateAccessToken.data.accessToken);
			}
		}
	}

	private static void handleRoom(Client client, MCPPacket packet)
	{
		switch ((RoomPacketType) packet.getMinorPacketType()) {
			case Joined -> {
				Joined joined = (Joined) packet.getData();
				if (joined.data == null) throw new PacketDataIncorrect();
				client.getEvents().emit(ClientEvent.RoomJoined, packet);
			}
			case Left -> {
				Left left = (Left) packet.getData();
				if (left.data == null) throw new PacketDataIncorrect();
				client.getEvents().emit(ClientEvent.RoomLeft, packet);
			}
			case Updated -> {
				Updated updated = (Updated) packet.getData();
				if (updated.data == null) throw new PacketDataIncorrect();
				client.getEvents().emit(ClientEvent.RoomUpdated, packet);
			}
			case Deleted -> {
				Deleted deleted = (Deleted) packet.getData();
				if (deleted.data == null) throw new PacketDataIncorrect();
				client.getEvents().emit(ClientEvent.RoomDeleted, packet);
			}
			case Created -> {
				Created created = (Created) packet.getData();
				if (created.data == null) throw new PacketDataIncorrect();
				client.getEvents().emit(ClientEvent.RoomCreated, packet);
			}
			case List -> {
				List list = (List) packet.getData();
				if (list.data == null) throw new PacketDataIncorrect();
				client.getEvents().emit(ClientEvent.RoomList, packet);
			}
		}
	}

	private static void handleMessage(Client client, MCPPacket packet)
	{
		switch ((MessagePacketType) packet.getMinorPacketType()) {
			case TextMessage -> {
				TextMessage textMessage = (TextMessage) packet.getData();
				if (textMessage.data == null || textMessage.data.message == null || textMessage.data.user == null || textMessage.data.rawTime <= 0) throw new PacketDataIncorrect();
				client.getEvents().emit(ClientEvent.TextMessage, packet);
			}
			case ImageMessage -> {
				ImageMessage imageMessage = (ImageMessage) packet.getData();
				if (imageMessage.data == null || imageMessage.data.image == null || imageMessage.data.user == null || imageMessage.data.rawTime <= 0) throw new PacketDataIncorrect();
				client.getEvents().emit(ClientEvent.ImageMessage, packet);
			}
		}
	}

	private static void handleError(Client client, MCPPacket packet)
	{
		client.getEvents().emit(ClientEvent.Error, packet);
	}
}
