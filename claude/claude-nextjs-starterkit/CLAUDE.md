# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 개발 명령어

```bash
pnpm dev          # 개발 서버 실행 (Turbopack, http://localhost:3000)
pnpm build        # 프로덕션 빌드
pnpm start        # 프로덕션 서버 실행
pnpm lint         # ESLint 실행
pnpm lint:fix     # ESLint 자동 수정
pnpm format       # Prettier 포맷
```

테스트 프레임워크는 구성되어 있지 않습니다.

## 기술 스택

- **Next.js 15** — App Router, Turbopack
- **TypeScript** — strict 모드
- **Tailwind CSS v4** — CSS-first 방식 (`tailwind.config` 없음)
- **shadcn/ui** — new-york 스타일, neutral 컬러, RSC 지원
- **next-themes** — 다크 모드 (`ThemeProvider`가 루트 레이아웃에 설정됨)
- **lucide-react** — 아이콘

## 아키텍처

`@/*`는 `src/*`로 매핑됩니다.

```
src/
├── app/
│   ├── globals.css   # Tailwind v4 임포트 + shadcn CSS 변수 (oklch 색상 공간)
│   └── layout.tsx    # 루트 레이아웃: 폰트(Geist), ThemeProvider 래핑
├── components/
│   ├── theme-provider.tsx  # next-themes ThemeProvider 래퍼
│   ├── theme-toggle.tsx    # 라이트/다크/시스템 전환 버튼
│   └── ui/                 # shadcn 컴포넌트 (CLI로 추가)
└── lib/
    └── utils.ts      # cn() — clsx + tailwind-merge 조합
```

## Tailwind CSS v4 주의사항

Tailwind v4는 CSS-first 방식입니다. 테마 커스터마이징은 `globals.css`의 `@theme inline` 블록에서 CSS 변수로 처리합니다. `tailwind.config.ts` 파일을 생성하지 마세요.

## shadcn 컴포넌트 추가

```bash
pnpm dlx shadcn@latest add <컴포넌트명>
```

추가된 컴포넌트는 `src/components/ui/`에 생성됩니다.
