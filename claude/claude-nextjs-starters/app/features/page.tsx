import type { Metadata } from "next"
import { Features } from "@/components/sections/features"

export const metadata: Metadata = {
  title: "기능",
  description: "스타터킷이 제공하는 핵심 기능을 소개합니다.",
}

export default function FeaturesPage() {
  return <Features />
}
