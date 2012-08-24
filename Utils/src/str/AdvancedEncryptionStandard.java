package str;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.util.Base64;

// wss4j-1.6.4.jar

/**
 * This class encrypts and decrypts messages based on a passphrase and a salt. <BR>
 * It uses:<BR>
 * <UL>
 * <LI>the Advanced Encryption Standard key generation algorithm, as specified
 * by National Institute of Standards and Technology (NIST) in U.S. Federal
 * Information Processing Standard (FIPS) PUB 197 (FIPS 197)
 * <LI>SHA-256 Message Digest algorithm conforming to FIPS PUB 180-2
 * <LI>the PBEWithMD5AndDES cipher algorithm, the password-based encryption
 * algorithm as defined in: RSA Laboratories,
 * "PKCS #5: Password-Based Encryption Standard," version 1.5, Nov 1993, for key
 * generation
 * </UL>
 * <P>
 * This class requires JCE_POLICY-x.zip files, where x is the JRE release. The
 * JAR files in the zip need to be placed in jre/lib/security.
 * 
 * Without those files, you'll get a java.security.InvalidKeyException error when you run this file.
 * <P>
 * To fix this you have to install the Unlimited Strength Jurisdiction Policy Files into your
 * runtime class path.  You can find the files here:
 * <a href=https://cds.sun.com/is-bin/INTERSHOP.enfinity/WFS/CDS-CDS_Developer-Site/en_US/-/USD/ViewProductDetail-Start?ProductRef=jce_policy-6-oth-JPR@CDS-CDS_Developer>Policy Files</a>
 * <P>
 * Unzip and then copy the files to your [JAVA_HOME]/lib/security folder.  If your
 * JAVA_HOME is your JDK, then it will be java_home/jre/lib/security.
 */

public final class AdvancedEncryptionStandard
{
	/** The Constant SALT_SIZE_BYTES. */
	private static final int SALT_SIZE_BYTES = 20;

	/** The Constant HASH_ITERATIONS. */
	private static final int HASH_ITERATIONS = 1024;

	/**
	 * Do not use. All methods are static.
	 */
	private AdvancedEncryptionStandard()
	{
	}

	/**
	 * The static salt. Do not publish or publicize this! Used for encrypting
	 * system information.
	 */
	private static byte[] staticSalt =
			{ (byte) 0x24, (byte) 0x6f, (byte) 0x4c, (byte) 0xcb, (byte) 0xcb,
					(byte) 0x54 };

	/**
	 * Gets the secret key spec. The SecretKeySpec specifies a secret key in a
	 * provider-independent fashion.
	 * 
	 * @param passphrase
	 *            the non-null passphrase
	 * @param salt
	 *            the non-null salt
	 * 
	 * @return the secret key spec
	 * 
	 * @throws NoSuchAlgorithmException
	 *             the no such algorithm exception
	 * @throws InvalidKeySpecException
	 *             the invalid key spec exception
	 */
	public static SecretKeySpec getSecretKeySpec(final String passphrase,
			final byte[] salt) throws InvalidKeySpecException,
			NoSuchAlgorithmException
	{
		int iterationCount = HASH_ITERATIONS;

		KeySpec keySpec =
				new PBEKeySpec(passphrase.toCharArray(), salt, iterationCount);

		SecretKey secretKey =
				SecretKeyFactory.getInstance("PBEWithMD5AndDES")
						.generateSecret(keySpec);

		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(secretKey.getEncoded());
		md.update(salt);
		for (int i = 1; i < iterationCount; i++)
			md.update(md.digest());

		byte[] keyBytes = md.digest();
		SecretKeySpec skeyspec = new SecretKeySpec(keyBytes, "AES");
		return skeyspec;
	}

	/**
	 * Encrypt a message with static (JACOB) salt.
	 * 
	 * @param message
	 *            the non-null message
	 * @param passphrase
	 *            the non-null passphrase
	 * 
	 * @return the encrypted string
	 * 
	 * @throws NoSuchAlgorithmException
	 *             the no such algorithm exception
	 * @throws NoSuchPaddingException
	 *             the no such padding exception
	 * @throws InvalidKeyException
	 *             the invalid key exception
	 * @throws IllegalBlockSizeException
	 *             the illegal block size exception
	 * @throws BadPaddingException
	 *             the bad padding exception
	 * @throws InvalidKeySpecException
	 *             the invalid key spec exception
	 */
	public static String encryptWithStaticSalt(final String message,
			final String passphrase) throws NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException,
			InvalidKeySpecException
	{
		SecretKeySpec skeySpec = getSecretKeySpec(passphrase, staticSalt);
		return encrypt(message, skeySpec);
	}

