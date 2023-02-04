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
	public String username;
	private SecretKey secretKey;
	private String accessToken;
	public ClientStatus status = ClientStatus.Awaiting;
	private final Server server;
	public boolean isRoot = false;

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
		IvParameterSpec iv = new IvParameterSpec(new byte[16]);
		Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
		cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
		byte[] encrypted = cipher.doFinal(message.getBytes());
		return Base64.getEncoder().encodeToString(encrypted);
	}

	public String decrypt(String message) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
	{
		IvParameterSpec iv = new IvParameterSpec(new byte[16]);
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
						clientMessage = decrypt(clientMessage);
					} catch (Exception ignored) {

					} finally {
						packet = MCPPacket.parse(clientMessage);
					}
					ServerIncomingGateway.handle(packet, this, server);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setSecretKey(SecretKey secretKey)
	{
		this.secretKey = secretKey;
	}

	public void setAccessToken(String accessToken)
	{
		this.accessToken = accessToken;
	}

	public String getAccessToken() { return accessToken; }

	public void createUpdateAccessTokenTimer() {
		new Thread(() -> {
			while (!socket.isClosed()) {
				try {
					Thread.sleep(1000 * 60 * 60 * 24);
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
}
