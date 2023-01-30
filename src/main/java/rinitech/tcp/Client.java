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

import rinitech.tcp.handlers.HandleIncomingPacket;
import rinitech.tcp.packets.MCPPacket;
import rinitech.tcp.types.ClientStatus;

public class Client extends Thread
{
	private final Socket socket;
	private final Scanner reader;
	private final PrintWriter writer;
	public String username;
	private SecretKey secretKey;
	private String accessToken;
	public ClientStatus status = ClientStatus.Awaiting;
	private final Server server;

	public Client(Socket socket, String username, Scanner reader, PrintWriter writer, SecretKey secretKey, String accessToken, Server server)
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

	public static Client fromSocket(Socket socket, Server server) throws IOException
	{
		Scanner reader = new Scanner(socket.getInputStream());
		PrintWriter writer = new PrintWriter(socket.getOutputStream());

		return new Client(socket, null, reader, writer, null, null, server);
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
		// TODO: Create timer for send new accessToken every 10 minutes
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
					System.out.println(packet.getMajorPacketType() + " " + packet.getMinorPacketType() + " " + packet.getData());

					System.out.println(clientMessage);
					HandleIncomingPacket.handle(packet, this, server);
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
}
