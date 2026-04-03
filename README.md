# Message Backend Server

메시지 생성 프로젝트의 백엔드 서버 저장소입니다. 프론트엔드 요청을 받아 캠페인을 생성하고, 고객 세그먼트와 메시지 결과를 저장 및 조회하며, AI 서버와 연결해 전체 캠페인 처리 흐름을 orchestration 합니다.

## 프로젝트 개요

- 이 저장소는 3개 레포로 분리된 전체 시스템 중 백엔드 서버를 담당합니다.
- Azure Container Apps에 배포되어 프론트엔드와 AI 서버 사이의 중심 API 역할을 합니다.
- 프론트 서비스 주소: https://message-fe-app.redriver-ce1c37ed.japaneast.azurecontainerapps.io/
- 별도 연동 플랫폼 주소: https://marketing-platform-app.redriver-ce1c37ed.japaneast.azurecontainerapps.io/

## 주요 기능

- 프로모션 PDF 업로드 후 캠페인 생성
- 고객 CSV 업로드 및 세그먼트 저장
- AI 서버 호출을 통한 세그먼트 생성과 메시지 초안 생성
- 세그먼트 결과 조회 및 CSV 다운로드
- 메시지 결과 조회, 수정, CSV 다운로드
- 캠페인 목록 관리

## 처리 흐름

1. 프론트엔드가 프로모션 PDF를 업로드합니다.
2. 백엔드가 AI 서버에 PDF 분석을 요청해 캠페인 정보를 생성합니다.
3. 프론트엔드가 고객 CSV를 업로드합니다.
4. 백엔드가 고객 데이터를 정리해 AI 서버에 세그먼트 생성을 요청합니다.
5. 세그먼트 저장 후 메시지 생성 요청을 보내고 결과를 저장합니다.
6. 프론트엔드에서 세그먼트 결과와 메시지 결과를 조회하거나 CSV로 내려받습니다.

## 주요 API

### `POST /api/campaigns`

- 프로모션 PDF를 받아 캠페인을 생성합니다.

### `POST /api/campaigns/{campaign_id}/segments`

- 고객 CSV를 업로드하고 세그먼트를 생성합니다.

### `POST /api/campaigns/{campaign_id}/messages`

- 세그먼트별 메시지 초안을 생성합니다.

### `GET /api/campaigns`

- 전체 캠페인 목록을 조회합니다.

### `GET /api/campaigns/{campaign_id}/segments`

- 세그먼트 결과를 조회합니다.

### `GET /api/campaigns/{campaign_id}/segments/csv`

- 세그먼트 결과를 CSV로 다운로드합니다.

### `GET /api/campaigns/{campaign_id}/messages`

- 생성된 메시지 결과를 조회합니다.

### `GET /api/campaigns/{campaign_id}/messages/map`

- 고객별 메시지 매핑 결과를 조회합니다.

### `GET /api/campaigns/{campaign_id}/messages/csv`

- 고객별 메시지 결과를 CSV로 다운로드합니다.

### `PATCH /api/campaigns/messages/{result_id}`

- 메시지 초안을 수정합니다.

## 기술 스택

- Java 17
- Spring Boot 3
- Spring Web
- Spring Data JPA
- H2 Database
- Gradle
- Docker
- Azure Container Apps

## 현재 설정 기준

- 기본 포트: `8080`
- 기본 데이터베이스: H2 in-memory
- AI 서버 연동 주소 설정 키: `ai.server.url`
- CORS는 전체 경로에 대해 허용되도록 설정되어 있습니다.

## 로컬 실행

### 1. 애플리케이션 실행

```bash
./gradlew bootRun
```

Windows에서는 다음 명령을 사용할 수 있습니다.

```bash
gradlew.bat bootRun
```

### 2. 빌드

```bash
./gradlew build
```

### 3. 주요 설정 파일

- `src/main/resources/application.properties`
- `build.gradle`
- `Dockerfile`

## AI 서버 연동

- 프로모션 PDF 분석: `POST /ai/campaign/extract`
- 고객 세그먼트 생성: `POST /cluster-customers`
- 메시지 초안 생성: `POST /generate-messages`

백엔드는 Azure Container Apps 환경에서 AI 서버와 내부 네트워크로 통신하도록 구성되어 있습니다.

## 배포

- 모든 서버는 Azure Container Apps에 배포되어 있습니다.
- 이 저장소는 운영 환경의 API 허브 역할을 하며, 프론트엔드와 AI 서버를 연결합니다.
- 현재 코드 기준으로 컨테이너는 빌드된 Spring Boot JAR를 실행하는 방식입니다.

## 관련 저장소

- Frontend: https://github.com/likemin35/Work_experience_program_FE
- AI Server: https://github.com/likemin35/Work_experience_program_AI
