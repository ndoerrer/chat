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

/**	Crypto class
*	This class implements means for encrypted and signed communication.
*	This includes DH key exchange and RSA key generation.
*	RSA keys are used to sign and verify authorship of messages.
*	DH keys are used to create a pairwise shared secret for message encryption.
*/
public class Crypto{
	private KeyPair DHkey_pair;
	private KeyPair RSAkey_pair;
	private byte[] shared_secret = null;
	private KeyAgreement key_agree;
	private final int DHkey_size;
	private final int RSAkey_size;
	private PublicKey foreign_RSA_key;

	/**	Crypto constructor
	*	This constructor sets the key sizes in bits for DH and RSA keys.
	*/
	public Crypto(){
		DHkey_size = 512; 			//multiple of 64, must be in 512 to 8192
 		RSAkey_size = 2048;
	}

	/**	generateDHKeyPair method
	*	This method generates a new Diffie Hellman keypair. Resulting
	*	keypair is stored in a private variable.
	*	@returns true, if key generation was successful.
	*/
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

	/**	generateRSAKeyPair method
	*	This method generates a new RSA public-private key pair.
	*	Resulting keypair is stored in a private variable.
	*	@returns true, if key generation was successful.
	*/
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

	/**	getDHPublicKey method
	*	This method returns the public DH key, if initialized.
	*	@returns public DH key.
	*/
	public PublicKey getDHPublicKey(){
		if (DHkey_pair == null)
			return null;		//TODO: catch on client and serverside
		return DHkey_pair.getPublic();
	}

	/**	getRSAPublicKey method
	*	This method returns the public RSAkey, if initialized.
	*	@returns public RSA key.
	*/
	public PublicKey getRSAPublicKey(){
		if (RSAkey_pair == null)
			return null;		//TODO: catch on client and serverside
		return RSAkey_pair.getPublic();
	}

	/**	computeSharedSecret method
	*	This method takes a foreign public DH key and computes a shared secret
	*	to use as symmetric key for encrypting messages.
	*	@param foreign_key: foreign public DH key to compute shared secret from.
	*	@returns true, if secret generation was successful.
	*/
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

	/**	encrypt method
	*	This method gets a String text and encrypts it using the shared
	*	secret with AES encryption. Result is returned in Base64.
	*	@param text: text to encrypt.
	*	@returns encrypted text in Base64 encoding.
	*/
	public String encrypt(String text){
		//byte[] bytes = Base64.getDecoder().decode(text);
		//System.out.println("DEBUG: text length = "+text.length());
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

	/**	decrypt method
	*	This method gets a Base64 encoded encrypted String text and decrypts
	*	it using the shared secret with AES decryption.
	*	Result is returned as ascii text.
	*	@param text: Base64 encrypted text to decrypt.
	*	@returns decrypted text as ascii.
	*/
	public String decrypt(String cipher_text){
		byte[] bytes = Base64.getDecoder().decode(cipher_text);
		//System.out.println("DEBUG: cipher_text length = "+cipher_text.length());
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

	/**	getHash method
	*	This method computes the SHA-256 hash of given text.
	*	@param text: text String to compute hash from.
	*	@returns hash sequence as byte array.
	*/
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

	/**	getSignature method
	*	This method computes the RSA signature of a text
	*	so the private key encrypted hash of the text.
	*	@param text to sign.
	*	@returns signature for the text.
	*/
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

	/**	verifySignature method
	*	This method verifies ownership of a signed text using a known public
	*	key, the text and the signature itself.
	*	@param text: text to check signature for.
	*	@param signature: signature of the text.
	*	@param key: key to check signature against.
	*	@returns true, if owner of the key is author of the signature
	*/
	public boolean verifySignature(String text, String signature, Key key){
		//System.out.println("DEBUG: text="+text);
		byte [] hash_from_text = getHash(text);
		byte [] hash_from_signature;
		byte [] bytes = Base64.getDecoder().decode(signature);
		try {
			Cipher c = Cipher.getInstance("RSA");
			c.init(Cipher.DECRYPT_MODE, key);
			hash_from_signature = c.doFinal(bytes);
			//System.out.println("DEBUG: hash_from_text" + new String(hash_from_text));
			//System.out.println("DEBUG: hash_from_signature" + new String(hash_from_signature));
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

	/**	setForeignRSAKey method
	*	This method stores a foreign RSA public key.
	*	@param foreign_key_in: Key to store.
	*/
	public void setForeignRSAKey(PublicKey foreign_key_in){
		foreign_RSA_key = foreign_key_in;
	}

	/**	getForeignRSAKey method
	*	This method returns the currently stored foreign RSA public key.
	*	@returns currently stored foreign RSA public key.
	*/
	public PublicKey getForeignRSAKey(){
		return foreign_RSA_key;
	}

	/**	toHexString method
	*	This method converts a byte array to a hexadecimal string.
	*	@param bytes: byte sequence to translate.
	*	@returns hexadecimal string representation.
	*/
	public static String toHexString(byte [] bytes){
		String result = "";
		for (byte b : bytes)
			result += String.format("%02x", b);
		return result;
	}
}
