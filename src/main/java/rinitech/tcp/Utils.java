package rinitech.tcp;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class Utils
{
	public static byte[] hexStringToByteArray(String s)
	{
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2)
		{
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	public static String byteArrayToHexString(byte[] bytes)
	{
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes)
		{
			sb.append(String.format("%02X", b));
		}
		return sb.toString();
	}

	public static SecretKey generateSecretKey(String key)
	{
		byte[] decodedKey = java.util.Base64.getDecoder().decode(key);
		return new SecretKeySpec(decodedKey,"AES");
	}

	public static String generateAccessToken(String username)
	{
		byte[] array = new byte[8];
		new java.util.Random().nextBytes(array);

		return Base64.getEncoder().encodeToString(array) + Base64.getEncoder().encodeToString(username.getBytes());
	}
}
