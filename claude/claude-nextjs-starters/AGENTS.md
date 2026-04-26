<!-- BEGIN:nextjs-agent-rules -->
# 이 Next.js는 학습 데이터와 다르다

Next.js **16.2.4** / React **19.2.4** 기준. `package.json` 버전이 다르면 이 파일을 신뢰하기 전에 `node_modules/next/dist/docs/01-app/02-guides/upgrading/version-16.md` 먼저 확인.

---

## 스택

- Next.js 16.2.4, React 19.2.4, TypeScript (strict)
- Tailwind v4 — `tailwind.config` 파일 없음. `app/globals.css` 안에서 `@theme` 블록으로 토큰 선언
- shadcn/ui (style: `radix-nova`), Lucide icons, `radix-ui` meta-package
- npm (package-lock.json). **Node 20.9+ 필요** (Node 18 지원 중단)

## 명령어

```
npm run dev      # Turbopack 기본
npm run build
npm run start
npm run lint     # ESLint flat config (eslint.config.mjs) 직접 호출
```

**없는 스크립트**: `npm test`, `npm run format`, `npm run typecheck` — 임의로 호출하지 말 것.  
타입 검사 필요 시: `npx tsc --noEmit`

---

## Next.js 16 함정 — 이것 때문에 코드가 깨진다

### Async Request APIs (완전 제거)
`cookies()`, `headers()`, `draftMode()`, `params`, `searchParams` 전부 **`await` 필수**.  
v15의 sync escape hatch (`UnsafeUnwrapped*`) 완전 삭제됨.

```ts
// ❌ 이전
export default function Page({ params }) {
  const { slug } = params
}

// ✅ 현재
export default async function Page({ params }: PageProps<'/blog/[slug]'>) {
  const { slug } = await params
}
```

동적 라우트 prop 타입은 `npx next typegen`으로 생성하는 `PageProps<'/blog/[slug]'>` 헬퍼 사용.

### middleware.ts → proxy.ts
미들웨어 파일은 `proxy.ts`로 바뀜. named export도 `middleware` → `proxy`.  
`edge` runtime **미지원** — runtime은 `nodejs` 고정, 설정 불가.  
설정 플래그도 변경: `skipMiddlewareUrlNormalize` → `skipProxyUrlNormalize`.

### revalidateTag — 두 번째 인자 필수
```ts
// ❌ 단일 인자는 TS 에러
revalidateTag('posts')

// ✅ cacheLife 프로필 인자 필수
revalidateTag('posts', 'max')
```

### cacheLife / cacheTag
`unstable_` prefix 제거됨. `next/cache`에서 바로 import:
```ts
import { cacheLife, cacheTag } from 'next/cache'
```

### PPR 플래그 제거
`experimental.ppr` 없어짐 → `next.config.ts`의 top-level `cacheComponents: true` 사용.

### Turbopack 기본
`next dev` / `next build` 모두 Turbopack 기본. webpack config 추가 시 빌드 실패.  
webpack 유지하려면 `--webpack` 플래그 필요.

### next lint 제거
`next lint` 명령 없어짐. `npm run lint`는 ESLint를 직접 호출 (이미 그렇게 설정됨).

### fetch 기본 캐시 안 됨
```ts
// 캐시하려면 명시 필요
fetch(url, { cache: 'force-cache' })
// 또는 cacheLife / cacheTag 사용
```

### next/image 기본값 변경
- `qualities` 기본값 `[75]`만. 다른 값은 `images.qualities`에 명시
- 로컬 이미지에 쿼리스트링 쓰면 `images.localPatterns.search` 필요
- `images.domains` deprecated → `remotePatterns` 사용

### 제거된 API — 사용 금지
`next/amp`, `useAmp`, `serverRuntimeConfig`, `publicRuntimeConfig`, `next/config`, `unstable_rootParams`, `runtime: 'experimental-edge'`

---

## 프로젝트 컨벤션

- `@/*` alias = 프로젝트 루트 (`src/` 디렉토리 없음)
- 서버 컴포넌트 기본. `"use client"`는 인터랙티브 leaf에만 — 페이지/섹션으로 끌어올리지 말 것
- 컴포넌트 배치: shadcn primitives → `components/ui/`, 사이트 chrome → `components/layout/`, 페이지 빌딩 블록 → `components/sections/`
- 클래스 머지: 항상 `cn()` from `@/lib/utils`
- 사이트 전역 상수 (이름·설명·nav 링크): `lib/site.ts` — 새 페이지 nav 추가 시 여기서 갱신
- 모든 라우트는 `Metadata` export
- 코드 스타일: double-quoted strings, 세미콜론 없음, import 그룹 순서 (next → 외부 → `@/`)
- UI 텍스트 / aria-label / metadata description: **한국어**. 코드 식별자: 영어

## shadcn/ui 컴포넌트 추가

```
npx shadcn@latest add <component>
```

`radix-ui` meta-package (`^1.4.3`) 사용 중 — 개별 `@radix-ui/*` 패키지 별도 추가 금지.

## 현재 없는 것 — 가정 금지

- 데이터 페칭 없음 (모든 페이지 정적). API 추가 시 fetch 캐시 함정 주의
- `proxy.ts`, `instrumentation.ts`, `.env*` 없음
- `loading.tsx`, `error.tsx`, `not-found.tsx`, `route.ts` 없음 — 추가 시 Next 16 docs 확인
- Prettier / Biome 없음. ESLint flat config(`eslint.config.mjs`)만

## 참고 docs

- `node_modules/next/dist/docs/01-app/02-guides/upgrading/version-16.md`
- `node_modules/next/dist/docs/01-app/02-guides/migrating-to-cache-components.md`
- `node_modules/next/dist/docs/01-app/03-api-reference/03-file-conventions/proxy.md`
<!-- END:nextjs-agent-rules -->
