package rinitech.tcp;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

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

	public static SecretKey generateSecretKey(String key)
	{
		byte[] decodedKey = java.util.Base64.getDecoder().decode(key);
		return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
	}
}
