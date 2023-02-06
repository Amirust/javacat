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
import rinitech.tcp.Room;

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
			case Heartbeat -> handleHeartbeat(packet, serverClient, server);
			case Authentication -> handleAuthentication(packet, serverClient, server);
			case Message -> handleMessage(packet, serverClient, server);
			case User -> handleUser(packet, serverClient, server);
			case Room -> handleRoom(packet, serverClient, server);
		}
	}

	private static void handleHandshake(MCPPacket packet, ServerClient serverClient, Server server)
	{
		if (packet.getMinorPacketType().equals(HandshakePacketType.Handshake)) {
			if (serverClient.getStatus() == ClientStatus.Awaiting) {
				serverClient.setStatus(ClientStatus.Handshake);
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
				byte[] sharedKey = server.getDH().generateSharedSecret(publicKey);
				String base64SharedKey = Base64.getEncoder().encodeToString(sharedKey);
				if (base64SharedKey.length() > 43) base64SharedKey = base64SharedKey.substring(0, 43);
				SecretKey secretKey = Utils.generateSecretKey(base64SharedKey);
				Handshake handshakeResponse = new Handshake();
				handshakeResponse.data = new HandshakeData();
				handshakeResponse.data.version = "2.0.0";
				handshakeResponse.data.publicKey = Utils.byteArrayToHexString(server.getDH().generatePublicKey());

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

	private static void handleHeartbeat(MCPPacket packet, ServerClient serverClient, Server server)
	{
		if (packet.getMinorPacketType().equals(HeartbeatPacketType.Ping)) {
			if (serverClient.getStatus() == ClientStatus.Connected) {
				serverClient.setLastHeartbeat(new Date());
			}
		}
	}

	private static void handleAuthentication(MCPPacket packet, ServerClient serverClient, Server server)
	{
		switch ((AuthenticationPacketType) packet.getMinorPacketType()) {
			case Login -> {
				if (serverClient.getStatus() == ClientStatus.Handshake) {
					serverClient.setStatus(ClientStatus.AwaitingLogin);
					rinitech.tcp.packets.json.Login login = (Login) packet.getData();
					User user = server.getDatabase().getUser(login.data.username);
					if (login.data.username.equals(server.getConfig().rootUsername)) {
						user = new User();
						user.username = server.getConfig().rootUsername;
						user.password = server.getConfig().rootPassword;
						serverClient.setRoot(true);
					}
					if (user == null) {
						serverClient.send(new rinitech.tcp.errors.UserNotFound().toPacket(), true);
						break;
					}
					if (login.data.password == null || login.data.password.isEmpty()) {
						serverClient.send(new rinitech.tcp.errors.UserPasswordRequired().toPacket(), true);
						break;
					}
					if (user.password.equals(login.data.password)) {
						serverClient.setUsername(login.data.username);
						serverClient.setAccessToken(Utils.generateAccessToken(serverClient.getUsername()));
						serverClient.setStatus(ClientStatus.Connected);
						rinitech.tcp.packets.json.Accepted accepted = new rinitech.tcp.packets.json.Accepted();
						accepted.data = new AcceptedData();
						ArrayList<SerializableRoom> rooms = new ArrayList<>();
						for (rinitech.tcp.Room room : Server.getRooms()) {
							SerializableRoom serializableRoom = new SerializableRoom(room);
							rooms.add(serializableRoom);
						}

						accepted.data.rooms = rooms.toArray(new SerializableRoom[0]);
						accepted.data.http = server.getConfig().http;
						accepted.data.heartbeatRate = server.getConfig().heartbeatRate;
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
						serverClient.setLastHeartbeat(new Date());
						serverClient.createHeartbeatTimer();
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
				if (serverClient.getStatus() == ClientStatus.Connected) {
					rinitech.tcp.packets.json.CreateTextMessage createTextMessage = (CreateTextMessage) packet.getData();
					Room room = Room.fromId(createTextMessage.rawRoom);
					if (createTextMessage.data.message == null || createTextMessage.data.message.isEmpty()) {
						serverClient.send(new rinitech.tcp.errors.PacketDataIncorrect().toPacket(), true);
						break;
					} else if (room == null) {
						serverClient.send(new rinitech.tcp.errors.RoomNotFound().toPacket(), true);
						break;
					}

					rinitech.tcp.packets.json.TextMessage textMessage = new rinitech.tcp.packets.json.TextMessage();
					textMessage.data = new TextMessageData();

					textMessage.data.message = createTextMessage.data.message;
					textMessage.data.rawTime = new Date().getTime();
					textMessage.data.user = serverClient.getUsername();
					textMessage.rawRoom = room.getId();

					MCPPacket packetToSend = new MCPPacket(MajorPacketType.Message, MessagePacketType.TextMessage, textMessage);
					room.broadcast(packetToSend);
				}
			}
			case CreateImageMessage -> {
				if (serverClient.getStatus() == ClientStatus.Connected) {
					rinitech.tcp.packets.json.CreateImageMessage createImageMessage = (CreateImageMessage) packet.getData();
					Room room = Room.fromId(createImageMessage.rawRoom);
					if (createImageMessage.data.image == null || createImageMessage.data.image.isEmpty()) {
						serverClient.send(new rinitech.tcp.errors.PacketDataIncorrect().toPacket(), true);
						break;
					} else if (room == null) {
						serverClient.send(new rinitech.tcp.errors.RoomNotFound().toPacket(), true);
						break;
					}

					rinitech.tcp.packets.json.ImageMessage imageMessage = new rinitech.tcp.packets.json.ImageMessage();
					imageMessage.data = new ImageMessageData();

					imageMessage.data.image = createImageMessage.data.image;
					imageMessage.data.rawTime = new Date().getTime();
					imageMessage.data.user = serverClient.getUsername();
					imageMessage.rawRoom = room.getId();

					MCPPacket packetToSend = new MCPPacket(MajorPacketType.Message, MessagePacketType.ImageMessage, imageMessage);
					room.broadcast(packetToSend);
				}
			}
		}
	}

	private static void handleUser(MCPPacket packet, ServerClient client, Server server)
	{
		switch ((UserPacketType) packet.getMinorPacketType()) {
			case Create -> {
				if (client.getStatus() == ClientStatus.Connected) {
					if (!client.isRoot()) client.send(new AccessDenied().toPacket(), true);
					rinitech.tcp.packets.json.UserCreate createUser = (UserCreate) packet.getData();
					if (createUser.data.username == null || createUser.data.username.isEmpty() || createUser.data.password == null || createUser.data.password.isEmpty()) {
						client.send(new rinitech.tcp.errors.PacketDataIncorrect().toPacket(), true);
						break;
					}

					if (server.getDatabase().getUser(createUser.data.username) != null) {
						client.send(new rinitech.tcp.errors.UserAlreadyExists().toPacket(), true);
						break;
					}

					server.getDatabase().addUser(createUser.data.username, createUser.data.password);
					UserCreated userCreated = new UserCreated();
					userCreated.data = new UserCreatedData();
					userCreated.data.username = createUser.data.username;
					MCPPacket packetToSend = new MCPPacket(MajorPacketType.User, UserPacketType.Created, userCreated);
					client.send(packetToSend, true);
				}
			}
			case Delete -> {
				if (client.getStatus() == ClientStatus.Connected) {
					if (!client.isRoot()) client.send(new AccessDenied().toPacket(), true);
					rinitech.tcp.packets.json.UserDelete deleteUser = (UserDelete) packet.getData();
					if (deleteUser.data.username == null || deleteUser.data.username.isEmpty()) {
						client.send(new rinitech.tcp.errors.PacketDataIncorrect().toPacket(), true);
						break;
					}

					if (server.getDatabase().getUser(deleteUser.data.username) == null) {
						client.send(new rinitech.tcp.errors.UserNotFound().toPacket(), true);
						break;
					}

					server.getDatabase().deleteUser(deleteUser.data.username);
					UserDeleted userDeleted = new UserDeleted();
					userDeleted.data = new UserDeletedData();
					userDeleted.data.username = deleteUser.data.username;
					MCPPacket packetToSend = new MCPPacket(MajorPacketType.User, UserPacketType.Deleted, userDeleted);
					client.send(packetToSend, true);
				}
			}
		}
	}

	private static void handleRoom(MCPPacket packet, ServerClient serverClient, Server server)
	{
		switch ((RoomPacketType) packet.getMinorPacketType()) {
			case Create -> {
				if (serverClient.getStatus() == ClientStatus.Connected) {
					if (!serverClient.isRoot()) serverClient.send(new AccessDenied().toPacket(), true);
					rinitech.tcp.packets.json.Create createRoom = (Create) packet.getData();
					if (createRoom.data.name == null || createRoom.data.name.isEmpty()) {
						serverClient.send(new rinitech.tcp.errors.PacketDataIncorrect().toPacket(), true);
						break;
					}

					int id = server.getRooms().size() + 1;
					rinitech.tcp.Room room = new rinitech.tcp.Room(id, createRoom.data.name);
					server.getDatabase().addRoom(room.getName(), room.getId());
					server.getRooms().add(room);

					Created created = new Created();
					created.data = new CreatedData();
					created.data.id = id;
					created.data.name = createRoom.data.name;
					MCPPacket packetToSend = new MCPPacket(MajorPacketType.Room, RoomPacketType.Created, created);

					serverClient.send(packetToSend, true);
				}
			}
			case Join -> {
				if (serverClient.getStatus() == ClientStatus.Connected) {
					if (!serverClient.isRoot()) serverClient.send(new AccessDenied().toPacket(), true);
					rinitech.tcp.packets.json.Join joinRoom = (Join) packet.getData();

					rinitech.tcp.Room room = server.getRooms().stream().filter(r -> r.getId() == joinRoom.data.room).findFirst().orElse(null);
					if (room == null) {
						serverClient.send(new rinitech.tcp.errors.RoomNotFound().toPacket(), true);
						break;
					}

					room.getUsers().add(serverClient);

					Joined joined = new Joined();
					joined.data = new JoinedData();
					joined.data.room = room.getId();
					joined.data.user = serverClient.getUsername();
					joined.data.allUsers = room.getUsers().stream().map(ServerClient::getUsername).toList().toArray(new String[0]);
					MCPPacket packetToSend = new MCPPacket(MajorPacketType.Room, RoomPacketType.Joined, joined);

					room.broadcast(packetToSend);
				}
			}
			case Leave -> {
				if (serverClient.getStatus() == ClientStatus.Connected) {
					if (!serverClient.isRoot()) serverClient.send(new AccessDenied().toPacket(), true);
					rinitech.tcp.packets.json.Leave leaveRoom = (Leave) packet.getData();

					rinitech.tcp.Room room = server.getRooms().stream().filter(r -> r.getId() == leaveRoom.data.room).findFirst().orElse(null);
					if (room == null) {
						serverClient.send(new rinitech.tcp.errors.RoomNotFound().toPacket(), true);
						break;
					}

					room.getUsers().remove(serverClient);

					Left left = new Left();
					left.data = new LeftData();
					left.data.room = room.getId();
					left.data.user = serverClient.getUsername();
					MCPPacket packetToSend = new MCPPacket(MajorPacketType.Room, RoomPacketType.Left, left);

					serverClient.send(packetToSend, true);
					room.broadcast(packetToSend);
				}
			}
			case Update -> {
				if (serverClient.getStatus() == ClientStatus.Connected) {
					if (!serverClient.isRoot()) serverClient.send(new AccessDenied().toPacket(), true);
					rinitech.tcp.packets.json.Update updateRoom = (Update) packet.getData();

					rinitech.tcp.Room room = server.getRooms().stream().filter(r -> r.getId() == updateRoom.data.room).findFirst().orElse(null);
					if (room == null) {
						serverClient.send(new rinitech.tcp.errors.RoomNotFound().toPacket(), true);
						break;
					}

					room.setName(updateRoom.data.name);
					server.getDatabase().updateRoom(room.getId(), room.getName());

					Updated updated = new Updated();
					updated.data = new UpdatedData();
					updated.data.room = room.getId();
					updated.data.name = room.getName();
					MCPPacket packetToSend = new MCPPacket(MajorPacketType.Room, RoomPacketType.Updated, updated);

					serverClient.send(packetToSend, true);
					room.broadcast(packetToSend);
				}
			}
			case Delete -> {
				if (serverClient.getStatus() == ClientStatus.Connected) {
					if (!serverClient.isRoot()) serverClient.send(new AccessDenied().toPacket(), true);
					rinitech.tcp.packets.json.Delete deleteRoom = (Delete) packet.getData();

					rinitech.tcp.Room room = server.getRooms().stream().filter(r -> r.getId() == deleteRoom.data.room).findFirst().orElse(null);
					if (room == null) {
						serverClient.send(new rinitech.tcp.errors.RoomNotFound().toPacket(), true);
						break;
					}

					server.getRooms().remove(room);
					server.getDatabase().deleteRoom(room.getId());

					Deleted deleted = new Deleted();
					deleted.data = new DeletedData();
					deleted.data.room = room.getId();
					MCPPacket packetToSend = new MCPPacket(MajorPacketType.Room, RoomPacketType.Deleted, deleted);

					serverClient.send(packetToSend, true);
					room.broadcast(packetToSend);
				}
			}
			case RequireList -> {
				if (serverClient.getStatus() == ClientStatus.Connected) {
					ArrayList<SerializableRoom> rooms = new ArrayList<>();
					for (rinitech.tcp.Room room : server.getRooms()) {
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
