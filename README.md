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
| AI 동화 생성 | 아이 이름·나이·테마 입력 → Gemini AI가 12페이지 동화 자동 작성 |
| AI 삽화 생성 | 각 페이지 내용에 맞는 지브리 스타일 삽화 실시간 생성 (Pollinations.ai) |
| 동화 미리보기 | 책 뷰어 형태의 페이지 넘김 + 썸네일 네비게이션 |
| 텍스트 인라인 편집 | 생성된 동화 텍스트를 페이지별로 직접 수정·저장 |
| 책 제작 주문 | SweetBook API 연동으로 하드커버 실물 책 주문 |
| 회원 시스템 | 회원가입 / 로그인 / 내 동화 목록 / 주문 내역 관리 |
| 샘플 미리보기 | 비로그인 사용자도 생성된 동화 예시 열람 가능 |

---

## 📸 서비스 화면

### 홈 — 실제 생성된 동화 샘플 썸네일

> 테마별 AI 삽화가 자동 생성되어 카드 형태로 표시됩니다. 클릭 시 동화 미리보기로 이동합니다.

<p align="center">
  <img src="https://github.com/user-attachments/assets/302e6f7f-2720-4b61-9af1-75c2bb9c3bb0" width="90%" alt="홈 화면" />
</p>

---

### 동화 미리보기 — AI 삽화 + 텍스트

> 생성된 동화를 책 뷰어 형태로 한 페이지씩 감상할 수 있습니다. 왼쪽은 AI가 생성한 삽화, 오른쪽은 AI가 작성한 동화 텍스트입니다.

<p align="center">
  <img src="https://github.com/user-attachments/assets/fe1edab9-9301-499b-96ee-a06ffdcd20fc" width="48%" alt="1페이지 — 해변 장면" />
  <img src="https://github.com/user-attachments/assets/0ef377fa-293f-43e7-ae8b-15415a074bf8" width="48%" alt="2페이지 — 바닷속 장면" />
</p>

<p align="center">
  <sub>왼쪽: 1페이지 — 해변 장면 / 오른쪽: 2페이지 — 바닷속 장면</sub>
</p>

---

### 텍스트 인라인 편집

> 마음에 들지 않는 내용은 ✏️ 버튼을 눌러 페이지별로 직접 수정하고 저장할 수 있습니다.

<p align="center">
  <img src="https://github.com/user-attachments/assets/084d3d22-7ac3-40a4-815e-17794e774d56" width="48%" alt="보기 모드" />
  <img src="https://github.com/user-attachments/assets/c1520ee4-5827-422f-9e58-8303ecd26ce9" width="48%" alt="편집 모드" />
</p>

<p align="center">
  <sub>왼쪽: 보기 모드 / 오른쪽: 편집 모드</sub>
</p>

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
GEMINI_FALLBACK_MODEL=gemini-2.0-flash-lite
GEMINI_API_KEY=your_gemini_api_key
SWEETBOOK_API_KEY=your_sweetbook_api_key
JWT_SECRET=your_jwt_secret_32chars_minimum
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
| `/api/stories/{id}/pages/{pageNumber}` | PATCH | ✗ | 특정 페이지 텍스트 수정 |

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
| `/api/images/generate` | GET | ✗ | Pollinations.ai 이미지 생성 프록시<br>`?prompt=&seed=&width=&height=&negative_prompt=` |

#### 관리자 `/api/admin`

| API | Method | 인증 필요 | 용도 |
|-----|--------|-----------|------|
| `/api/admin/sweetbook/templates` | GET | ✗ | SweetBook 템플릿 목록 직접 조회 |
| `/api/admin/sweetbook/book-specs` | GET | ✗ | SweetBook 책 규격(판형) 목록 조회 |

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
| `/prompt/{encoded_prompt}` | GET | AI 삽화 이미지 생성 (`negative_prompt` 파라미터로 성인 이미지 차단) |

> 프론트엔드 → `/api/images/generate` (백엔드 프록시) → Pollinations.ai 순서로 중계하여 브라우저 CORS 제약 및 타임아웃 문제를 해결했습니다.

---

## 4. AI 도구 사용 내역

