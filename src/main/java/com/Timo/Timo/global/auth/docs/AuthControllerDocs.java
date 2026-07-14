package com.Timo.Timo.global.auth.docs;

import com.Timo.Timo.global.auth.dto.request.AuthTokenRequest;
import com.Timo.Timo.global.auth.dto.response.AuthReissueResponse;
import com.Timo.Timo.global.auth.dto.response.AuthTokenResponse;
import com.Timo.Timo.global.auth.principal.CustomUserDetails;
import com.Timo.Timo.global.exception.dto.ErrorDto;
import com.Timo.Timo.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;

public interface AuthControllerDocs {

  @Operation(
      summary = "AccessToken 발급",
      description = """
      1회성 인증 코드(code)를 AccessToken으로 교환합니다.
     	"""
  )
  @SecurityRequirements
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      content = @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = AuthTokenRequest.class),
          examples = @ExampleObject(
              name = "로그인 요청",
              value =
                  """
                  {
                    "code": "550e8400-e29b-41d4-a716-446655440000"
                  }
                  """
          )
      )
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "로그인 성공",
          useReturnTypeSchema = true
      ),
      @ApiResponse(
          responseCode = "400",
          description = "요청 바디에 code가 누락된 경우",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ErrorDto.class)
          )
      ),
      @ApiResponse(
          responseCode = "401",
          description = "유효하지 않거나 만료된 인증 코드인 경우",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ErrorDto.class)
          )
      ),
      @ApiResponse(
          responseCode = "404",
          description = "인증 코드에 해당하는 사용자를 찾을 수 없는 경우",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ErrorDto.class)
          )
      ),
      @ApiResponse(
          responseCode = "500",
          description = "서버 내부 오류",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ErrorDto.class)
          )
      )
  })
  ResponseEntity<BaseResponse<AuthTokenResponse>> token(
      @RequestBody AuthTokenRequest body
  );

  @Operation(
      summary = "AccessToken 재발급",
      description = """
			쿠키로 전달된 RefreshToken과 sessionId를 검증하여 AccessToken을 재발급합니다.
			재발급 성공 시 RefreshToken과 sessionId 쿠키를 갱신합니다.
			"""

  )
  @SecurityRequirements
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "재발급 성공",
          useReturnTypeSchema = true
      ),
      @ApiResponse(
          responseCode = "401",
          description = "RefreshToken이 없거나 유효하지 않거나 만료된 경우",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ErrorDto.class)
          )
      ),
      @ApiResponse(
          responseCode = "404",
          description = "RefreshToken에 해당하는 사용자를 찾을 수 없는 경우 (탈퇴 등으로 삭제된 사용자)",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ErrorDto.class)
          )
      ),
      @ApiResponse(
          responseCode = "500",
          description = "서버 내부 오류",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ErrorDto.class)
          )
      )
  })
  ResponseEntity<BaseResponse<AuthReissueResponse>> reissue(
      @CookieValue(name = "refreshToken", required = false) String refreshToken,
      @CookieValue(name = "sessionId", required = false) String sessionId
  );

  @Operation(
      summary = "로그아웃",
      description = """
			현재 세션을 로그아웃하고 RefreshToken 및 sessionId 쿠키를 만료시킵니다.
			"""
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "로그아웃 성공",
          useReturnTypeSchema = true
      ),
      @ApiResponse(
          responseCode = "401",
          description = "Access Token이 없거나 만료되었거나 유효하지 않은 경우",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ErrorDto.class)
          )
      ),
      @ApiResponse(
          responseCode = "500",
          description = "서버 내부 오류",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ErrorDto.class)
          )
      )
  })
  ResponseEntity<BaseResponse<Void>> logout(
      @Parameter(hidden = true) CustomUserDetails userDetails,
      @CookieValue(name = "sessionId", required = false) String sessionId,
      @Parameter(hidden = true) HttpServletRequest request
  );

  @Operation(
      summary = "회원 탈퇴",
      description = """
			회원 탈퇴를 진행하며, 사용자와 관련된 모든 데이터를 영구 삭제합니다.<br>
			이 작업은 되돌릴 수 없습니다.
			"""
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "회원 탈퇴 성공",
          useReturnTypeSchema = true
      ),
      @ApiResponse(
          responseCode = "401",
          description = "Access Token이 없거나 만료되었거나 유효하지 않은 경우",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ErrorDto.class)
          )
      ),
      @ApiResponse(
          responseCode = "404",
          description = "Access Token의 사용자 ID에 해당하는 사용자를 찾을 수 없는 경우",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ErrorDto.class)
          )
      ),
      @ApiResponse(
          responseCode = "500",
          description = "서버 내부 오류",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ErrorDto.class)
          )
      )
  })

  ResponseEntity<BaseResponse<Void>> withdraw(
      @Parameter(hidden = true) CustomUserDetails userDetails,
      @CookieValue(name = "sessionId", required = false) String sessionId,
      @Parameter(hidden = true) HttpServletRequest request
  );
}
