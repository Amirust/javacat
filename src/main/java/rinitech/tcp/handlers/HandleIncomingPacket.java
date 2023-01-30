package rinitech.tcp.handlers;

import rinitech.database.types.User;
import rinitech.tcp.Client;
import rinitech.tcp.Server;
import rinitech.tcp.Utils;
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

public class HandleIncomingPacket
{
	public static void handle(MCPPacket packet, Client client, Server server)
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
				client.events.emit("handshake", packet);
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
						break;
					}
					if (user.password == null) {
						client.send(new rinitech.tcp.errors.UserPasswordRequired().toPacket(), true);
						break;
					}
					if (user.password.equals(login.data.password)) {
						client.username = login.data.username;
						client.setAccessToken(Utils.generateAccessToken(client.username));
						client.status = ClientStatus.Connected;
						rinitech.tcp.packets.json.Accepted accepted = new rinitech.tcp.packets.json.Accepted();
						accepted.data = new AcceptedData();
						ArrayList<SerializableRoom> rooms = new ArrayList<>();
						for (rinitech.tcp.Room room : server.rooms) {
							SerializableRoom serializableRoom = new SerializableRoom(room);
							rooms.add(serializableRoom);
						}

						accepted.data.rooms = rooms.toArray(new SerializableRoom[0]);
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
						client.createUpdateAccessTokenTimer();
					}
					else {
						client.send(new rinitech.tcp.errors.UserPasswordInvalid().toPacket(), true);
					}
				}
			}
			case Accepted -> {
				if (client.status == ClientStatus.AwaitingLogin) {
					client.status = ClientStatus.Connected;
					client.events.emit("accepted", packet);
				}
			}
			case UpdateAccessToken -> {
				if (client.status == ClientStatus.Connected) {
					rinitech.tcp.packets.json.UpdateAccessToken updateAccessToken = (UpdateAccessToken) packet.getData();
					if (updateAccessToken.data.accessToken == null) {
						client.send(new rinitech.tcp.errors.PacketDataIncorrect().toPacket(), true);
						break;
					}
					client.events.emit("updateAccessToken", packet);
					client.setAccessToken(updateAccessToken.data.accessToken);
				}
			}
		}
	}

	private static void handleMessage(MCPPacket packet, Client client, Server server)
	{
		switch ((MessagePacketType) packet.getMinorPacketType()) {
			case TextMessage -> {
				if (client.status == ClientStatus.Connected) {
					rinitech.tcp.packets.json.TextMessage textMessage = (TextMessage) packet.getData();
					if (textMessage.data.message == null || textMessage.data.time == null || textMessage.data.user == null) {
						throw new rinitech.tcp.errors.PacketDataIncorrect();
					} else if (textMessage.room == null) {
						throw new rinitech.tcp.errors.RoomNotFound();
					}

					client.events.emit("roomMessage", packet);
				}
			}
			case ImageMessage -> {
				if (client.status == ClientStatus.Connected) {
					rinitech.tcp.packets.json.ImageMessage imageMessage = (ImageMessage) packet.getData();
					if (imageMessage.data.image == null || imageMessage.data.time == null || imageMessage.data.user == null) {
						throw new rinitech.tcp.errors.PacketDataIncorrect();
					} else if (imageMessage.room == null) {
						throw new rinitech.tcp.errors.RoomNotFound();
					}

					client.events.emit("roomMessage", packet);
				}
			}
			case CreateTextMessage -> {
				if (client.status == ClientStatus.Connected) {
					rinitech.tcp.packets.json.CreateTextMessage createTextMessage = (CreateTextMessage) packet.getData();
					if (createTextMessage.data.message == null || createTextMessage.data.message.isEmpty()) {
						client.send(new rinitech.tcp.errors.PacketDataIncorrect().toPacket(), true);
						break;
					} else if (createTextMessage.room == null) {
						client.send(new rinitech.tcp.errors.RoomNotFound().toPacket(), true);
						break;
					}

					rinitech.tcp.packets.json.TextMessage textMessage = new rinitech.tcp.packets.json.TextMessage();
					textMessage.data = new TextMessageData();

					textMessage.data.message = createTextMessage.data.message;
					textMessage.data.time = new Date();
					textMessage.data.user = client.username;
					textMessage.rawRoom = createTextMessage.room.id;

					MCPPacket packetToSend = new MCPPacket(MajorPacketType.Message, MessagePacketType.TextMessage, textMessage);
					server.database.addTextMessage(client.username, textMessage.data.message, createTextMessage.room.id, textMessage.data.time);
					textMessage.room.broadcast(packetToSend);
				}
			}
			case CreateImageMessage -> {
				if (client.status == ClientStatus.Connected) {
					rinitech.tcp.packets.json.CreateImageMessage createImageMessage = (CreateImageMessage) packet.getData();
					if (createImageMessage.data.image == null || createImageMessage.data.image.isEmpty()) {
						client.send(new rinitech.tcp.errors.PacketDataIncorrect().toPacket(), true);
						break;
					} else if (createImageMessage.room == null) {
						client.send(new rinitech.tcp.errors.RoomNotFound().toPacket(), true);
						break;
					}

					rinitech.tcp.packets.json.ImageMessage imageMessage = new rinitech.tcp.packets.json.ImageMessage();
					imageMessage.data = new ImageMessageData();

					imageMessage.data.image = createImageMessage.data.image;
					imageMessage.data.time = new Date();
					imageMessage.data.user = client.username;
					imageMessage.rawRoom = createImageMessage.room.id;

					MCPPacket packetToSend = new MCPPacket(MajorPacketType.Message, MessagePacketType.ImageMessage, imageMessage);
					server.database.addImageMessage(client.username, imageMessage.data.image, createImageMessage.room.id, imageMessage.data.time);
					imageMessage.room.broadcast(packetToSend);
				}
			}
		}
	}

	private static void handleRoom(MCPPacket packet, Client client, Server server)
	{
		switch ((RoomPacketType) packet.getMinorPacketType()) {
			case Create -> {
				if (client.status == ClientStatus.Connected) {
					rinitech.tcp.packets.json.Create createRoom = (Create) packet.getData();
					if (createRoom.data.name == null || createRoom.data.name.isEmpty()) {
						client.send(new rinitech.tcp.errors.PacketDataIncorrect().toPacket(), true);
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

					client.send(packetToSend, true);
				}
			}
			case Join -> {
				if (client.status == ClientStatus.Connected) {
					rinitech.tcp.packets.json.Join joinRoom = (Join) packet.getData();

					rinitech.tcp.Room room = server.rooms.stream().filter(r -> r.id == joinRoom.data.room).findFirst().orElse(null);
					if (room == null) {
						client.send(new rinitech.tcp.errors.RoomNotFound().toPacket(), true);
						break;
					}

					room.users.add(client);

					Joined joined = new Joined();
					joined.data = new JoinedData();
					joined.data.room = room.id;
					joined.data.user = client.username;
					joined.data.allUsers = room.users.stream().map(u -> u.username).toList().toArray(new String[0]);
					MCPPacket packetToSend = new MCPPacket(MajorPacketType.Room, RoomPacketType.Joined, joined);

					client.send(packetToSend, true);
					room.broadcast(packetToSend);
				}
			}
			case Leave -> {
				if (client.status == ClientStatus.Connected) {
					rinitech.tcp.packets.json.Leave leaveRoom = (Leave) packet.getData();

					rinitech.tcp.Room room = server.rooms.stream().filter(r -> r.id == leaveRoom.data.room).findFirst().orElse(null);
					if (room == null) {
						client.send(new rinitech.tcp.errors.RoomNotFound().toPacket(), true);
						break;
					}

					room.users.remove(client);

					Left left = new Left();
					left.data = new LeftData();
					left.data.room = room.id;
					left.data.user = client.username;
					MCPPacket packetToSend = new MCPPacket(MajorPacketType.Room, RoomPacketType.Left, left);

					client.send(packetToSend, true);
					room.broadcast(packetToSend);
				}
			}
			case List -> {
				if (client.status == ClientStatus.Connected) client.events.emit("list", packet);
			}
			case Update -> {
				if (client.status == ClientStatus.Connected) {
					rinitech.tcp.packets.json.Update updateRoom = (Update) packet.getData();

					rinitech.tcp.Room room = server.rooms.stream().filter(r -> r.id == updateRoom.data.room).findFirst().orElse(null);
					if (room == null) {
						client.send(new rinitech.tcp.errors.RoomNotFound().toPacket(), true);
						break;
					}

					room.name = updateRoom.data.name;
					server.database.updateRoom(room.id, room.name);

					Updated updated = new Updated();
					updated.data = new UpdatedData();
					updated.data.room = room.id;
					updated.data.name = room.name;
					MCPPacket packetToSend = new MCPPacket(MajorPacketType.Room, RoomPacketType.Updated, updated);

					client.send(packetToSend, true);
					room.broadcast(packetToSend);
				}
			}
			case Delete -> {
				if (client.status == ClientStatus.Connected) {
					rinitech.tcp.packets.json.Delete deleteRoom = (Delete) packet.getData();

					rinitech.tcp.Room room = server.rooms.stream().filter(r -> r.id == deleteRoom.data.room).findFirst().orElse(null);
					if (room == null) {
						client.send(new rinitech.tcp.errors.RoomNotFound().toPacket(), true);
						break;
					}

					server.rooms.remove(room);
					server.database.deleteRoom(room.id);

					Deleted deleted = new Deleted();
					deleted.data = new DeletedData();
					deleted.data.room = room.id;
					MCPPacket packetToSend = new MCPPacket(MajorPacketType.Room, RoomPacketType.Deleted, deleted);

					client.send(packetToSend, true);
					room.broadcast(packetToSend);
				}
			}
			case Created -> {
				if (client.status == ClientStatus.Connected) client.events.emit("created", packet);
			}
			case Joined -> {
				if (client.status == ClientStatus.Connected) client.events.emit("joined", packet);
			}
			case Left -> {
				if (client.status == ClientStatus.Connected) client.events.emit("left", packet);
			}
			case Deleted -> {
				if (client.status == ClientStatus.Connected) client.events.emit("deleted", packet);
			}
			case Updated -> {
				if (client.status == ClientStatus.Connected) client.events.emit("updated", packet);
			}
			case RequireList -> {
				if (client.status == ClientStatus.Connected) {
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

	private static void handleError(MCPPacket packet, Client client, Server server)
	{
		client.events.emit("error", packet);
	}
}
