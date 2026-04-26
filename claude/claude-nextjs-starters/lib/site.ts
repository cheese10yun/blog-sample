export const siteConfig = {
  name: "StarterKit",
  description:
    "Next.js + TypeScript + TailwindCSS + ShadcnUI 기반 모던 웹 스타터킷",
  url: "https://example.com",
  navItems: [
    { label: "홈", href: "/" },
    { label: "기능", href: "/features" },
    { label: "소개", href: "/about" },
  ],
  links: {
    github: "https://github.com",
  },
}

export type SiteConfig = typeof siteConfig