	/**
	 * Encrypt a message given the secret key spec.
	 * 
	 * @param message
	 *            the non-null message
	 * @param secretKeySpec
	 *            the non-null SecretKeySpec
	 * 
	 * @return the encrypted string, Base64 encoded.
	 * 
	 * @throws NoSuchPaddingException
	 *             the no such padding exception
	 * @throws NoSuchAlgorithmException
	 *             the no such algorithm exception
	 * @throws InvalidKeyException
	 *             the invalid key exception
	 * @throws BadPaddingException
	 *             the bad padding exception
	 * @throws IllegalBlockSizeException
	 *             the illegal block size exception
	 */
	public static String encrypt(final String message,
			final SecretKeySpec secretKeySpec) throws NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException
	{
		Cipher cipher = Cipher.getInstance(secretKeySpec.getAlgorithm());
		cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
		byte[] encrypted = cipher.doFinal(message.getBytes());
		return Base64.encode(encrypted);
	}

	/**
	 * Decrypts a Base-64 encoded encrypted message using the static (JACOB)
	 * salt.
	 * 
	 * @param message
	 *            the non-null message
	 * @param passphrase
	 *            the non-null passphrase
	 * 
	 * @return the decrypted string
	 * 
	 * @throws NoSuchPaddingException
	 *             the no such padding exception
	 * @throws NoSuchAlgorithmException
	 *             the no such algorithm exception
	 * @throws InvalidKeyException
	 *             the invalid key exception
	 * @throws BadPaddingException
	 *             the bad padding exception
	 * @throws IllegalBlockSizeException
	 *             the illegal block size exception
	 * @throws WSSecurityException
	 *             the WS security exception
	 * @throws InvalidKeySpecException
	 *             the invalid key spec exception
	 */
	public static String decryptWithStaticSalt(final String message,
			final String passphrase) throws NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeyException, WSSecurityException,
			IllegalBlockSizeException, BadPaddingException,
			InvalidKeySpecException
	{
		SecretKeySpec skeySpec = getSecretKeySpec(passphrase, staticSalt);
		return decrypt(message, skeySpec);
	}

	/**
	 * Decrypts a Base-64 encoded encrypted message given the same secret key
	 * spec used to encrypt the message.
	 * 
	 * @param message
	 *            the non-null message
	 * @param secretKeySpec
	 *            the non-null SecretKeySpec
	 * 
	 * @return the decrypted string
	 * 
	 * @throws NoSuchPaddingException
	 *             the no such padding exception
	 * @throws NoSuchAlgorithmException
	 *             the no such algorithm exception
	 * @throws InvalidKeyException
	 *             the invalid key exception
	 * @throws BadPaddingException
	 *             the bad padding exception
	 * @throws IllegalBlockSizeException
	 *             the illegal block size exception
	 * @throws WSSecurityException
	 *             the WS security exception
	 */
	public static String decrypt(final String message,
			final SecretKeySpec secretKeySpec) throws NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeyException, WSSecurityException,
			IllegalBlockSizeException, BadPaddingException
	{
		Cipher cipher = Cipher.getInstance(secretKeySpec.getAlgorithm());
		cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
		// If you're getting an exception here,make sure the jce_policy files
		// are in the right place: jre/lib/security
		byte[] decrypted = cipher.doFinal(Base64.decode(message));
		return new String(decrypted);
	}

	/**
	 * Creates a random 20-byte salt.
	 * 
	 * @return the salt byte[]
	 */
	public static byte[] createRandomSalt()
	{
		// Never use Random() to make your salt. Use SecureRandom().
		Random r = new SecureRandom();
		byte[] salt = new byte[SALT_SIZE_BYTES];
		r.nextBytes(salt);
		return salt;
	}

	/**
	 * Main method, shows how to use the AdvancedEncryptionStandard class.
	 * 
	 * @param args
	 *            ignored
	 * 
	 * @throws Exception
	 *             any exception
	 */
	public static void main(String[] args) throws Exception
	{
		String message = "This is just an example";
		System.out.println("Original  : " + message);
		System.out.println();

		byte[] salt = createRandomSalt();
		System.out.println("Using a specified salt of length : " + salt.length);
		String saltStr = Base64.encode(salt);
		System.out.println("Salt encoded, suitable for XML or DB storage : "
				+ saltStr);
		byte[] saltFromStr = Base64.decode(saltStr);

		SecretKeySpec skeySpec = getSecretKeySpec("mypassword", saltFromStr);

		String encrypted = encrypt(message, skeySpec);
		System.out.println("Encrypted : " + encrypted);

		skeySpec = getSecretKeySpec("mypassword", salt);

		String decrypted = decrypt(encrypted, skeySpec);
		System.out.println("Decrypted : " + decrypted);

		if (message.equals(decrypted))
			System.out.println("THE ONE AND THE SAME");
		System.out.println();

		System.out.println("Using the system salt of length : "
				+ staticSalt.length);
		encrypted = encryptWithStaticSalt(message, "mypassword");
		System.out.println("Encrypted : " + encrypted);
		decrypted = decryptWithStaticSalt(encrypted, "mypassword");
		System.out.println("Decrypted : " + decrypted);
		if (message.equals(decrypted))
			System.out.println("THE ONE AND THE SAME");
		System.out.println();
	}
}
