import Link from "next/link"
import { ArrowRight } from "lucide-react"

import { Button } from "@/components/ui/button"
import { siteConfig } from "@/lib/site"

export function Hero() {
  return (
    <section className="container mx-auto flex flex-col items-center gap-8 px-4 py-24 text-center">
      <div className="inline-flex items-center rounded-full border px-4 py-1.5 text-xs font-medium text-muted-foreground">
        Next.js 16 · ShadcnUI · TailwindCSS v4
      </div>
      <h1 className="max-w-2xl text-4xl font-bold tracking-tight sm:text-5xl lg:text-6xl">
        빠르게 시작하는{" "}
        <span className="text-primary">모던 웹 스타터킷</span>
      </h1>
      <p className="max-w-xl text-lg leading-relaxed text-muted-foreground">
        App Router, TypeScript, TailwindCSS, ShadcnUI를 기반으로 구성된
        프로덕션 레디 스타터킷입니다. 반복 작업 없이 제품 기능 개발에 바로
        집중하세요.
      </p>
      <div className="flex flex-col gap-3 sm:flex-row">
        <Button size="lg" asChild>
          <Link href="/features">
            시작하기
            <ArrowRight className="ml-1 size-4" />
          </Link>
        </Button>
        <Button size="lg" variant="outline" asChild>
          <Link
            href={siteConfig.links.github}
            target="_blank"
            rel="noreferrer"
          >
            GitHub 보기
          </Link>
        </Button>
      </div>
    </section>
  )
}
