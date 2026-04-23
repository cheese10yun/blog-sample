# Next.js Starter Kit

Next.js 15 + TypeScript + Tailwind CSS v4 + shadcn/ui + lucide-react로 구성된 웹 개발 스타터 킷.

## 기술 스택

- **[Next.js 15](https://nextjs.org/)** — App Router, Turbopack
- **[TypeScript](https://www.typescriptlang.org/)** — strict 모드
- **[Tailwind CSS v4](https://tailwindcss.com/)** — CSS-first, `tailwind.config` 없음
- **[shadcn/ui](https://ui.shadcn.com/)** — new-york 스타일, neutral 컬러
- **[lucide-react](https://lucide.dev/)** — 아이콘
- **[next-themes](https://github.com/pacocoursey/next-themes)** — 다크 모드

## 요구사항

- Node.js **20.9+**
- pnpm **9+**

## 시작하기

```bash
pnpm install
pnpm dev
```

브라우저에서 `http://localhost:3000`을 열면 됩니다.

## 스크립트

| 명령어 | 설명 |
|---|---|
| `pnpm dev` | 개발 서버 실행 (Turbopack) |
| `pnpm build` | 프로덕션 빌드 |
| `pnpm start` | 프로덕션 서버 실행 |
| `pnpm lint` | ESLint 실행 |
| `pnpm lint:fix` | ESLint 자동 수정 |
| `pnpm format` | Prettier 포맷 |

## 디렉토리 구조

```
src/
├── app/
│   ├── globals.css       # Tailwind v4 + shadcn CSS 변수
│   ├── layout.tsx        # 루트 레이아웃 + ThemeProvider
│   └── page.tsx          # 데모 랜딩 페이지
├── components/
│   ├── theme-provider.tsx
│   ├── theme-toggle.tsx
│   └── ui/               # shadcn 컴포넌트
│       ├── badge.tsx
│       ├── button.tsx
│       ├── card.tsx
│       └── dropdown-menu.tsx
└── lib/
    └── utils.ts          # cn() 헬퍼
```

## shadcn 컴포넌트 추가

```bash
pnpm dlx shadcn@latest add <컴포넌트명>
# 예시
pnpm dlx shadcn@latest add dialog
pnpm dlx shadcn@latest add form
pnpm dlx shadcn@latest add table
```

## 경로 별칭

`@/*`는 `src/*`로 매핑됩니다.

```ts
import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
```
