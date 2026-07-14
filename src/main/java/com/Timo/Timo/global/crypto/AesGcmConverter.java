package com.Timo.Timo.global.crypto;

import com.Timo.Timo.global.crypto.exception.CryptoErrorCode;
import com.Timo.Timo.global.exception.CustomException;
import jakarta.persistence.AttributeConverter;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AesGcmConverter implements AttributeConverter<String, String> {

  private static final String ALGORITHM = "AES/GCM/NoPadding";
  private static final int GCM_TAG_LENGTH_BITS = 128;
  private static final int GCM_IV_LENGTH_BYTES = 12;

  private static SecretKeySpec secretKey;

  @Value("${security.crypto.calendar-token-key}")
  public void setSecretKey(String base64Key) {
    byte[] keyBytes = Base64.getDecoder().decode(base64Key);
    secretKey = new SecretKeySpec(keyBytes, "AES");
  }

  @Override
  public String convertToDatabaseColumn(String plainText) {
    if (plainText == null) {
      return null;
    }
    try {
      byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
      new SecureRandom().nextBytes(iv);

      Cipher cipher = Cipher.getInstance(ALGORITHM);
      cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));

      byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

      byte[] combined = new byte[iv.length + cipherText.length];
      System.arraycopy(iv, 0, combined, 0, iv.length);
      System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);

      return Base64.getEncoder().encodeToString(combined);
    } catch (Exception e) {
      throw new CustomException(CryptoErrorCode.CRYPTO_ENCRYPTION_FAILED);
    }
  }

  @Override
  public String convertToEntityAttribute(String encoded) {
    if (encoded == null) {
      return null;
    }
    try {
      byte[] combined = Base64.getDecoder().decode(encoded);

      byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
      byte[] cipherText = new byte[combined.length - GCM_IV_LENGTH_BYTES];
      System.arraycopy(combined, 0, iv, 0, iv.length);
      System.arraycopy(combined, iv.length, cipherText, 0, cipherText.length);

      Cipher cipher = Cipher.getInstance(ALGORITHM);
      cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));

      byte[] plainBytes = cipher.doFinal(cipherText);
      return new String(plainBytes, StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new CustomException(CryptoErrorCode.CRYPTO_DECRYPTION_FAILED);
    }
  }
}
