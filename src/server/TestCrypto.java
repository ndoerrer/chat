package chAT.server;

import chAT.global.*;

import java.security.PublicKey;
import java.util.Base64;

public class TestCrypto{
	public static String toHexString(byte [] bytes){
		String result = "";
		for (byte b : bytes)
			result += String.format("%02x", b);
		return result;
	}

	public static void main(String [] args){
		Crypto alice_crypto = new Crypto();
		Crypto bob_crypto = new Crypto();

		alice_crypto.generateDHKeyPair();
		bob_crypto.generateDHKeyPair();

		PublicKey alice_cryptopublic_key = alice_crypto.getDHPublicKey();
		PublicKey bob_cryptopublic_key = bob_crypto.getDHPublicKey();

		System.out.println("alice's public key: " + toHexString(alice_cryptopublic_key.getEncoded()));
		System.out.println("bob's public key: " + toHexString(bob_cryptopublic_key.getEncoded()));

		alice_crypto.computeSharedSecret(bob_cryptopublic_key);
		bob_crypto.computeSharedSecret(alice_cryptopublic_key);

		alice_crypto.generateRSAKeyPair();
		bob_crypto.generateRSAKeyPair();

		PublicKey alice_RSApublic_key = alice_crypto.getRSAPublicKey();
		PublicKey bob_RSApublic_key = bob_crypto.getRSAPublicKey();

		System.out.println("alice's public key: " + toHexString(alice_RSApublic_key.getEncoded()));
		System.out.println("bob's public key: " + toHexString(bob_RSApublic_key.getEncoded()));

		//String msg = "the turtle moves!";
		String msg = "It was the dawn of the third age of mankind.";
		msg += " Ten years after the Earth Minbari war.";
		System.out.println("message = " + msg);

		String alice_signature = alice_crypto.getSignature(msg);
		String bob_signature = bob_crypto.getSignature(msg);

		System.out.println("alice: msg signature: " + alice_signature);
		System.out.println("bob: msg signature: " + bob_signature);

		String encrypted_by_alice = alice_crypto.encrypt(msg);
		String encrypted_by_bob = bob_crypto.encrypt(msg);

		System.out.println("encrypted by alice: "+ encrypted_by_alice);
		System.out.println("encrypted by bob: "+ encrypted_by_bob);
		//message exchange
		String decrypted_by_alice = alice_crypto.decrypt(encrypted_by_bob);
		String decrypted_by_bob = bob_crypto.decrypt(encrypted_by_alice);

		System.out.println("decrypted by alice: "+ decrypted_by_alice);
		System.out.println("decrypted by bob: "+ decrypted_by_bob);

		boolean alice_check = alice_crypto.verifySignature(msg, alice_signature, alice_RSApublic_key);
		boolean bob_check = alice_crypto.verifySignature(msg, bob_signature, bob_RSApublic_key);

		System.out.println("alice check:" + alice_check);
		System.out.println("bob check:" + bob_check);
	}
}
