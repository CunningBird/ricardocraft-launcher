package ru.ricardocraft.backend.base.helper;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.security.*;
import java.security.interfaces.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public final class SecurityHelper {

    public static final String EC_ALGO = "EC";

    // EC Algorithm constants
    public static final String EC_CURVE = "secp256r1";
    public static final String EC_SIGN_ALGO = "SHA256withECDSA";
    public static final int TOKEN_LENGTH = 16;

    // RSA Algorithm constants

    public static final String RSA_ALGO = "RSA";
    public static final String RSA_SIGN_ALGO = "SHA256withRSA";
    public static final String RSA_CIPHER_ALGO = "RSA/ECB/PKCS1Padding";

    // Algorithm size constants
    public static final int AES_KEY_LENGTH = 16;
    public static final int RSA_KEY_LENGTH_BITS = 2048;
    public static final int CRYPTO_MAX_LENGTH = 2048;
    public static final String HEX = "0123456789abcdef";

    private SecurityHelper() {
    }

    public static byte[] digest(DigestAlgorithm algo, byte[] bytes) {
        return newDigest(algo).digest(bytes);
    }


    public static byte[] digest(DigestAlgorithm algo, InputStream input) throws IOException {
        byte[] buffer = IOHelper.newBuffer();
        MessageDigest digest = newDigest(algo);
        for (int length = input.read(buffer); length != -1; length = input.read(buffer))
            digest.update(buffer, 0, length);
        return digest.digest();
    }


    public static byte[] digest(DigestAlgorithm algo, Path file) throws IOException {
        try (InputStream input = IOHelper.newInput(file)) {
            return digest(algo, input);
        }
    }


    public static byte[] digest(DigestAlgorithm algo, String s) {
        return digest(algo, IOHelper.encode(s));
    }


    public static byte[] digest(DigestAlgorithm algo, URL url) throws IOException {
        try (InputStream input = IOHelper.newInput(url)) {
            return digest(algo, input);
        }
    }

    public static KeyPair genECDSAKeyPair(SecureRandom random) {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(EC_ALGO);
            generator.initialize(new ECGenParameterSpec(EC_CURVE), random);
            return generator.genKeyPair();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            throw new InternalError(e);
        }
    }

    public static KeyPair genRSAKeyPair(SecureRandom random) {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(RSA_ALGO);
            generator.initialize(RSA_KEY_LENGTH_BITS, random);
            return generator.genKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new InternalError(e);
        }
    }

    public static Cipher newCipher(String algo) {
        try {
            return Cipher.getInstance(algo);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new InternalError(e);
        }
    }

    public static MessageDigest newDigest(DigestAlgorithm algo) {
        VerifyHelper.verify(algo, a -> a != DigestAlgorithm.PLAIN, "PLAIN digest");
        try {
            return MessageDigest.getInstance(algo.name);
        } catch (NoSuchAlgorithmException e) {
            throw new InternalError(e);
        }
    }

    public static SecureRandom newRandom() {
        return new SecureRandom();
    }

    private static Cipher newRSACipher(int mode, RSAKey key) {
        Cipher cipher = newCipher(RSA_CIPHER_ALGO);
        try {
            cipher.init(mode, (Key) key);
        } catch (InvalidKeyException e) {
            throw new InternalError(e);
        }
        return cipher;
    }

    private static KeyFactory newECDSAKeyFactory() {
        try {
            return KeyFactory.getInstance(EC_ALGO);
        } catch (NoSuchAlgorithmException e) {
            throw new InternalError(e);
        }
    }

    private static KeyFactory newRSAKeyFactory() {
        try {
            return KeyFactory.getInstance(RSA_ALGO);
        } catch (NoSuchAlgorithmException e) {
            throw new InternalError(e);
        }
    }

    private static Signature newECSignature() {
        try {
            return Signature.getInstance(EC_SIGN_ALGO);
        } catch (NoSuchAlgorithmException e) {
            throw new InternalError(e);
        }
    }

    private static Signature newRSASignature() {
        try {
            return Signature.getInstance(RSA_SIGN_ALGO);
        } catch (NoSuchAlgorithmException e) {
            throw new InternalError(e);
        }
    }

    public static Signature newECSignSignature(ECPrivateKey key) {
        Signature signature = newECSignature();
        try {
            signature.initSign(key);
        } catch (InvalidKeyException e) {
            throw new InternalError(e);
        }
        return signature;
    }

    public static Signature newRSASignSignature(RSAPrivateKey key) {
        Signature signature = newRSASignature();
        try {
            signature.initSign(key);
        } catch (InvalidKeyException e) {
            throw new InternalError(e);
        }
        return signature;
    }


    public static Signature newECVerifySignature(ECPublicKey key) {
        Signature signature = newECSignature();
        try {
            signature.initVerify(key);
        } catch (InvalidKeyException e) {
            throw new InternalError(e);
        }
        return signature;
    }

    public static byte[] randomBytes(int length) {
        return randomBytes(newRandom(), length);
    }

    public static byte[] randomBytes(Random random, int length) {
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return bytes;
    }


    public static String randomStringToken() {
        return randomStringToken(newRandom());
    }

    public static String randomStringToken(Random random) {
        return toHex(randomToken(random));
    }

    public static byte[] randomToken(Random random) {
        return randomBytes(random, TOKEN_LENGTH);
    }

    public static String randomStringAESKey() {
        return toHex(randomAESKey(newRandom()));
    }

    public static byte[] randomAESKey(Random random) {
        return randomBytes(random, AES_KEY_LENGTH);
    }

    public static byte[] sign(byte[] bytes, ECPrivateKey privateKey) {
        Signature signature = newECSignSignature(privateKey);
        try {
            signature.update(bytes);
            return signature.sign();
        } catch (SignatureException e) {
            throw new InternalError(e);
        }
    }

    public static byte[] sign(byte[] bytes, RSAPrivateKey privateKey) {
        Signature signature = newRSASignSignature(privateKey);
        try {
            signature.update(bytes);
            return signature.sign();
        } catch (SignatureException e) {
            throw new InternalError(e);
        }
    }


    public static String toHex(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        int offset = 0;
        char[] hex = new char[bytes.length << 1];
        for (byte currentByte : bytes) {
            int ub = Byte.toUnsignedInt(currentByte);
            hex[offset] = HEX.charAt(ub >>> 4);
            offset++;
            hex[offset] = HEX.charAt(ub & 0x0F);
            offset++;
        }
        return new String(hex);
    }

    public static ECPublicKey toPublicECDSAKey(byte[] bytes) throws InvalidKeySpecException {
        return (ECPublicKey) newECDSAKeyFactory().generatePublic(new X509EncodedKeySpec(bytes));
    }

    public static ECPrivateKey toPrivateECDSAKey(byte[] bytes) throws InvalidKeySpecException {
        return (ECPrivateKey) newECDSAKeyFactory().generatePrivate(new PKCS8EncodedKeySpec(bytes));
    }

    public static RSAPublicKey toPublicRSAKey(byte[] bytes) throws InvalidKeySpecException {
        return (RSAPublicKey) newRSAKeyFactory().generatePublic(new X509EncodedKeySpec(bytes));
    }

    public static RSAPrivateKey toPrivateRSAKey(byte[] bytes) throws InvalidKeySpecException {
        return (RSAPrivateKey) newRSAKeyFactory().generatePrivate(new PKCS8EncodedKeySpec(bytes));
    }

    public static Cipher newRSADecryptCipher(RSAPrivateKey privateKey) {
        try {
            return newRSACipher(Cipher.DECRYPT_MODE, privateKey);
        } catch (SecurityException e) {
            throw new InternalError(e);
        }
    }

    public static byte[] getAESKey(byte[] seed) throws Exception {
        KeyGenerator kGen = KeyGenerator.getInstance("AES");
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        sr.setSeed(seed);
        kGen.init(128, sr); // 192 and 256 bits may not be available
        SecretKey sKey = kGen.generateKey();
        return sKey.getEncoded();
    }

    public static byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception {
        SecretKeySpec sKeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, sKeySpec);
        return cipher.doFinal(encrypted);
    }

    public static byte[] decrypt(String seed, byte[] encrypted) throws Exception {
        return decrypt(getAESKey(IOHelper.encode(seed)), encrypted);
    }

    public static byte[] fromHex(String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++) {
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16).byteValue();
        }
        return result;
    }

    public enum DigestAlgorithm {
        PLAIN("plain", -1), MD5("MD5", 128), SHA1("SHA-1", 160), SHA224("SHA-224", 224), SHA256("SHA-256", 256), SHA512("SHA-512", 512);
        private static final Map<String, DigestAlgorithm> ALGORITHMS;

        static {
            DigestAlgorithm[] algorithmsValues = values();
            ALGORITHMS = new HashMap<>(algorithmsValues.length);
            for (DigestAlgorithm algorithm : algorithmsValues)
                ALGORITHMS.put(algorithm.name, algorithm);
        }

        // Instance
        public final String name;
        public final int bits;
        public final int bytes;

        DigestAlgorithm(String name, int bits) {
            this.name = name;
            this.bits = bits;

            // Convert to bytes
            bytes = bits / Byte.SIZE;
            assert bits % Byte.SIZE == 0;
        }

        @Override
        public String toString() {
            return name;
        }

        public byte[] verify(byte[] digest) {
            if (digest.length != bytes)
                throw new IllegalArgumentException("Invalid digest length: " + digest.length);
            return digest;
        }
    }
}
