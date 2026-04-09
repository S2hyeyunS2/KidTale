# KidTale의 코드컨벤션 규칙

## 0. 최우선 원칙
이 문서는 본 프로젝트의 최상위 규칙이다.  
모든 코드 작성 및 AI 도구는 이 문서를 반드시 따른다.

---

## 1. 프로젝트 개요

AI 동화 생성 + 책 제작 서비스

사용자가 아이 정보를 입력하면 AI(Gemini)가 동화를 생성하고,  
SweetBook API를 통해 책 생성 및 주문까지 이어지는 웹 애플리케이션을 만든다.

---

## 2. 기술 스택

- Java 17
- Spring Boot 3.5.x
- Gradle
- React + Vite + TailwindCSS

---

## 3. 아키텍처

3계층 구조

Controller → Service → Repository

### Controller
- 요청/응답 처리
- DTO 변환
- 비즈니스 로직 금지

### Service
- 핵심 비즈니스 로직
- 트랜잭션 처리
- 외부 API 호출 담당

### Repository
- DB 접근 전담
- JPA 사용

---

## 4. 프로젝트 구조 (예시)

kidtale/
├── backend/
│   └── src/main/java/com/hyeyuns2/kidtale/
│       ├── common/
│       ├── story/
│       │   ├── controller/
│       │   ├── service/
│       │   ├── repository/
│       │   ├── entity/
│       │   └── dto/
│       ├── order/
│       └── external/
│           ├── sweetbook/
│           └── ai/

---

## 5. 핵심 기능 흐름

1. 사용자 입력 (이름, 나이, 테마)
2. Gemini API 호출 → 동화 생성
3. DB 저장
4. SweetBook Books API 호출
5. 미리보기 제공
6. SweetBook Orders API 호출 → 주문 생성

---

## 6. 외부 API 규칙

### SweetBook API (필수)
- POST /books
- POST /orders

### Gemini API
- 동화 생성 전용
- API Key는 환경변수 사용

---

## 7. 환경 변수

application.yml

```yaml
sweetbook:
  api:
    base-url: ${SWEETBOOK_API_BASE_URL}
    key: ${SWEETBOOK_API_KEY}

gemini:
  api:
    key: ${GEMINI_API_KEY}
```

.env.example

```
SWEETBOOK_API_BASE_URL=https://api.sweetbook.com
SWEETBOOK_API_KEY=
GEMINI_API_KEY=
VITE_API_BASE_URL=http://localhost:8080
```

---

## 8. 엔티티 규칙

- setter 지양
- 정적 생성 메서드 사용
- BaseEntity 사용 가능

---

## 9. 프론트 규칙

- 함수형 컴포넌트
- API 분리
- Tailwind 사용

---

## 10. 코딩 규칙

- 생성자 주입
- @Slf4j 사용
- null 대신 Optional
- 매직넘버 금지
- API Key 하드코딩 금지

---

## 11. 예외 처리

- GlobalExceptionHandler 사용
- 비즈니스 예외 분리
- 스택트레이스 노출 금지
