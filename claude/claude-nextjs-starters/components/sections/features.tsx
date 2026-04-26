import { type LucideIcon, Layers, Palette, Shield, Zap } from "lucide-react"

import {
  Card,
  CardContent,
  CardDescription,
  CardTitle,
} from "@/components/ui/card"

const features: { icon: LucideIcon; title: string; description: string }[] = [
  {
    icon: Zap,
    title: "빠른 시작",
    description:
      "헤더, 푸터, 네비게이션, 다크모드가 이미 구성되어 있어 클론 즉시 개발을 시작할 수 있습니다.",
  },
  {
    icon: Palette,
    title: "다크모드 지원",
    description:
      "next-themes와 ShadcnUI 디자인 토큰의 조합으로 라이트·다크·시스템 3가지 테마를 지원합니다.",
  },
  {
    icon: Shield,
    title: "타입 안전",
    description:
      "TypeScript strict 모드와 Next.js 16의 PageProps / LayoutProps 전역 타입을 활용합니다.",
  },
  {
    icon: Layers,
    title: "ShadcnUI 컴포넌트",
    description:
      "radix-nova 스타일의 접근성 친화적 UI 컴포넌트가 즉시 사용 가능하도록 세팅되어 있습니다.",
  },
]

export function Features() {
  return (
    <section id="features" className="container mx-auto px-4 py-20">
      <div className="mb-12 text-center">
        <h2 className="mb-3 text-3xl font-bold tracking-tight">
          스타터킷 구성
        </h2>
        <p className="mx-auto max-w-lg text-muted-foreground">
          반복 작업 없이 제품 기능 개발에 바로 집중할 수 있도록 핵심 기반을
          모두 담았습니다.
        </p>
      </div>
      <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
        {features.map(({ icon: Icon, title, description }) => (
          <Card key={title}>
            <CardContent className="flex flex-col gap-3">
              <Icon className="size-8 text-primary" />
              <CardTitle className="text-base">{title}</CardTitle>
              <CardDescription>{description}</CardDescription>
            </CardContent>
          </Card>
        ))}
      </div>
    </section>
  )
}
