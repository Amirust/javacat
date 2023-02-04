package rinitech.tcp.gateway;

import rinitech.database.types.User;
import rinitech.tcp.ServerClient;
import rinitech.tcp.Server;
import rinitech.tcp.Utils;
import rinitech.tcp.errors.AccessDenied;
import rinitech.tcp.errors.PacketDataIncorrect;
import rinitech.tcp.errors.UnsupportedVersion;
import rinitech.tcp.packets.MCPPacket;
import rinitech.tcp.packets.SerializableRoom;
import rinitech.tcp.packets.json.*;

import static rinitech.tcp.types.AuthenticationPacketType.*;

import rinitech.tcp.types.*;

import javax.crypto.SecretKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;

public class ServerIncomingGateway
{
	public static void handle(MCPPacket packet, ServerClient serverClient, Server server)
	{
		switch (packet.getMajorPacketType()) {
			case Handshake -> handleHandshake(packet, serverClient, server);
			case Authentication -> handleAuthentication(packet, serverClient, server);
			case Message -> handleMessage(packet, serverClient, server);
			case Room -> handleRoom(packet, serverClient, server);
		}
	}

	private static void handleHandshake(MCPPacket packet, ServerClient serverClient, Server server)
	{
		if (packet.getMinorPacketType().equals(HandshakePacketType.Handshake)) {
			if (serverClient.status == ClientStatus.Awaiting) {
				serverClient.status = ClientStatus.Handshake;
				Handshake handshake = (Handshake) packet.getData();
				if (handshake.data == null || handshake.data.version == null || handshake.data.publicKey == null) {
					serverClient.send(new PacketDataIncorrect().toPacket(), false);
					return;
				}
				if (!handshake.data.version.equals("2.0.0")) {
					serverClient.send(new UnsupportedVersion().toPacket(), false);
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

				serverClient.setSecretKey(secretKey);
				serverClient.send(
						new MCPPacket(
								MajorPacketType.Handshake,
								HandshakePacketType.Handshake,
								handshakeResponse
						), false
				);
			}
		}
	}

	private static void handleAuthentication(MCPPacket packet, ServerClient serverClient, Server server)
	{
		switch ((AuthenticationPacketType) packet.getMinorPacketType()) {
			case Login -> {
				if (serverClient.status == ClientStatus.Handshake) {
					serverClient.status = ClientStatus.AwaitingLogin;
					rinitech.tcp.packets.json.Login login = (Login) packet.getData();
					User user = server.database.getUser(login.data.username);
					if (login.data.username.equals(server.config.rootUsername)) {
						user = new User();
						user.username = server.config.rootUsername;
						user.password = server.config.rootPassword;
						serverClient.isRoot = true;
					}
					if (user == null) {
						serverClient.send(new rinitech.tcp.errors.UserNotFound().toPacket(), true);
						break;
					}
					if (login.data.password == null) {
						serverClient.send(new rinitech.tcp.errors.UserPasswordRequired().toPacket(), true);
						break;
					}
					if (user.password.equals(login.data.password)) {
						serverClient.username = login.data.username;
						serverClient.setAccessToken(Utils.generateAccessToken(serverClient.username));
						serverClient.status = ClientStatus.Connected;
						rinitech.tcp.packets.json.Accepted accepted = new rinitech.tcp.packets.json.Accepted();
						accepted.data = new AcceptedData();
						ArrayList<SerializableRoom> rooms = new ArrayList<>();
						for (rinitech.tcp.Room room : server.rooms) {
							SerializableRoom serializableRoom = new SerializableRoom(room);
							rooms.add(serializableRoom);
						}

						accepted.data.rooms = rooms.toArray(new SerializableRoom[0]);
						accepted.data.http = server.config.http;
						serverClient.send(
								new MCPPacket(
										MajorPacketType.Authentication,
										Accepted,
										accepted
								), true
						);

						rinitech.tcp.packets.json.UpdateAccessToken updateAccessToken = new rinitech.tcp.packets.json.UpdateAccessToken();
						updateAccessToken.data = new UpdateAccessTokenData();
						updateAccessToken.data.accessToken = serverClient.getAccessToken();
						serverClient.send(
								new MCPPacket(
										MajorPacketType.Authentication,
										UpdateAccessToken,
										updateAccessToken
								), true
						);
						serverClient.createUpdateAccessTokenTimer();
					}
					else {
						serverClient.send(new rinitech.tcp.errors.UserPasswordInvalid().toPacket(), true);
					}
				}
			}
		}
	}

	private static void handleMessage(MCPPacket packet, ServerClient serverClient, Server server)
	{
		switch ((MessagePacketType) packet.getMinorPacketType()) {
			case CreateTextMessage -> {
				if (serverClient.status == ClientStatus.Connected) {
					rinitech.tcp.packets.json.CreateTextMessage createTextMessage = (CreateTextMessage) packet.getData();
					if (createTextMessage.data.message == null || createTextMessage.data.message.isEmpty()) {
						serverClient.send(new rinitech.tcp.errors.PacketDataIncorrect().toPacket(), true);
						break;
					} else if (createTextMessage.room == null) {
						serverClient.send(new rinitech.tcp.errors.RoomNotFound().toPacket(), true);
						break;
					}

					rinitech.tcp.packets.json.TextMessage textMessage = new rinitech.tcp.packets.json.TextMessage();
					textMessage.data = new TextMessageData();

					textMessage.data.message = createTextMessage.data.message;
					textMessage.data.time = new Date();
					textMessage.data.user = serverClient.username;
					textMessage.rawRoom = createTextMessage.room.id;

					MCPPacket packetToSend = new MCPPacket(MajorPacketType.Message, MessagePacketType.TextMessage, textMessage);
					server.database.addTextMessage(serverClient.username, textMessage.data.message, createTextMessage.room.id, textMessage.data.time);
					textMessage.room.broadcast(packetToSend);
				}
			}
			case CreateImageMessage -> {
				if (serverClient.status == ClientStatus.Connected) {
					rinitech.tcp.packets.json.CreateImageMessage createImageMessage = (CreateImageMessage) packet.getData();
					if (createImageMessage.data.image == null || createImageMessage.data.image.isEmpty()) {
						serverClient.send(new rinitech.tcp.errors.PacketDataIncorrect().toPacket(), true);
						break;
					} else if (createImageMessage.room == null) {
						serverClient.send(new rinitech.tcp.errors.RoomNotFound().toPacket(), true);
						break;
					}

					rinitech.tcp.packets.json.ImageMessage imageMessage = new rinitech.tcp.packets.json.ImageMessage();
					imageMessage.data = new ImageMessageData();

					imageMessage.data.image = createImageMessage.data.image;
					imageMessage.data.time = new Date();
					imageMessage.data.user = serverClient.username;
					imageMessage.rawRoom = createImageMessage.room.id;

					MCPPacket packetToSend = new MCPPacket(MajorPacketType.Message, MessagePacketType.ImageMessage, imageMessage);
					server.database.addImageMessage(serverClient.username, imageMessage.data.image, createImageMessage.room.id, imageMessage.data.time);
					imageMessage.room.broadcast(packetToSend);
				}
			}
		}
	}

	private static void handleRoom(MCPPacket packet, ServerClient serverClient, Server server)
	{
		switch ((RoomPacketType) packet.getMinorPacketType()) {
			case Create -> {
				if (serverClient.status == ClientStatus.Connected) {
					if (!serverClient.isRoot) serverClient.send(new AccessDenied().toPacket(), true);
					rinitech.tcp.packets.json.Create createRoom = (Create) packet.getData();
					if (createRoom.data.name == null || createRoom.data.name.isEmpty()) {
						serverClient.send(new rinitech.tcp.errors.PacketDataIncorrect().toPacket(), true);
						break;
					}

					int id = server.rooms.size() + 1;
					rinitech.tcp.Room room = new rinitech.tcp.Room(id, createRoom.data.name);
					server.database.addRoom(room.name, room.id);
					server.rooms.add(room);

					Created created = new Created();
					created.data = new CreatedData();
					created.data.id = id;
					created.data.name = createRoom.data.name;
					MCPPacket packetToSend = new MCPPacket(MajorPacketType.Room, RoomPacketType.Created, created);

					serverClient.send(packetToSend, true);
				}
			}
			case Join -> {
				if (serverClient.status == ClientStatus.Connected) {
					if (!serverClient.isRoot) serverClient.send(new AccessDenied().toPacket(), true);
					rinitech.tcp.packets.json.Join joinRoom = (Join) packet.getData();

					rinitech.tcp.Room room = server.rooms.stream().filter(r -> r.id == joinRoom.data.room).findFirst().orElse(null);
					if (room == null) {
						serverClient.send(new rinitech.tcp.errors.RoomNotFound().toPacket(), true);
						break;
					}

					room.users.add(serverClient);

					Joined joined = new Joined();
					joined.data = new JoinedData();
					joined.data.room = room.id;
					joined.data.user = serverClient.username;
					joined.data.allUsers = room.users.stream().map(u -> u.username).toList().toArray(new String[0]);
					MCPPacket packetToSend = new MCPPacket(MajorPacketType.Room, RoomPacketType.Joined, joined);

					serverClient.send(packetToSend, true);
					room.broadcast(packetToSend);
				}
			}
			case Leave -> {
				if (serverClient.status == ClientStatus.Connected) {
					if (!serverClient.isRoot) serverClient.send(new AccessDenied().toPacket(), true);
					rinitech.tcp.packets.json.Leave leaveRoom = (Leave) packet.getData();

					rinitech.tcp.Room room = server.rooms.stream().filter(r -> r.id == leaveRoom.data.room).findFirst().orElse(null);
					if (room == null) {
						serverClient.send(new rinitech.tcp.errors.RoomNotFound().toPacket(), true);
						break;
					}

					room.users.remove(serverClient);

					Left left = new Left();
					left.data = new LeftData();
					left.data.room = room.id;
					left.data.user = serverClient.username;
					MCPPacket packetToSend = new MCPPacket(MajorPacketType.Room, RoomPacketType.Left, left);

					serverClient.send(packetToSend, true);
					room.broadcast(packetToSend);
				}
			}
			case Update -> {
				if (serverClient.status == ClientStatus.Connected) {
					if (!serverClient.isRoot) serverClient.send(new AccessDenied().toPacket(), true);
					rinitech.tcp.packets.json.Update updateRoom = (Update) packet.getData();

					rinitech.tcp.Room room = server.rooms.stream().filter(r -> r.id == updateRoom.data.room).findFirst().orElse(null);
					if (room == null) {
						serverClient.send(new rinitech.tcp.errors.RoomNotFound().toPacket(), true);
						break;
					}

					room.name = updateRoom.data.name;
					server.database.updateRoom(room.id, room.name);

					Updated updated = new Updated();
					updated.data = new UpdatedData();
					updated.data.room = room.id;
					updated.data.name = room.name;
					MCPPacket packetToSend = new MCPPacket(MajorPacketType.Room, RoomPacketType.Updated, updated);

					serverClient.send(packetToSend, true);
					room.broadcast(packetToSend);
				}
			}
			case Delete -> {
				if (serverClient.status == ClientStatus.Connected) {
					if (!serverClient.isRoot) serverClient.send(new AccessDenied().toPacket(), true);
					rinitech.tcp.packets.json.Delete deleteRoom = (Delete) packet.getData();

					rinitech.tcp.Room room = server.rooms.stream().filter(r -> r.id == deleteRoom.data.room).findFirst().orElse(null);
					if (room == null) {
						serverClient.send(new rinitech.tcp.errors.RoomNotFound().toPacket(), true);
						break;
					}

					server.rooms.remove(room);
					server.database.deleteRoom(room.id);

					Deleted deleted = new Deleted();
					deleted.data = new DeletedData();
					deleted.data.room = room.id;
					MCPPacket packetToSend = new MCPPacket(MajorPacketType.Room, RoomPacketType.Deleted, deleted);

					serverClient.send(packetToSend, true);
					room.broadcast(packetToSend);
				}
			}
			case RequireList -> {
				if (serverClient.status == ClientStatus.Connected) {
					ArrayList<SerializableRoom> rooms = new ArrayList<>();
					for (rinitech.tcp.Room room : server.rooms) {
						SerializableRoom serializableRoom = new SerializableRoom(room);
						rooms.add(serializableRoom);
					}

					List list = new List();
					list.data = new ListData();
					list.data.rooms = rooms.toArray(new SerializableRoom[0]);
				}
			}
		}
	}
}
