package chAT.global;

import java.security.KeyPairGenerator;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import javax.crypto.KeyAgreement;
import java.security.KeyFactory;
import java.security.InvalidKeyException;
import javax.crypto.BadPaddingException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.MessageDigest;

import java.util.Base64;

public class Crypto{
	private KeyPair DHkey_pair;
	private KeyPair RSAkey_pair;
	private byte[] shared_secret = null;
	private KeyAgreement key_agree;
	private final int DHkey_size;
	private final int RSAkey_size;
	private PublicKey foreign_RSA_key;

	public Crypto(){
		DHkey_size = 512; 				//multiple of 64 and can only range from 512 to 8192
 		RSAkey_size = 2048;
	}

	public boolean generateDHKeyPair(){
		try{
			KeyPairGenerator key_pair_gen = KeyPairGenerator.getInstance("DH");
			key_pair_gen.initialize(DHkey_size, new SecureRandom());
			DHkey_pair = key_pair_gen.generateKeyPair();
			key_agree = KeyAgreement.getInstance("DH");
			key_agree.init(DHkey_pair.getPrivate());
			return true;
		} catch (NoSuchAlgorithmException e){
			System.out.println("NoSuchAlgorithmException in generateKeyPair()");
		} catch (InvalidKeyException e){
			System.out.println("InvalidKeyException in generateKeyPair()");
		}
		return false;
	}

	public boolean generateRSAKeyPair(){
		try{
			KeyPairGenerator key_pair_gen = KeyPairGenerator.getInstance("RSA");
			key_pair_gen.initialize(RSAkey_size, new SecureRandom());
			RSAkey_pair = key_pair_gen.generateKeyPair();
			return true;
		} catch (NoSuchAlgorithmException e){
			System.out.println("NoSuchAlgorithmException in generateKeyPair()");
		}
		return false;
	}

	public PublicKey getDHPublicKey(){
		if (DHkey_pair == null)
			return null;		//TODO: catch on client and serverside
		return DHkey_pair.getPublic();
	}

	public PublicKey getRSAPublicKey(){
		if (RSAkey_pair == null)
			return null;		//TODO: catch on client and serverside
		return RSAkey_pair.getPublic();
	}

	public boolean computeSharedSecret(PublicKey foreign_key){
		try{
			key_agree.doPhase(foreign_key, true);
			shared_secret = key_agree.generateSecret();
			//System.out.println("DEBUG: shared secret="+toHexString(shared_secret));
			return true;
		} catch (InvalidKeyException e){
			System.out.println("InvalidKeyException in computeSharedKey()");
		}
		return false;
	}

	public String encrypt(String text){
		//byte[] bytes = Base64.getDecoder().decode(text);
		System.out.println("DEBUG: text length = "+text.length());
		try {
			Key key = new SecretKeySpec(shared_secret, 0, 16, "AES");
			Cipher c = Cipher.getInstance("AES");
			c.init(Cipher.ENCRYPT_MODE, key);
			return Base64.getEncoder().encodeToString(c.doFinal(text.getBytes()));//bytes
		} catch (InvalidKeyException e){
			System.out.println("InvalidKeyException in encrypt");
		} catch (BadPaddingException | NoSuchPaddingException | IllegalBlockSizeException e){
			System.out.println("BadPaddingException or NoSuchPaddingException or "+
								"IllegalBlockSizeException in encrypt");
		} catch (NoSuchAlgorithmException e) {
			System.out.println("NoSuchAlgorithmException in encrypt");
		}
		return null;
	}

	public String decrypt(String cipher_text){
		byte[] bytes = Base64.getDecoder().decode(cipher_text);
		System.out.println("DEBUG: cipher_text length = "+cipher_text.length());
		try {
			Key key = new SecretKeySpec(shared_secret, 0, 16, "AES");
			Cipher c = Cipher.getInstance("AES");
			c.init(Cipher.DECRYPT_MODE, key);
			return new String(c.doFinal(bytes));
		} catch (InvalidKeyException e){
			System.out.println("InvalidKeyException in decrypt");
		} catch (BadPaddingException | NoSuchPaddingException | IllegalBlockSizeException e){
			System.out.println("BadPaddingException or NoSuchPaddingException or "+
								"IllegalBlockSizeException in decrypt\n"+e);
		} catch (NoSuchAlgorithmException e) {
			System.out.println("NoSuchAlgorithmException in decrypt");
		}
		return null;
	}

	public byte [] getHash(String text){
		String hash = "";
		try{
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return digest.digest(text.getBytes());//StandardCharsets.UTF_8
		} catch (NoSuchAlgorithmException e){
			System.out.println("NoSuchAlgorithmException in getSignature");
		}
		return null;
	}

	public String getSignature(String text){
		byte [] hash = getHash(text);
		try {
			Cipher c = Cipher.getInstance("RSA");
			c.init(Cipher.ENCRYPT_MODE, RSAkey_pair.getPrivate());
			return Base64.getEncoder().encodeToString(c.doFinal(hash));
		} catch (InvalidKeyException e){
			System.out.println("InvalidKeyException in sign_cleartext");
		} catch (BadPaddingException | NoSuchPaddingException | IllegalBlockSizeException e){
			System.out.println("BadPaddingException or NoSuchPaddingException or "+
								"IllegalBlockSizeException in sign_cleartext\n"+e);
		} catch (NoSuchAlgorithmException e) {
			System.out.println("NoSuchAlgorithmException in sign_cleartext");
		}
		return "";
	}

	public boolean verifySignature(String text, String signature, Key key){
		byte [] hash_from_text = getHash(text);
		byte [] hash_from_signature;
		byte [] bytes = Base64.getDecoder().decode(signature);
		try {
			Cipher c = Cipher.getInstance("RSA");
			c.init(Cipher.DECRYPT_MODE, key);
			hash_from_signature = c.doFinal(bytes);
			System.out.println("DEBUG: hash_from_text" + new String(hash_from_text));
			System.out.println("DEBUG: hash_from_signature" + new String(hash_from_signature));
			return ((new String(hash_from_text)).equals(new String(hash_from_signature)));
		} catch (InvalidKeyException e){
			System.out.println("InvalidKeyException in unsign_cleartext");
		} catch (BadPaddingException | NoSuchPaddingException | IllegalBlockSizeException e){
			System.out.println("BadPaddingException or NoSuchPaddingException or "+
								"IllegalBlockSizeException in unsign_cleartext\n"+e);
		} catch (NoSuchAlgorithmException e) {
			System.out.println("NoSuchAlgorithmException in unsign_cleartext");
		}
		return false;
	}

	public void setForeignRSAKey(PublicKey foreign_key_in){
		foreign_RSA_key = foreign_key_in;
	}

	public PublicKey getForeignRSAKey(){
		return foreign_RSA_key;
	}

	public static String toHexString(byte [] bytes){
		String result = "";
		for (byte b : bytes)
			result += String.format("%02x", b);
		return result;
	}
}
