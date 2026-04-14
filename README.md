# KidTale — AI 맞춤 동화책 제작 서비스

> 아이의 이름과 테마를 입력하면 AI가 세상에 하나뿐인 동화를 만들고, 실제 책으로 인쇄해 배송해드립니다.

---

## 1. 서비스 소개

**KidTale**은 Gemini AI를 활용해 아이 맞춤형 동화를 자동 생성하고, SweetBook 인쇄 API와 연동해 실물 하드커버 동화책을 제작·배송하는 풀스택 웹 서비스입니다.

- **타겟 고객**: 자녀에게 특별한 선물을 주고 싶은 부모
- **핵심 가치**: 세상에 단 하나뿐인, 우리 아이 이름이 들어간 맞춤 동화책

### 주요 기능

| 기능 | 설명 |
|------|------|
| AI 동화 생성 | 아이 이름·나이·테마 입력 → Gemini AI가 24페이지 동화 자동 작성 |
| AI 삽화 생성 | 각 페이지 내용에 맞는 지브리 스타일 삽화 실시간 생성 (Pollinations.ai) |
| 동화 미리보기 | 책 뷰어 형태의 페이지 넘김 + 썸네일 네비게이션 |
| 책 제작 주문 | SweetBook API 연동으로 하드커버 실물 책 주문 |
| 회원 시스템 | 회원가입 / 로그인 / 내 동화 목록 / 주문 내역 관리 |
| 샘플 미리보기 | 비로그인 사용자도 생성된 동화 예시 열람 가능 |

---

## 2. 실행 방법

### 필수 환경

- Java 17+
- Node.js 18+
- (선택) IntelliJ IDEA

### 환경변수 설정

프로젝트 루트에 `.env` 파일(또는 IntelliJ Run Configuration 환경변수)을 생성합니다.

```bash
# .env 예시
GEMINI_API_KEY=your_gemini_api_key
SWEETBOOK_API_KEY=your_sweetbook_api_key
SWEETBOOK_API_BASE_URL=https://api-sandbox.sweetbook.com/v1
SWEETBOOK_BOOK_SPEC_UID=SQUAREBOOK_HC
JWT_SECRET=your_jwt_secret_32chars_minimum
JWT_EXPIRY_DAYS=7
```

### 백엔드 실행

```bash
# 프로젝트 루트에서
./gradlew bootRun
# 서버 기동 확인: http://localhost:8080
```

### 프론트엔드 실행

```bash
cd frontend
npm install
cp .env.example .env   # VITE_API_BASE_URL=http://localhost:8080
npm run dev
# 브라우저: http://localhost:5173
```

> **복사-붙여넣기 실행 순서**
> 1. 환경변수 설정
> 2. `./gradlew bootRun` (백엔드 먼저)
> 3. `cd frontend && npm install && npm run dev`

---

## 3. 사용한 API 목록

### 3-1. KidTale 백엔드 API (자체 구현)

#### 인증 `/api/auth`

| API | Method | 인증 필요 | 용도 |
|-----|--------|-----------|------|
| `/api/auth/signup` | POST | ✗ | 회원가입 |
| `/api/auth/login` | POST | ✗ | 로그인 (JWT 토큰 발급) |
| `/api/auth/me` | GET | ✓ | 내 정보 조회 |

#### 동화 `/api/stories`

| API | Method | 인증 필요 | 용도 |
|-----|--------|-----------|------|
| `/api/stories` | GET | ✗ | 전체 동화 목록 조회 (홈 샘플용) |
| `/api/stories` | POST | ✗ | AI 동화 생성 (Gemini API 호출) |
| `/api/stories/{id}` | GET | ✗ | 특정 동화 단건 조회 |
| `/api/stories/my` | GET | ✓ | 내 동화 목록 조회 (로그인 사용자 전용) |

#### 책 제작 `/api/books`

| API | Method | 인증 필요 | 용도 |
|-----|--------|-----------|------|
| `/api/books` | POST | ✗ | 동화 → SweetBook 포토북 변환 생성 |
| `/api/books/templates` | GET | ✗ | 사용 가능한 표지·내지 템플릿 목록 조회 |

#### 주문 `/api/orders`

| API | Method | 인증 필요 | 용도 |
|-----|--------|-----------|------|
| `/api/orders` | POST | ✓ | 인쇄 주문 생성 (배송 정보 포함) |
| `/api/orders/{id}` | GET | ✓ | 주문 단건 조회 |
| `/api/orders?storyId={id}` | GET | ✓ | 특정 동화의 주문 조회 |

#### 이미지 프록시 `/api/images`

| API | Method | 인증 필요 | 용도 |
|-----|--------|-----------|------|
| `/api/images/generate` | GET | ✗ | Pollinations.ai 이미지 생성 프록시<br>`?prompt=&seed=&width=&height=` |

