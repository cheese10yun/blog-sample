# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 언어 및 커뮤니케이션 규칙

- **기본 응답 언어**: 한국어
- **코드 주석**: 한국어로 작성
- **커밋 메시지**: 한국어로 작성
- **문서화**: 한국어로 작성
- **변수명/함수명**: 영어 (코드 표준 준수)

## 프로젝트 개요

개발자 웹 이력서 — 빌드 도구 없이 HTML, CSS, JavaScript(Vanilla), TailwindCSS CDN만으로 구성된 단일 페이지 정적 사이트.

## 로컬 개발 서버 실행

빌드 파이프라인이 없는 정적 파일 프로젝트이다. 다음 중 하나로 브라우저 확인:

```bash
# Python 내장 서버 (포트 8000)
python3 -m http.server 8000

# Node.js가 설치된 경우
npx serve .

# VS Code Live Server 확장 사용 시 index.html에서 "Open with Live Server"
```

TailwindCSS CLI를 사용하는 경우(CDN 대신):

```bash
# 설치
npm install -D tailwindcss

# 빌드 (변경 감지 포함)
npx tailwindcss -i ./style.css -o ./output.css --watch
```

## 아키텍처

```
index.html          — 전체 이력서 마크업 (단일 HTML 파일)
style.css           — TailwindCSS @layer 기반 커스텀 스타일
main.js             — 인터랙션 전담 (Intersection Observer, 다크 모드, 햄버거 메뉴)
tailwind.config.js  — 색상 토큰 및 폰트 확장 (CLI 사용 시)
assets/             — 이미지, favicon
```

**핵심 설계 원칙**
- JavaScript는 DOM 조작과 애니메이션 트리거만 담당하고, 스타일 결정은 TailwindCSS 클래스로 처리한다.
- 다크 모드는 `<html>` 태그의 `dark` 클래스 토글 방식(`class` 전략)으로 구현한다.
- 섹션 간 이동은 앵커 링크(`#section-id`)로만 처리하고 라우터를 사용하지 않는다.
- 스크롤 애니메이션은 `IntersectionObserver`를 사용하고 CSS transition으로 동작시킨다.

## 디자인 토큰

| 토큰 | 값 |
|------|----|
| Primary | `#3B82F6` (blue-500) |
| Background Light | `#F9FAFB` (gray-50) |
| Background Dark | `#111827` (gray-900) |
| Text Primary | `#111827` (gray-900) |
| Text Secondary | `#6B7280` (gray-500) |
| Font | Inter, system-ui |

## 반응형 브레이크포인트

TailwindCSS 기본 브레이크포인트를 그대로 사용한다:
- 모바일 기본: `< 768px`
- 태블릿: `md:` (768px~)
- 데스크탑: `lg:` (1024px~) / `xl:` (1280px~)

## 이력서 섹션 ID 규칙

각 `<section>` 태그의 `id`는 영어 소문자 케밥 케이스를 사용한다:
`#hero` `#about` `#skills` `#experience` `#projects` `#education` `#contact`
