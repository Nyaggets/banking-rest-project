package com.banking.Banking.Service;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.CleartextKeysetHandle;
import com.google.crypto.tink.JsonKeysetReader;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.aead.AeadFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Сервис для кодирования, декодирования и хэширования данных
 */
@Service
public class EncodeService {
    private final String keySet;
    private final Aead aead;

    public EncodeService(@Value("${TINK_KEYSET_BASE64}") String keySet) {
        this.keySet = keySet;
        try {
            AeadConfig.register();
            byte[] keysetJsonBytes = Base64.getDecoder().decode(keySet);
            KeysetHandle keysetHandle = CleartextKeysetHandle.read(JsonKeysetReader.withBytes(keysetJsonBytes));
            aead = AeadFactory.getPrimitive(keysetHandle);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String encodeString(String line, Long clientId) {
        try {
            byte[] cipherText = aead.encrypt(line.getBytes(), clientId.toString().getBytes());
            return Base64.getEncoder().encodeToString(cipherText);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Ошибка шифрования данных");
        }
    }

    public String encodeString(String line) {
        try {
            byte[] cipherText = aead.encrypt(line.getBytes(), "".getBytes());
            return Base64.getEncoder().encodeToString(cipherText);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Ошибка шифрования данных");
        }
    }

    public String decodeString(String lineBase64, Long clientId) {
        try {
            byte[] cipherText = Base64.getDecoder().decode(lineBase64);
            byte[] decodedLine = aead.decrypt(cipherText, clientId.toString().getBytes());
            return new String(decodedLine);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Ошибка дешифрования данных");
        }
    }

    public String decodeString(String lineBase64) {
        try {
            byte[] cipherText = Base64.getDecoder().decode(lineBase64);
            byte[] decodedLine = aead.decrypt(cipherText, "".getBytes());
            return new String(decodedLine);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Ошибка дешифрования данных");
        }
    }

    public String generateSha256Hash(String line) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(line.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка генерации хеша", e);
        }
    }
}
