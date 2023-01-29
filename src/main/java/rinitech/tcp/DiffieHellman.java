package rinitech.tcp;


import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPublicKeySpec;
import java.math.BigInteger;
import java.security.*;

public class DiffieHellman
{
	private KeyPair keyPair;
	private KeyAgreement keyAgree;
	private DHParameterSpec dhParamSpec;

	public DiffieHellman() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException
	{
		dhParamSpec = new DHParameterSpec(P, G);
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("DiffieHellman");
		keyPairGen.initialize(dhParamSpec);
		keyPair = keyPairGen.generateKeyPair();
		keyAgree = KeyAgreement.getInstance("DiffieHellman");
		keyAgree.init(keyPair.getPrivate());
	}
	public byte[] generatePublicKey()
	{
		try {
			BigInteger pubKeyBI = ((DHPublicKey) keyPair.getPublic()).getY();
			return pubKeyBI.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public byte[] generateSharedSecret(byte[] publicKey)
	{
		if (keyAgree == null) {
			throw new IllegalStateException("Must generate public key first");
		}
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("DiffieHellman");
			BigInteger pubKeyBI = new BigInteger(1, publicKey);
			DHPublicKey pubKey = (DHPublicKey) keyFactory.generatePublic(new DHPublicKeySpec(pubKeyBI, P, G));
			keyAgree.doPhase(pubKey, true);
			return keyAgree.generateSecret();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static final String modp5 = (
			"FFFFFFFF FFFFFFFF C90FDAA2 2168C234 C4C6628B 80DC1CD1" +
			"29024E08 8A67CC74 020BBEA6 3B139B22 514A0879 8E3404DD" +
			"EF9519B3 CD3A431B 302B0A6D F25F1437 4FE1356D 6D51C245" +
			"E485B576 625E7EC6 F44C42E9 A637ED6B 0BFF5CB6 F406B7ED" +
			"EE386BFB 5A899FA5 AE9F2411 7C4B1FE6 49286651 ECE45B3D" +
			"C2007CB8 A163BF05 98DA4836 1C55D39A 69163FA8 FD24CF5F" +
			"83655D23 DCA3AD96 1C62F356 208552BB 9ED52907 7096966D" +
			"670C354E 4ABC9804 F1746C08 CA237327 FFFFFFFF FFFFFFFF")
			.replaceAll("\\s", "");
	private static final BigInteger P = new BigInteger(modp5, 16);

	private static final BigInteger G = BigInteger.valueOf(2L);
}