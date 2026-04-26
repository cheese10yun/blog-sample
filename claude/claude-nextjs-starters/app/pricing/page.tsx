import type { Metadata } from "next"
import { Check } from "lucide-react"

import { Button } from "@/components/ui/button"
import {
  Card,
  CardContent,
  CardDescription,
  CardTitle,
} from "@/components/ui/card"

export const metadata: Metadata = {
  title: "가격",
  description: "합리적인 가격으로 시작하세요.",
}

const plans = [
  {
    name: "무료",
    price: "₩0",
    period: "영구 무료",
    description: "개인 프로젝트나 사이드 프로젝트에 적합합니다.",
    features: [
      "프로젝트 3개",
      "월 1,000건 API 호출",
      "커뮤니티 지원",
      "기본 대시보드",
    ],
    cta: "무료로 시작",
    highlighted: false,
  },
  {
    name: "프로",
    price: "₩19,900",
    period: "월",
    description: "성장하는 팀과 스타트업을 위한 플랜입니다.",
    features: [
      "프로젝트 무제한",
      "월 100,000건 API 호출",
      "우선 이메일 지원",
      "고급 분석 대시보드",
      "팀원 5명",
      "커스텀 도메인",
    ],
    cta: "프로 시작하기",
    highlighted: true,
  },
  {
    name: "엔터프라이즈",
    price: "문의",
    period: "",
    description: "대규모 조직을 위한 맞춤형 솔루션입니다.",
    features: [
      "모든 프로 기능 포함",
      "API 호출 무제한",
      "전담 계정 매니저",
      "SLA 99.99% 보장",
      "팀원 무제한",
      "온프레미스 배포 옵션",
      "보안 감사 지원",
    ],
    cta: "영업팀 문의",
    highlighted: false,
  },
]

export default function PricingPage() {
  return (
    <section className="container mx-auto px-4 py-20">
      <div className="mb-12 text-center">
        <h1 className="mb-3 text-3xl font-bold tracking-tight">가격 플랜</h1>
        <p className="mx-auto max-w-lg text-muted-foreground">
          규모에 맞는 플랜을 선택하세요. 언제든지 업그레이드하거나 다운그레이드할 수 있습니다.
        </p>
      </div>
      <div className="grid gap-8 sm:grid-cols-2 lg:grid-cols-3">
        {plans.map((plan) => (
          <Card
            key={plan.name}
            className={plan.highlighted ? "border-primary shadow-lg" : ""}
          >
            <CardContent className="flex flex-col gap-4 p-6">
              {plan.highlighted && (
                <span className="w-fit rounded-full bg-primary px-3 py-0.5 text-xs font-medium text-primary-foreground">
                  인기
                </span>
              )}
              <div>
                <CardTitle className="text-xl">{plan.name}</CardTitle>
                <CardDescription className="mt-1">{plan.description}</CardDescription>
              </div>
              <div className="flex items-end gap-1">
                <span className="text-3xl font-bold">{plan.price}</span>
                {plan.period && (
                  <span className="mb-1 text-sm text-muted-foreground">/ {plan.period}</span>
                )}
              </div>
              <ul className="flex flex-col gap-2">
                {plan.features.map((feature) => (
                  <li key={feature} className="flex items-center gap-2 text-sm">
                    <Check className="size-4 shrink-0 text-primary" />
                    {feature}
                  </li>
                ))}
              </ul>
              <Button
                className="mt-2 w-full"
                variant={plan.highlighted ? "default" : "outline"}
              >
                {plan.cta}
              </Button>
            </CardContent>
          </Card>
        ))}
      </div>
    </section>
  )
}
