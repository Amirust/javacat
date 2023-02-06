package rinitech.tcp;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Scanner;

import rinitech.tcp.errors.PacketDataIncorrect;
import rinitech.tcp.gateway.ServerIncomingGateway;
import rinitech.tcp.packets.MCPPacket;
import rinitech.tcp.packets.json.UpdateAccessToken;
import rinitech.tcp.packets.json.UpdateAccessTokenData;
import rinitech.tcp.types.AuthenticationPacketType;
import rinitech.tcp.types.ClientStatus;
import rinitech.tcp.types.MajorPacketType;

public class ServerClient extends Thread
{
	private final Socket socket;
	private final Scanner reader;
	private final PrintWriter writer;
	private String username;
	private SecretKey secretKey;
	private String accessToken;
	private ClientStatus status = ClientStatus.Awaiting;
	private final Server server;
	private boolean isRoot = false;
	private final IvParameterSpec iv = new IvParameterSpec(new byte[16]);

	public ServerClient(Socket socket, String username, Scanner reader, PrintWriter writer, SecretKey secretKey, String accessToken, Server server)
	{
		this.socket = socket;
		this.username = username;
		this.reader = reader;
		this.writer = writer;
		this.secretKey = secretKey;
		this.accessToken = accessToken;
		this.server = server;
	}

	public String encrypt(String message) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
	{
		Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
		cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
		byte[] encrypted = cipher.doFinal(message.getBytes());
		return Base64.getEncoder().encodeToString(encrypted);
	}

	public String decrypt(String message) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
	{
		Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
		cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
		byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(message));
		return new String(decrypted);
	}

	public static ServerClient fromSocket(Socket socket, Server server) throws IOException
	{
		Scanner reader = new Scanner(socket.getInputStream());
		PrintWriter writer = new PrintWriter(socket.getOutputStream());

		return new ServerClient(socket, null, reader, writer, null, null, server);
	}

	public void send(MCPPacket packet, boolean encrypt)
	{
		String message = packet.toJson();
		if (encrypt) {
			try {
				message = encrypt(message);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		writer.println(message);
		writer.flush();
	}

	public void run()
	{
		try {
			while (!socket.isClosed()) {
				if (reader.hasNext()) {
					String clientMessage = reader.nextLine();
					MCPPacket packet;
					try {
						try {
							clientMessage = decrypt(clientMessage);
						} catch (Exception ignored) {

						} finally {
							packet = MCPPacket.parse(clientMessage);
						}
					} catch (Exception e) {
						send(new PacketDataIncorrect().toPacket(), false);
						continue;
					}
					ServerIncomingGateway.handle(packet, this, server);
				}
				Thread.sleep(100);
			}
		} catch (Exception e) {
			send(new PacketDataIncorrect().toPacket(), false);
			e.printStackTrace();
		}
	}

	public void createUpdateAccessTokenTimer() {
		new Thread(() -> {
			while (!socket.isClosed()) {
				try {
					Thread.sleep(60 * 1000 * 10);
					String newAccessToken = Utils.generateAccessToken(username);
					UpdateAccessToken updateAccessToken = new UpdateAccessToken();
					updateAccessToken.data = new UpdateAccessTokenData();
					updateAccessToken.data.accessToken = newAccessToken;
					MCPPacket packet = new MCPPacket(
							MajorPacketType.Authentication,
							AuthenticationPacketType.UpdateAccessToken,
							updateAccessToken
					);
					send(packet, true);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void stopClient() {
		try {
			writer.close();
			reader.close();
			socket.close();
		} catch (IOException ignored) {}
	}

	public void setSecretKey(SecretKey secretKey)
	{
		this.secretKey = secretKey;
	}

	public SecretKey getSecretKey()
	{
		return secretKey;
	}

	public void setAccessToken(String accessToken)
	{
		this.accessToken = accessToken;
	}

	public String getAccessToken()
	{
		return accessToken;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getUsername()
	{
		return username;
	}

	public boolean isRoot()
	{
		return isRoot;
	}

	public void setRoot(boolean root)
	{
		isRoot = root;
	}

	public ClientStatus getStatus() { return status; }

	public void setStatus(ClientStatus status)
	{
		this.status = status;
	}
}
