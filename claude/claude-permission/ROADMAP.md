# Developer Web Resume — ROADMAP

## 프로젝트 개요

HTML, CSS, JavaScript, TailwindCSS를 사용하여 개발자 웹 이력서를 제작한다.

- **기술 스택:** HTML5, CSS3, JavaScript (Vanilla), TailwindCSS v3
- **목표:** 반응형 단일 페이지 웹 이력서

---

## 이력서 구성 섹션

| 섹션 | 내용 |
|------|------|
| Hero | 이름, 직함, 한 줄 소개, 연락처 링크 |
| About | 간략한 자기소개 |
| Skills | 기술 스택 목록 (Frontend / Backend / Tools) |
| Experience | 직장 경력 (회사명, 기간, 역할, 주요 업무) |
| Projects | 대표 프로젝트 (이름, 설명, 사용 기술, 링크) |
| Education | 학력 사항 |
| Contact | 이메일, GitHub, LinkedIn |

---

## 이력서 샘플 내용

### 기본 정보
- **이름:** 김개발
- **직함:** Frontend Developer
- **이메일:** dev.kim@example.com
- **GitHub:** github.com/devkim
- **LinkedIn:** linkedin.com/in/devkim
- **한 줄 소개:** 사용자 경험을 중시하는 3년차 프론트엔드 개발자

### About
웹 서비스 개발에 열정을 가진 프론트엔드 개발자입니다. 사용자 중심의 인터페이스 설계와 성능 최적화에 관심이 많으며, 팀과의 협업을 통해 더 나은 제품을 만드는 것을 즐깁니다.

### Skills
- **Frontend:** HTML5, CSS3, JavaScript (ES6+), TypeScript, React, Vue.js, TailwindCSS
- **Backend:** Node.js, Express
- **Tools:** Git, GitHub, Figma, VS Code, Webpack, Vite

### Experience
**Frontend Developer — (주)테크스타트** (2022.03 ~ 현재)
- React 기반 관리자 대시보드 개발 및 유지보수
- TailwindCSS 도입으로 스타일 작업 효율 40% 향상
- REST API 연동 및 상태 관리 (Redux Toolkit)

**Web Developer Intern — 웹에이전시** (2021.07 ~ 2022.02)
- 고객사 랜딩 페이지 퍼블리싱 (HTML/CSS/JS)
- 반응형 레이아웃 구현 및 크로스 브라우저 대응

### Projects
**포트폴리오 웹사이트**
- 설명: 개인 프로젝트 및 이력을 소개하는 정적 웹사이트
- 기술: HTML, TailwindCSS, JavaScript
- 링크: github.com/devkim/portfolio

**날씨 대시보드**
- 설명: OpenWeather API를 활용한 실시간 날씨 확인 앱
- 기술: React, TailwindCSS, REST API
- 링크: github.com/devkim/weather-app

### Education
**한국대학교 컴퓨터공학과** (2017.03 ~ 2021.02) — 학사 졸업

---

## 단계별 개발 로드맵

### Phase 1 — 프로젝트 초기 설정
- [ ] 디렉토리 구조 생성 (`index.html`, `style.css`, `main.js`)
- [ ] TailwindCSS CDN 또는 CLI 설정
- [ ] 기본 HTML 뼈대 작성 (meta, viewport, font 연결)
- [ ] Google Fonts 또는 시스템 폰트 설정
- [ ] 색상 팔레트 및 디자인 토큰 정의 (tailwind.config.js)

### Phase 2 — 레이아웃 및 네비게이션
- [ ] 고정형 상단 네비게이션 바 구현
- [ ] 섹션 간 앵커 링크 연결
- [ ] 모바일 햄버거 메뉴 구현 (JavaScript)
- [ ] 반응형 그리드 레이아웃 설계

### Phase 3 — 섹션별 UI 구현
- [ ] Hero 섹션 (이름, 직함, CTA 버튼, 소셜 링크)
- [ ] About 섹션 (텍스트 + 프로필 이미지 영역)
- [ ] Skills 섹션 (카테고리별 기술 배지)
- [ ] Experience 섹션 (타임라인 UI)
- [ ] Projects 섹션 (카드 그리드)
- [ ] Education 섹션
- [ ] Contact 섹션 (링크 버튼 모음)

### Phase 4 — 인터랙션 및 애니메이션
- [ ] 스크롤 진행 표시 바
- [ ] 스크롤 시 섹션 페이드인 애니메이션 (Intersection Observer)
- [ ] 네비게이션 활성 섹션 하이라이트
- [ ] 다크 모드 토글

### Phase 5 — 반응형 및 접근성
- [ ] 모바일(375px), 태블릿(768px), 데스크탑(1280px) 뷰 대응
- [ ] 키보드 네비게이션 지원
- [ ] 이미지 alt 속성 및 aria 레이블 추가
- [ ] 색상 대비 접근성 기준 확인 (WCAG AA)

### Phase 6 — 최적화 및 배포
- [ ] 이미지 최적화 (WebP 변환, lazy loading)
- [ ] Lighthouse 성능 점수 확인 및 개선
- [ ] Open Graph 메타 태그 추가 (SNS 공유용)
- [ ] GitHub Pages 또는 Netlify 배포
- [ ] 커스텀 도메인 연결 (선택)

---

## 디렉토리 구조

```
resume/
├── index.html
├── style.css          # TailwindCSS 커스텀 스타일
├── main.js            # 인터랙션 스크립트
├── tailwind.config.js # Tailwind 설정 (CLI 사용 시)
└── assets/
    ├── profile.jpg
    └── favicon.ico
```

---

## 디자인 가이드

| 항목 | 값 |
|------|-----|
| Primary Color | `#3B82F6` (blue-500) |
| Background (Light) | `#F9FAFB` (gray-50) |
| Background (Dark) | `#111827` (gray-900) |
| Text Primary | `#111827` (gray-900) |
| Text Secondary | `#6B7280` (gray-500) |
| Font | Inter, system-ui |
| Border Radius | `rounded-xl` |
| Shadow | `shadow-md` |