| AI 도구 | 활용 내용 |
|---------|----------|
| **Claude Code** | 전체 백엔드 API 라우팅 구조 설계, Spring Security + JWT 인증 구현, CORS 문제 디버깅, React 컴포넌트 설계 및 구현, 이미지 생성 프록시 설계, 텍스트 편집 기능 구현 |
| **Gemini AI (gemini-2.5-flash)** | 서비스 내 동화 텍스트 실제 생성 (런타임 활용) |
| **Pollinations.ai** | 서비스 내 삽화 이미지 실제 생성 (런타임 활용) |

---

## 5. 설계 의도

### 🎯 왜 이 서비스를 만들었는가

아이들은 자신이 좋아하는 주제나 캐릭터에 대해 강한 호기심을 보이고, 그 관심을 기반으로 상상력을 빠르게 확장해 나간다고 생각했습니다.

하지만 기존 동화책은 이미 정해진 이야기와 캐릭터를 기반으로 하기 때문에, 아이 개개인의 관심사나 이름, 상황이 반영되기 어렵다는 한계가 있습니다.

이러한 점에서, 아이의 이름과 나이, 그리고 좋아하는 주제를 입력하고 그 아이만을 위한 맞춤형 동화를 만들어주는 서비스가 있다면 더 높은 몰입감과 즐거움을 줄 수 있다고 판단했습니다.

또한 단순히 AI가 이야기를 생성하는 것에서 끝나는 것이 아니라, 그 이야기를 실제 "책"으로 제작할 수 있도록 연결함으로써 디지털 경험을 물리적인 추억으로 확장시키고자 했습니다.

이 서비스를 통해 아이에게는 상상력을 키울 수 있는 경험을, 부모에게는 아이와의 특별한 추억을 남길 수 있는 가치를 제공하고자 합니다.

### 🔧 주요 기술 결정

| 결정 | 이유 |
|------|------|
| Spring Boot + React 분리 아키텍처 | 백엔드 API 서버와 프론트엔드를 독립적으로 개발·배포 가능 |
| JWT 기반 Stateless 인증 | 서버 세션 없이 확장 가능한 인증 구조, 모바일 확장 고려 |
| 백엔드 이미지 프록시 | 브라우저 CORS 제약 및 타임아웃 문제를 서버에서 통합 처리 |
| 지브리 스타일 + 나이대별 프롬프트 | 아이에게 친근한 화풍, 연령대별 신체·얼굴 묘사로 몰입감 향상 |
| negative_prompt 적용 | 성인·청소년 이미지 생성 차단으로 서비스 안전성 확보 |
| pagesJson 구조 (JSON 직렬화) | 페이지 수가 유동적인 동화 특성상 정규화 없이 유연하게 저장·수정 가능 |
| H2 인메모리 DB | 빠른 개발·데모 환경 (프로덕션은 MySQL/PostgreSQL로 교체 가능) |
| userId 기반 데이터 격리 | 로그인한 사용자는 자신의 동화만 조회, 더미 데이터와 분리 |

### 📌 더 시간이 있었다면 추가했을 기능

- 주문 상태 실시간 알림 (이메일 / 카카오 알림톡)
- 관리자 대시보드 (주문 현황, 동화 통계)
- 이미지 캐싱 레이어 (S3 + CDN)로 반복 생성 비용 절감
- MySQL 전환 및 Docker Compose 배포 환경 구성

### 💰 비즈니스 가능성

본 서비스는 AI 기반 콘텐츠 생성과 실물 상품 제작(Book Print API)을 결합한 구조로, 디지털과 커머스를 동시에 연결할 수 있는 비즈니스 모델을 가지고 있습니다.

아이의 이름과 관심사를 반영한 맞춤형 동화책은 일반 동화책보다 높은 개인화 가치를 가지기 때문에, 부모 입장에서 프리미엄 상품으로 소비될 가능성이 높습니다.

단건 구매를 넘어 다음과 같은 반복 수익 모델로 확장할 수 있습니다.

- 매월 새로운 동화를 생성해주는 **구독형 서비스**
- 나이별·주제별 성장 기록을 모아주는 **시리즈형 책 제작**

또한 다음과 같은 B2B 및 선물 시장으로도 확장이 가능합니다.

- 어린이집·유치원 단체 졸업 앨범
- 교육기관 맞춤 콘텐츠 제작
- 생일·명절 선물용 맞춤 동화책

AI를 통해 콘텐츠 생성 비용은 낮추면서, 실물 책 제작 및 배송을 통해 수익을 창출할 수 있기 때문에 **확장성과 수익성을 동시에 확보할 수 있는 구조**라고 판단했습니다.

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
