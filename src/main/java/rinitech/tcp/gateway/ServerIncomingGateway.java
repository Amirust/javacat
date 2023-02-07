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
import rinitech.tcp.packets.SerializableUser;
import rinitech.tcp.packets.json.*;
import rinitech.tcp.Room;

import static rinitech.tcp.types.AuthenticationPacketType.*;

import rinitech.tcp.packets.json.List;
import rinitech.tcp.types.*;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.security.*;

public class ServerIncomingGateway
{
	public static void handle(MCPPacket packet, ServerClient serverClient, Server server) throws NoSuchAlgorithmException, IOException
	{
		switch (packet.getMajorPacketType()) {
			case Handshake -> handleHandshake(packet, serverClient, server);
			case Heartbeat -> handleHeartbeat(packet, serverClient, server);
			case Authentication -> handleAuthentication(packet, serverClient, server);
			case Message -> handleMessage(packet, serverClient, server);
			case File -> handleFile(packet, serverClient, server);
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
					serverClient.send(new PacketDataIncorrect().toPacket(packet.getId()), false);
					return;
				}
				if (!handshake.data.version.equals(server.getConfig().version)) {
					serverClient.send(new UnsupportedVersion().toPacket(packet.getId()), false);
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
				handshakeResponse.data.version = server.getConfig().version;
				handshakeResponse.data.publicKey = Utils.byteArrayToHexString(server.getDH().generatePublicKey());

				serverClient.setSecretKey(secretKey);
				serverClient.send(
						new MCPPacket(
								MajorPacketType.Handshake,
								HandshakePacketType.Handshake,
								handshakeResponse,
								packet.getId()
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
						serverClient.send(new rinitech.tcp.errors.UserNotFound().toPacket(packet.getId()), true);
						break;
					}
					if (login.data.password == null || login.data.password.isEmpty()) {
						serverClient.send(new rinitech.tcp.errors.UserPasswordRequired().toPacket(packet.getId()), true);
						break;
					}
					if (user.password.equals(login.data.password)) {
						if (serverClient.isRoot()) serverClient.setId(0);
						else serverClient.setId(user.id);

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
						accepted.data.heartbeatRate = server.getConfig().heartbeatRate;
						serverClient.send(
								new MCPPacket(
										MajorPacketType.Authentication,
										Accepted,
										accepted,
										packet.getId()
								), true
						);
						serverClient.setLastHeartbeat(new Date());
						serverClient.createHeartbeatTimer();
					}
					else {
						serverClient.send(new rinitech.tcp.errors.UserPasswordInvalid().toPacket(packet.getId()), true);
					}
				}
			}
			case Register -> {
				if (serverClient.getStatus() == ClientStatus.Handshake) {
					rinitech.tcp.packets.json.Register register = (Register) packet.getData();
					if (register.data.username == null || register.data.username.isEmpty() || register.data.password == null || register.data.password.isEmpty()) {
						serverClient.send(new rinitech.tcp.errors.PacketDataIncorrect().toPacket(packet.getId()), true);
						break;
					}
					if (server.getDatabase().getUser(register.data.username) != null || register.data.username.equals(server.getConfig().rootUsername)) {
						serverClient.send(new rinitech.tcp.errors.UserAlreadyExists().toPacket(packet.getId()), true);
						break;
					}
					server.getDatabase().addUser(register.data.username, register.data.password);
					UserCreated userCreated = new UserCreated();
					userCreated.data = new UserCreatedData();
					userCreated.data.username = register.data.username;

					MCPPacket packetToSend = new MCPPacket(
							MajorPacketType.User,
							UserPacketType.Created,
							userCreated,
							packet.getId()
					);
					serverClient.send(packetToSend, true);
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
					if (createTextMessage.data.text == null || createTextMessage.data.text.isEmpty()) {
						serverClient.send(new rinitech.tcp.errors.PacketDataIncorrect().toPacket(packet.getId()), true);
						break;
					} else if (room == null) {
						serverClient.send(new rinitech.tcp.errors.RoomNotFound().toPacket(packet.getId()), true);
						break;
					}

					rinitech.tcp.packets.json.TextMessage textMessage = new rinitech.tcp.packets.json.TextMessage();
					textMessage.data = new TextMessageData();

					textMessage.data.message = createTextMessage.data.text;
					textMessage.data.rawTime = new Date().getTime();
					textMessage.data.user = serverClient.getUsername();
					textMessage.rawRoom = room.getId();

					MCPPacket packetToSend = new MCPPacket(MajorPacketType.Message, MessagePacketType.TextMessage, textMessage, "DontHaveID");
					room.broadcast(packetToSend);
				}
			}
			case CreateImageMessage -> {
				if (serverClient.getStatus() == ClientStatus.Connected) {
					rinitech.tcp.packets.json.CreateImageMessage createImageMessage = (CreateImageMessage) packet.getData();
					Room room = Room.fromId(createImageMessage.rawRoom);
					if ((createImageMessage.data.image == null || createImageMessage.data.image.isEmpty()) && (createImageMessage.data.url == null || createImageMessage.data.url.isEmpty())) {
						serverClient.send(new rinitech.tcp.errors.PacketDataIncorrect().toPacket(packet.getId()), true);
						break;
					} else if (room == null) {
						serverClient.send(new rinitech.tcp.errors.RoomNotFound().toPacket(packet.getId()), true);
						break;
					}


					rinitech.tcp.packets.json.ImageMessage imageMessage = new rinitech.tcp.packets.json.ImageMessage();
					imageMessage.data = new ImageMessageData();

					if (createImageMessage.data.image != null && !createImageMessage.data.image.isEmpty()) {
						imageMessage.data.image = createImageMessage.data.image;
					} else {
						imageMessage.data.image = createImageMessage.data.url;
					}
					imageMessage.data.rawTime = new Date().getTime();
					imageMessage.data.user = serverClient.getUsername();
					imageMessage.rawRoom = room.getId();

					MCPPacket packetToSend = new MCPPacket(MajorPacketType.Message, MessagePacketType.ImageMessage, imageMessage, "DontHaveID");
					room.broadcast(packetToSend);
				}
			}
		}
	}

	private static void handleFile(MCPPacket packet, ServerClient serverClient, Server server) throws NoSuchAlgorithmException, IOException
	{
		switch ((FilePacketType) packet.getMinorPacketType()) {
			case RequestImageUpload -> {
				if (serverClient.getStatus() == ClientStatus.Connected) {
					rinitech.tcp.packets.json.RequestImageUpload requestImageUpload = (RequestImageUpload) packet.getData();
					if (
							requestImageUpload.data.name == null || requestImageUpload.data.name.isEmpty() ||
							requestImageUpload.data.type == null || requestImageUpload.data.type.isEmpty() ||
							requestImageUpload.data.size == 0
					) {
						serverClient.send(new rinitech.tcp.errors.PacketDataIncorrect().toPacket(packet.getId()), true);
						break;
					}

					if ((new File(".")).getUsableSpace() < requestImageUpload.data.size) {
						MCPPacket packetToSend = new MCPPacket(MajorPacketType.File, FilePacketType.ImageUploadRejected, new ImageUploadRejected(), packet.getId());
						serverClient.send(packetToSend, true);
						break;
					}

					String extension = "";
					System.out.println(requestImageUpload.data.type);
					switch (requestImageUpload.data.type) {
						case "image/jpeg" -> extension = ".jpg";
						case "image/png" -> extension = ".png";
						case "image/gif" -> extension = ".gif";
						default -> {
							serverClient.send(new rinitech.tcp.errors.PacketDataIncorrect().toPacket(packet.getId()), true);
							break;
						}
					}
					if (extension.isEmpty())
						break;

					String filename = Utils.byteArrayToHexString(MessageDigest.getInstance("MD5").digest(requestImageUpload.data.name.getBytes()));

					File file = new File("images/" + filename + extension);
					if (file.exists())
						filename += "-" + new Date().getTime();

					System.out.println("Creating file: " + filename + extension);
					file = new File("images/" + filename + extension);
					file.createNewFile();

					rinitech.tcp.packets.json.ImageUploadAccepted imageUploadAccepted = new ImageUploadAccepted();
					imageUploadAccepted.data = new ImageUploadAcceptedData();
					imageUploadAccepted.data.id = filename;

					MCPPacket packetToSend = new MCPPacket(MajorPacketType.File, FilePacketType.ImageUploadAccepted, imageUploadAccepted, packet.getId());
					server.getFileUploads().add(filename);
					serverClient.send(packetToSend, true);
				}
			}

			case ImageUploadPart -> {
				if (serverClient.getStatus() == ClientStatus.Connected) {
					rinitech.tcp.packets.json.ImageUploadPart imageUploadPart = (ImageUploadPart) packet.getData();
					if (imageUploadPart.data.id == null || imageUploadPart.data.id.isEmpty() || imageUploadPart.data.data == null || imageUploadPart.data.data.isEmpty()) {
						serverClient.send(new rinitech.tcp.errors.PacketDataIncorrect().toPacket(packet.getId()), true);
						break;
					}

					System.out.println("Received part of file: " + imageUploadPart.data.id);

					if (!server.getFileUploads().contains(imageUploadPart.data.id)) {
						serverClient.send(new rinitech.tcp.errors.PacketDataIncorrect().toPacket(packet.getId()), true);
						break;
					}

					Collection<File> all = new ArrayList<>(Arrays.asList(Objects.requireNonNull(new File("images/").listFiles())));
					File file = null;

					for (File fileI : all) {
						if (fileI.getName().startsWith(imageUploadPart.data.id)) file = fileI;
					}

					if (file == null) {
						serverClient.send(new rinitech.tcp.errors.PacketDataIncorrect().toPacket(packet.getId()), true);
						break;
					}

					FileOutputStream fileOutputStream = new FileOutputStream(file, true);
					fileOutputStream.write(Base64.getDecoder().decode(imageUploadPart.data.data));
					fileOutputStream.close();
				}
			}

			case ImageUploadCompleted -> {
				if (serverClient.getStatus() == ClientStatus.Connected) {
					rinitech.tcp.packets.json.ImageUploadComplete imageUploadComplete = (ImageUploadComplete) packet.getData();
					if (imageUploadComplete.data.id == null || imageUploadComplete.data.id.isEmpty()) {
						serverClient.send(new rinitech.tcp.errors.PacketDataIncorrect().toPacket(packet.getId()), true);
						break;
					}

					if (!server.getFileUploads().contains(imageUploadComplete.data.id)) {
						serverClient.send(new rinitech.tcp.errors.PacketDataIncorrect().toPacket(packet.getId()), true);
						break;
					}

					server.getFileUploads().remove(imageUploadComplete.data.id);
				}
			}

			case RequestImageDownload -> {
				if (serverClient.getStatus() == ClientStatus.Connected) {
					rinitech.tcp.packets.json.RequestImageDownload requestImageDownload = (RequestImageDownload) packet.getData();
					if (requestImageDownload.data.id == null || requestImageDownload.data.id.isEmpty()) {
						serverClient.send(new rinitech.tcp.errors.PacketDataIncorrect().toPacket(packet.getId()), true);
						break;
					}

					Collection<File> all = new ArrayList<>(Arrays.asList(Objects.requireNonNull(new File("images/").listFiles())));

					all.stream().findAny().ifPresent(file -> {
						if (file.getName().startsWith(requestImageDownload.data.id)) {
							try {
								String mime = Files.probeContentType(file.toPath());
								if (mime == null) {
									serverClient.send(new rinitech.tcp.errors.PacketDataIncorrect().toPacket(packet.getId()), true);
									return;
								}

								rinitech.tcp.packets.json.ImageDownloadMeta imageDownloadMeta = new ImageDownloadMeta();
								imageDownloadMeta.data = new ImageDownloadMetaData();
								imageDownloadMeta.data.id = requestImageDownload.data.id;
								imageDownloadMeta.data.type = mime;
								imageDownloadMeta.data.size = (int) file.length();
								serverClient.send(new MCPPacket(MajorPacketType.File, FilePacketType.ImageDownloadMeta, imageDownloadMeta, packet.getId()), true);

								try { Thread.sleep(100); } catch (InterruptedException ignored) {}
								FileInputStream fileInputStream = new FileInputStream(file);
								byte[] buffer = new byte[4 * 1024];
								int read;
								int i = 0;
								while ((read = fileInputStream.read(buffer)) != -1) {
									i++;
									rinitech.tcp.packets.json.ImageDownloadPart imageDownloadPart = new ImageDownloadPart();
									imageDownloadPart.data = new ImageDownloadPartData();
									imageDownloadPart.data.id = requestImageDownload.data.id;
									imageDownloadPart.data.data = Base64.getEncoder().encodeToString(buffer);
									imageDownloadPart.data.part = i;
									System.out.println("Sending part " + i);
									serverClient.send(new MCPPacket(MajorPacketType.File, FilePacketType.ImageDownloadPart, imageDownloadPart, packet.getId()), true);
									try { Thread.sleep(10); } catch (InterruptedException ignored) {}
								}
								fileInputStream.close();

								rinitech.tcp.packets.json.ImageDownloadCompleted imageDownloadCompleted = new ImageDownloadCompleted();
								imageDownloadCompleted.data = new ImageDownloadCompletedData();
								imageDownloadCompleted.data.id = requestImageDownload.data.id;

								serverClient.send(new MCPPacket(MajorPacketType.File, FilePacketType.ImageDownloadCompleted, imageDownloadCompleted, packet.getId()), true);
							} catch (IOException e) {
								e.printStackTrace();
							}
						} else {
							serverClient.send(new rinitech.tcp.errors.PacketDataIncorrect().toPacket(packet.getId()), true);
						}
					});
				}
			}
		}
	}

	private static void handleUser(MCPPacket packet, ServerClient client, Server server)
	{
		switch ((UserPacketType) packet.getMinorPacketType()) {
			case Create -> {
				if (client.getStatus() == ClientStatus.Connected) {
					if (!client.isRoot()) client.send(new AccessDenied().toPacket(packet.getId()), true);
					rinitech.tcp.packets.json.UserCreate createUser = (UserCreate) packet.getData();
					if (createUser.data.username == null || createUser.data.username.isEmpty() || createUser.data.password == null || createUser.data.password.isEmpty()) {
						client.send(new rinitech.tcp.errors.PacketDataIncorrect().toPacket(packet.getId()), true);
						break;
					}

					if (server.getDatabase().getUser(createUser.data.username) != null || createUser.data.username.equals(server.getConfig().rootUsername)) {
						client.send(new rinitech.tcp.errors.UserAlreadyExists().toPacket(packet.getId()), true);
						break;
					}

					server.getDatabase().addUser(createUser.data.username, createUser.data.password);
					UserCreated userCreated = new UserCreated();
					userCreated.data = new UserCreatedData();
					userCreated.data.username = createUser.data.username;
					MCPPacket packetToSend = new MCPPacket(MajorPacketType.User, UserPacketType.Created, userCreated, packet.getId());
					client.send(packetToSend, true);
				}
			}
			case Delete -> {
				if (client.getStatus() == ClientStatus.Connected) {
					if (!client.isRoot()) client.send(new AccessDenied().toPacket(packet.getId()), true);
					rinitech.tcp.packets.json.UserDelete deleteUser = (UserDelete) packet.getData();
					if (deleteUser.data.username == null || deleteUser.data.username.isEmpty()) {
						client.send(new rinitech.tcp.errors.PacketDataIncorrect().toPacket(packet.getId()), true);
						break;
					}

					if (server.getDatabase().getUser(deleteUser.data.username) == null) {
						client.send(new rinitech.tcp.errors.UserNotFound().toPacket(packet.getId()), true);
						break;
					}

					server.getDatabase().deleteUser(deleteUser.data.username);
					UserDeleted userDeleted = new UserDeleted();
					userDeleted.data = new UserDeletedData();
					userDeleted.data.username = deleteUser.data.username;
					MCPPacket packetToSend = new MCPPacket(MajorPacketType.User, UserPacketType.Deleted, userDeleted, packet.getId());
					client.send(packetToSend, true);
				}
			}
			case Update -> {
				if (client.getStatus() == ClientStatus.Connected) {
					rinitech.tcp.packets.json.UserUpdate updateUser = (UserUpdate) packet.getData();
					String newUsername = updateUser.data.username;
					String newPassword = updateUser.data.password;
					String newAvatar = updateUser.data.avatar;
					if ((newUsername == null || newUsername.isEmpty()) && (newPassword == null || newPassword.isEmpty()) && (newAvatar == null || newAvatar.isEmpty())) {
						client.send(new rinitech.tcp.errors.PacketDataIncorrect().toPacket(packet.getId()), true);
						break;
					}

					if (server.getDatabase().getUser(newUsername) != null || newUsername.equals(server.getConfig().rootUsername) || newUsername.equals(client.getUsername())) {
						client.send(new rinitech.tcp.errors.UserAlreadyExists().toPacket(packet.getId()), true);
						break;
					}

					if (newUsername != null && !newUsername.isEmpty()) {
						server.getDatabase().updateUserUsername(client.getUsername(), newUsername);
					} else if (newPassword != null && !newPassword.isEmpty()) {
						server.getDatabase().updateUserPassword(client.getUsername(), newPassword);
					} else if (newAvatar != null && !newAvatar.isEmpty()) {
						server.getDatabase().updateUserAvatar(client.getUsername(), newAvatar);
					}

					UserUpdated userUpdated = new UserUpdated();
					userUpdated.data = new UserUpdatedData();
					userUpdated.data.id = client.getId();
					userUpdated.data.username = newUsername;
					userUpdated.data.avatar = newAvatar;

					MCPPacket packetToSend = new MCPPacket(MajorPacketType.User, UserPacketType.Updated, userUpdated, "DontHaveID");
					server.getRooms().forEach((room) -> {
						if (room.getUsers().contains(client)) room.broadcast(packetToSend);
					});
				}
			}
			case GetInfo -> {
				if (client.getStatus() == ClientStatus.Connected) {
					rinitech.tcp.packets.json.UserGetInfo getUserInfo = (UserGetInfo) packet.getData();
					if (getUserInfo.data.username == null || getUserInfo.data.username.isEmpty()) {
						client.send(new rinitech.tcp.errors.PacketDataIncorrect().toPacket(packet.getId()), true);
						break;
					}

					User user = server.getDatabase().getUser(getUserInfo.data.username);
					if (user == null) {
						client.send(new rinitech.tcp.errors.UserNotFound().toPacket(packet.getId()), true);
						break;
					}

					UserInfo userInfo = new UserInfo();
					userInfo.data = new UserInfoData();
					userInfo.data.id = user.id;
					userInfo.data.username = user.username;
					userInfo.data.avatar = user.avatar;
					MCPPacket packetToSend = new MCPPacket(MajorPacketType.User, UserPacketType.Info, userInfo, packet.getId());
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
					if (!serverClient.isRoot()) serverClient.send(new AccessDenied().toPacket(packet.getId()), true);
					rinitech.tcp.packets.json.Create createRoom = (Create) packet.getData();
					if (createRoom.data.name == null || createRoom.data.name.isEmpty()) {
						serverClient.send(new rinitech.tcp.errors.PacketDataIncorrect().toPacket(packet.getId()), true);
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
					MCPPacket packetToSend = new MCPPacket(MajorPacketType.Room, RoomPacketType.Created, created, packet.getId());

					serverClient.send(packetToSend, true);
				}
			}
			case Join -> {
				if (serverClient.getStatus() == ClientStatus.Connected) {
					if (!serverClient.isRoot()) serverClient.send(new AccessDenied().toPacket(packet.getId()), true);
					rinitech.tcp.packets.json.Join joinRoom = (Join) packet.getData();

					rinitech.tcp.Room room = server.getRooms().stream().filter(r -> r.getId() == joinRoom.data.room).findFirst().orElse(null);
					if (room == null) {
						serverClient.send(new rinitech.tcp.errors.RoomNotFound().toPacket(packet.getId()), true);
						break;
					}

					room.getUsers().add(serverClient);
					SerializableUser[] users = new SerializableUser[room.getUsers().size()];
					for (int i = 0; i < room.getUsers().size(); i++) {
						User user = server.getDatabase().getUser(room.getUsers().get(i).getId());
						users[i] = new SerializableUser(user);
					}

					Joined joined = new Joined();
					joined.data = new JoinedData();
					joined.data.room = room.getId();
					joined.data.user = serverClient.getUsername();
					joined.data.allUsers = users;
					MCPPacket packetToSend = new MCPPacket(MajorPacketType.Room, RoomPacketType.Joined, joined, "DontHaveID");

					room.broadcast(packetToSend);
				}
			}
			case Leave -> {
				if (serverClient.getStatus() == ClientStatus.Connected) {
					if (!serverClient.isRoot()) serverClient.send(new AccessDenied().toPacket(packet.getId()), true);
					rinitech.tcp.packets.json.Leave leaveRoom = (Leave) packet.getData();

					rinitech.tcp.Room room = server.getRooms().stream().filter(r -> r.getId() == leaveRoom.data.room).findFirst().orElse(null);
					if (room == null) {
						serverClient.send(new rinitech.tcp.errors.RoomNotFound().toPacket(packet.getId()), true);
						break;
					}

					room.getUsers().remove(serverClient);

					Left left = new Left();
					left.data = new LeftData();
					left.data.room = room.getId();
					left.data.user = serverClient.getUsername();
					MCPPacket packetToSend = new MCPPacket(MajorPacketType.Room, RoomPacketType.Left, left, "DontHaveID");

					serverClient.send(packetToSend, true);
					room.broadcast(packetToSend);
				}
			}
			case Update -> {
				if (serverClient.getStatus() == ClientStatus.Connected) {
					if (!serverClient.isRoot()) serverClient.send(new AccessDenied().toPacket(packet.getId()), true);
					rinitech.tcp.packets.json.Update updateRoom = (Update) packet.getData();

					rinitech.tcp.Room room = server.getRooms().stream().filter(r -> r.getId() == updateRoom.data.room).findFirst().orElse(null);
					if (room == null) {
						serverClient.send(new rinitech.tcp.errors.RoomNotFound().toPacket(packet.getId()), true);
						break;
					}

					room.setName(updateRoom.data.name);
					server.getDatabase().updateRoom(room.getId(), room.getName());

					Updated updated = new Updated();
					updated.data = new UpdatedData();
					updated.data.room = room.getId();
					updated.data.name = room.getName();
					MCPPacket packetToSend = new MCPPacket(MajorPacketType.Room, RoomPacketType.Updated, updated, "DontHaveID");

					serverClient.send(packetToSend, true);
					room.broadcast(packetToSend);
				}
			}
			case Delete -> {
				if (serverClient.getStatus() == ClientStatus.Connected) {
					if (!serverClient.isRoot()) serverClient.send(new AccessDenied().toPacket(packet.getId()), true);
					rinitech.tcp.packets.json.Delete deleteRoom = (Delete) packet.getData();

					rinitech.tcp.Room room = server.getRooms().stream().filter(r -> r.getId() == deleteRoom.data.room).findFirst().orElse(null);
					if (room == null) {
						serverClient.send(new rinitech.tcp.errors.RoomNotFound().toPacket(packet.getId()), true);
						break;
					}

					server.getRooms().remove(room);
					server.getDatabase().deleteRoom(room.getId());

					Deleted deleted = new Deleted();
					deleted.data = new DeletedData();
					deleted.data.room = room.getId();
					MCPPacket packetToSend = new MCPPacket(MajorPacketType.Room, RoomPacketType.Deleted, deleted, "DontHaveID");

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
