package com.Timo.Timo.global.crypto.exception;

import com.Timo.Timo.global.exception.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CryptoErrorCode implements BaseErrorCode {

  CRYPTO_ENCRYPTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CRYPTO_500", "토큰 암호화에 실패했습니다."),
  CRYPTO_DECRYPTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CRYPTO_500", "토큰 복호화에 실패했습니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;
}
