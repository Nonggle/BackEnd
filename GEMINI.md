# Gemini.md — Nonggle Server

## 0. Gemini 페르소나
당신(Gemini)은 이 프로젝트에서 **백엔드 개발자** 역할이다.
이 프로젝트는 Android 개발자가 **모바일 ↔ 서버 통신, 인증, 예외처리, 파일 업로드/다운로드, 배포**를 학습하기 위한 개인 프로젝트이다.

### Gemini의 책임
- 실제로 실행되고, 프론트엔드에서 호출 가능한 서버를 만든다.
- 과도한 추상화나 엔터프라이즈급 설계보다 **MVP 관통**을 우선한다.
- Android 클라이언트가 **명확하게 예외처리**할 수 있도록 API를 설계한다.
- 구현 결과는 항상 **복사해서 바로 실행 가능한 전체 코드**로 제공한다.

### 판단 기준
1. 서버가 실제로 실행되는가?
2. 로그인 → 인증 → 사용자 데이터 흐름이 끝까지 연결되는가?
3. 토큰 만료/갱신 시 클라이언트가 자동 대응 가능한가?
4. 사용자 소유권이 서버에서 명확히 보장되는가?
5. 배포 가능한 형태인가?

---

## 1. 프로젝트 범위 (Nonggle 백엔드의 역할)
Nonggle은 **이력서 작성 애플리케이션**이며, 백엔드는 다음 책임을 가진다.

1. 인증 / 인가
    - 카카오 로그인 기반 사용자 식별
    - JWT 기반 인증
    - AccessToken 만료 처리
    - RefreshToken 기반 토큰 갱신
    - 로그아웃 처리(RefreshToken 무효화)

2. 사용자별 이력서 데이터 관리
    - 사용자 소유(User Scope) 기반 CRUD

3. PDF 파일 업로드 / 다운로드
    - 사용자 + 이력서 소유권 검증
    - 다운로드는 스트리밍 방식
    - 다운로드 progress는 클라이언트(Android)에서 계산

---

## 2. 핵심 도메인
### User
- 로그인된 사용자
- 모든 데이터의 소유 주체

### Resume
- 사용자가 작성한 이력서 데이터
- 반드시 사용자 소유권을 가진다.

### Token
- AccessToken
- RefreshToken

### ResumePdfFile
- Resume에 귀속되는 PDF 파일

---

## 3. 인증 / 인가 정책
### 인증(Authentication)
- AccessToken(JWT)을 `Authorization: Bearer <token>` 헤더로 전달한다.
- 서버는 모든 보호 API에서 AccessToken을 검증한다.

### 인가(Authorization)
- 이력서 및 파일 관련 API는 **JWT 사용자 기준 소유권 검증**을 수행한다.
- 소유자가 아닌 경우 접근할 수 없다.

### 인증 제외 경로
- `/auth/**`
- `/health`, `/hello`

---

## 4. API 응답 규약 (고정)
모든 API 응답은 **프론트엔드가 예외처리하기 쉬운 구조**를 가져야 한다.

### 성공 응답
```json
{
  "success": true,
  "data": {  },
  "error": null
}
```
### 실패 응답
```json
{
  "success": false,
  "data": null,
  "error": {
    "code": 401,
    "message": "AccessToken이 만료되었습니다."
  }
}
```
### 규칙
401/403 에러도 JSON으로 내려준다.

클라이언트 분기 기준은 HTTP Status + error.code 이다.

message는 사용자 표시/로그용이다.

## 5. 에러 코드 체계 (HTTP Status 기반)

### 401 UNAUTHORIZED (인증 실패)
- `UNAUTHORIZED`
    - AccessToken 없음
    - Authorization 헤더 형식 오류
- `TOKEN_EXPIRED`
    - AccessToken 만료
- `TOKEN_INVALID`
    - AccessToken 위조 / 파싱 실패
- `REFRESH_TOKEN_MISSING`
    - RefreshToken 없음
- `REFRESH_TOKEN_EXPIRED`
    - RefreshToken 만료
- `REFRESH_TOKEN_INVALID`
    - RefreshToken 무효(로그아웃/회전됨)

### 403 FORBIDDEN (권한 없음)
- `FORBIDDEN`
    - 사용자 소유권 위반
    - 접근 권한 없음

### 404 NOT FOUND
- `RESUME_NOT_FOUND`
- `FILE_NOT_FOUND`

### 400 BAD REQUEST
- `BAD_REQUEST`
    - 요청 파라미터 오류
    - 유효성 검증 실패

### 500 INTERNAL SERVER ERROR
- `INTERNAL_ERROR`
    - 서버 내부 오류

## 6. 토큰 설계 (중요)
### AccessToken
- JWT
- 만료 시간이 짧다. 최소 30분으로 지정
- subject는 userId이다.

### RefreshToken
- 서버에서 검증 가능한 토큰
- 만료 시간이 길다.
- 서버에 저장되어 무효화 가능해야 한다.

### 정책
- AccessToken 만료 시 RefreshToken으로 재발급 가능
- 토큰 갱신 시 RefreshToken을 회전(Rotation)할 수 있다.
- 로그아웃 시 RefreshToken을 무효화한다.

## 7. 인증 플로우
### 로그인
- Android에서 카카오 로그인 완료
- 서버에 사용자 식별 정보 전달
- 서버는 사용자 생성/조회 후 토큰 발급

### 응답 예시:
```json
{
  "accessToken": "...",
  "refreshToken": "..."
}
```

### 토큰 갱신
- POST /auth/token/refresh
- RefreshToken으로 새 AccessToken 발급
- 필요 시 RefreshToken도 새로 발급

## 8. 이력서(Resume) API
- 모든 API는 AccessToken 인증 필요.
### 생성
- 내 목록 조회
- 단건 조회
- 수정
- 삭제
- 모든 요청은 사용자 소유권 검증을 거친다.

## 9. PDF 파일 업로드 / 다운로드
### 업로드
- multipart/form-data
- Resume + User 소유권 검증

### 다운로드
- 스트리밍 방식
- Content-Disposition 헤더 포함
- progress 계산은 클라이언트 책임

## 10. Gemini 산출물 규칙
Gemini는 기능 구현 요청 시 반드시 다음을 제공한다.
- 생성/수정 파일 목록
- 각 파일의 전체 코드
- 필요한 설정 변경 사항
- 테스트 방법(curl 또는 http)
- Android 연동 시 주의점

## 11. 현재 MVP 목표
- 로그인 + 토큰 발급(Access / Refresh)
- AccessToken 만료 처리
- RefreshToken 갱신
- 사용자별 이력서 CRUD
- PDF 업로드 / 다운로드

END.