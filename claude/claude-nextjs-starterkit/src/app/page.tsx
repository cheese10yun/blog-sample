import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { ThemeToggle } from "@/components/theme-toggle";
import { Code, Palette, Rocket, Zap } from "lucide-react";

const stacks = [
  {
    icon: Rocket,
    title: "Next.js 15",
    description: "App Router, Server Components, Turbopack으로 빠른 빌드와 라우팅을 제공합니다.",
    badge: "Framework",
  },
  {
    icon: Palette,
    title: "Tailwind CSS v4",
    description: "config 파일 없이 CSS-first @theme으로 커스텀 디자인 시스템을 구성합니다.",
    badge: "Styling",
  },
  {
    icon: Code,
    title: "shadcn/ui",
    description: "복사-붙여넣기 방식의 접근성 있는 Radix UI 기반 컴포넌트 라이브러리입니다.",
    badge: "Components",
  },
  {
    icon: Zap,
    title: "TypeScript",
    description: "strict 모드로 타입 안정성을 보장하는 개발 환경을 제공합니다.",
    badge: "Language",
  },
] as const;

export default function Page() {
  return (
    <div className="min-h-screen bg-background">
      <header className="border-b">
        <div className="container mx-auto flex h-16 items-center justify-between px-4">
          <div className="flex items-center gap-2">
            <Rocket className="h-6 w-6" />
            <span className="font-semibold">Next.js Starter Kit</span>
          </div>
          <ThemeToggle />
        </div>
      </header>

      <main className="container mx-auto px-4 py-16">
        <div className="mb-12 text-center">
          <Badge className="mb-4">v0.1.0</Badge>
          <h1 className="mb-4 text-4xl font-bold tracking-tight">
            빠르게 시작하는 Next.js 스타터 킷
          </h1>
          <p className="mx-auto max-w-2xl text-lg text-muted-foreground">
            Next.js 15, TypeScript, Tailwind CSS v4, shadcn/ui로 구성된 현대적인 웹 개발 시작점.
          </p>
          <div className="mt-8 flex justify-center gap-4">
            <Button size="lg">시작하기</Button>
            <Button size="lg" variant="outline">
              문서 보기
            </Button>
          </div>
        </div>

        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
          {stacks.map((stack) => (
            <Card key={stack.title}>
              <CardHeader>
                <div className="mb-2 flex items-center gap-2">
                  <stack.icon className="h-5 w-5" />
                  <Badge variant="secondary">{stack.badge}</Badge>
                </div>
                <CardTitle>{stack.title}</CardTitle>
                <CardDescription>{stack.description}</CardDescription>
              </CardHeader>
              <CardContent />
            </Card>
          ))}
        </div>
      </main>
    </div>
  );
}
