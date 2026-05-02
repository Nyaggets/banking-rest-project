package com.banking.Banking;

import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.CleartextKeysetHandle;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.aead.AeadKeyTemplates;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

    public class GenerateKeyForEnv {
        public static void main(String[] args) throws Exception {
            AeadConfig.register();
            KeysetHandle keysetHandle = KeysetHandle.generateNew(AeadKeyTemplates.AES256_EAX);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            CleartextKeysetHandle.write(keysetHandle, com.google.crypto.tink.JsonKeysetWriter.withOutputStream(outputStream));

            String base64Keyset = Base64.getEncoder().encodeToString(outputStream.toByteArray());
            System.out.println("TINK_KEYSET_BASE64=" + base64Keyset);
        }
    }

