package rinitech.tcp;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.BufferedReader;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Client extends Thread
{
	private final Socket socket;
	private final String username;
	private final BufferedReader reader;
	private final SecretKey secretKey;
	private final String accessToken;

	public Client(Socket socket, String username, BufferedReader reader, SecretKey secretKey, String accessToken)
	{
		this.socket = socket;
		this.username = username;
		this.reader = reader;
		this.secretKey = secretKey;
		this.accessToken = accessToken;
	}

	public String encrypt(String message) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
	{
		IvParameterSpec iv = new IvParameterSpec(new byte[16]);
		Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
		cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
		byte[] encrypted = cipher.doFinal(message.getBytes());
		return Base64.getEncoder().encodeToString(encrypted);
	}

	public String decrypt(String message)  throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
	{
		IvParameterSpec iv = new IvParameterSpec(new byte[16]);
		Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
		cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
		byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(message));
		return new String(decrypted);
	}
}