---

### 3-2. SweetBook Book Print API (외부)

| API | Method | 용도 |
|-----|--------|------|
| `/v1/books` | POST | 새 포토북(동화책) 생성 |
| `/v1/books/{uid}/cover` | POST | 표지 이미지 등록 |
| `/v1/books/{uid}/pages` | POST | 내지 페이지 콘텐츠 등록 |
| `/v1/orders` | POST | 실물 책 인쇄 주문 생성 |
| `/v1/templates` | GET | 사용 가능한 책 템플릿 목록 조회 |

### 3-3. Gemini AI API (Google, 외부)

| API | Method | 용도 |
|-----|--------|------|
| `/v1beta/models/{model}:generateContent` | POST | 동화 텍스트 생성<br>기본: gemini-2.5-flash / 폴백: gemini-2.0-flash |

### 3-4. Pollinations.ai Image API (외부)

| API | Method | 용도 |
|-----|--------|------|
| `/prompt/{encoded_prompt}` | GET | AI 삽화 이미지 생성 |

> 프론트엔드 → `/api/images/generate` (백엔드 프록시) → Pollinations.ai 순서로 중계하여 브라우저 CORS 제약 및 타임아웃 문제를 해결했습니다.

---

## 4. AI 도구 사용 내역

| AI 도구 | 활용 내용 |
|---------|----------|
| **Claude Code** | 전체 백엔드 API 라우팅 구조 설계, Spring Security + JWT 인증 구현, CORS 문제 디버깅, React 컴포넌트 설계 및 구현, 이미지 생성 프록시 설계 |
| **Gemini AI (gemini-2.5-flash)** | 서비스 내 동화 텍스트 실제 생성 (런타임 활용) |
| **Pollinations.ai** | 서비스 내 삽화 이미지 실제 생성 (런타임 활용) |

---

## 5. 설계 의도

### 왜 이 서비스를 선택했나

아이에게 줄 수 있는 가장 특별한 선물은 **"나만을 위한 이야기"** 라고 생각했습니다. 기존 동화책은 대량 출판물이지만, KidTale은 아이의 이름·나이·관심 테마를 반영한 세상에 하나뿐인 책을 만들어냅니다. AI 생성 → 미리보기 → 실물 인쇄 주문까지 이어지는 완전한 사용자 여정을 3단계로 단순화했습니다.

### 주요 기술 결정

| 결정 | 이유 |
|------|------|
| Spring Boot + React 분리 아키텍처 | 백엔드 API 서버와 프론트엔드를 독립적으로 개발·배포 가능 |
| JWT 기반 Stateless 인증 | 서버 세션 없이 확장 가능한 인증 구조, 모바일 확장 고려 |
| 백엔드 이미지 프록시 | 브라우저 CORS 제약 및 타임아웃 문제를 서버에서 통합 처리 |
| 지브리 스타일 프롬프트 | 아이에게 친근한 화풍, 연령대별 캐릭터 얼굴 묘사로 몰입감 향상 |
| H2 인메모리 DB | 빠른 개발·데모 환경 (프로덕션은 MySQL/PostgreSQL로 교체 가능) |
| userId 기반 데이터 격리 | 로그인한 사용자는 자신의 동화만 조회, 더미 데이터와 분리 |

### 더 시간이 있었다면 추가했을 기능

- 동화 텍스트 직접 편집 기능
- 주문 상태 실시간 알림 (이메일 / 카카오 알림톡)
- 관리자 대시보드 (주문 현황, 동화 통계)
- 이미지 캐싱 레이어 (S3 + CDN)로 반복 생성 비용 절감
- MySQL 전환 및 Docker Compose 배포 환경 구성

### 비즈니스 가능성

- **수익 모델**: 책 1권당 인쇄비 + 서비스 이용료 (SaaS)
- **확장성**: 생일 선물, 어린이집·유치원 단체 주문, 해외 배송
- **진입 장벽**: AI 프롬프트 튜닝과 UX 플로우가 핵심 경쟁력 — 후발 주자가 단순 복제 어려움

---

## 기술 스택

### 백엔드
- Java 17, Spring Boot 3, Spring Security, Spring Data JPA
- H2 (인메모리 DB), WebClient (WebFlux)
- JWT (jjwt 0.12.x), Lombok

### 프론트엔드
- React 18, Vite, Tailwind CSS
- React Router v6, Axios

### 외부 API
- Google Gemini API (동화 생성)
- Pollinations.ai (삽화 생성)
- SweetBook API (인쇄 주문)

---

**제작: 김혜윤** | kimhyeyun98@gmail.com  
© 2026 KidTale Corp. Inc. All rights reserved
