import type { Metadata } from "next"
import Link from "next/link"
import { ArrowRight } from "lucide-react"

import { Button } from "@/components/ui/button"
import { siteConfig } from "@/lib/site"

export const metadata: Metadata = {
  title: "소개",
  description: `${siteConfig.name} 프로젝트 소개`,
}

export default function AboutPage() {
  return (
    <section className="container mx-auto max-w-2xl px-4 py-20">
      <h1 className="mb-4 text-3xl font-bold tracking-tight">소개</h1>
      <p className="mb-6 leading-relaxed text-muted-foreground">
        {siteConfig.name}은 {siteConfig.description}입니다. App Router,
        TypeScript, TailwindCSS, ShadcnUI 기반으로 구성되어 있어 프로덕트
        개발에 곧바로 집중할 수 있습니다.
      </p>
      <Button asChild>
        <Link href={siteConfig.links.github} target="_blank" rel="noreferrer">
          GitHub 저장소
          <ArrowRight className="ml-1 size-4" />
        </Link>
      </Button>
    </section>
  )
}
