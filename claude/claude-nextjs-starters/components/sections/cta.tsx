import Link from "next/link"
import { ArrowRight } from "lucide-react"

import { Button } from "@/components/ui/button"
import { siteConfig } from "@/lib/site"

export function CTA() {
  return (
    <section id="about" className="container mx-auto px-4 py-20">
      <div className="rounded-2xl bg-muted px-8 py-16 text-center">
        <h2 className="mb-4 text-3xl font-bold tracking-tight">
          지금 바로 시작하세요
        </h2>
        <p className="mx-auto mb-8 max-w-md text-muted-foreground">
          GitHub에서 클론 후 npm install 한 번으로 개발 환경이 준비됩니다.
          나머지는 제품 기능에만 집중하세요.
        </p>
        <Button size="lg" asChild>
          <Link
            href={siteConfig.links.github}
            target="_blank"
            rel="noreferrer"
          >
            GitHub에서 보기
            <ArrowRight className="ml-1 size-4" />
          </Link>
        </Button>
      </div>
    </section>
  )
}
