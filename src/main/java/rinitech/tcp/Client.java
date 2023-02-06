package rinitech.tcp;

import rinitech.tcp.gateway.ClientIncomingGateway;
import rinitech.tcp.gateway.ClientOutgoingGateway;
import rinitech.tcp.packets.MCPPacket;
import rinitech.tcp.types.ClientEvent;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Scanner;

public class Client
{
	private final Scanner reader;
	private final PrintWriter writer;
	private String username;
	private SecretKey secretKey;
	private String accessToken;
	private final EventEmitter<MCPPacket> events = new EventEmitter<>();
	private final DiffieHellman DH = new DiffieHellman();
	private final IvParameterSpec iv = new IvParameterSpec(new byte[16]);

	public Client(Socket serverSocket, String username, Scanner reader, PrintWriter writer, SecretKey secretKey, String accessToken) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException
	{
		this.username = username;
		this.reader = reader;
		this.writer = writer;
		this.secretKey = secretKey;
		this.accessToken = accessToken;

		try {
			new Thread(() -> {
				while (!serverSocket.isClosed()) {
					if (reader.hasNext()) {
						String clientMessage = reader.nextLine();
						MCPPacket packet;
						try {
							clientMessage = decrypt(clientMessage);
						} catch (Exception ignored) {

						} finally {
							packet = MCPPacket.parse(clientMessage);
						}

						ClientIncomingGateway.handle(this, packet);
					}
				}
			}).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Client createConnection(String host, int port, String username, String password) {
		try {
			Socket socket = new Socket(host, port);
			Scanner reader = new Scanner(socket.getInputStream());
			PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
			Client client = new Client(socket, username, reader, writer, null, null);
			ClientOutgoingGateway.sendHandshake(client);
			client.events.on(ClientEvent.HandshakeSuccess, (packet) -> {
				ClientOutgoingGateway.sendLogin(client, client.username, password);
			});
			return client;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
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

	public void send(MCPPacket packet, boolean encrypt)
	{
		if (encrypt) {
			try {
				writer.println(encrypt(packet.toJson()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else writer.println(packet.toJson());
	}

	public void setSecretKey(SecretKey secretKey)
	{
		this.secretKey = secretKey;
	}

	public void setAccessToken(String accessToken)
	{
		this.accessToken = accessToken;
	}

	public String getUsername()
	{
		return username;
	}

	public String getAccessToken()
	{
		return accessToken;
	}

	public EventEmitter<MCPPacket> getEvents()
	{
		return events;
	}

	public DiffieHellman getDH()
	{
		return DH;
	}
}