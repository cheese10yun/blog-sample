import type { Metadata } from "next"
import Link from "next/link"

import { Button } from "@/components/ui/button"
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { siteConfig } from "@/lib/site"

export const metadata: Metadata = {
  title: "로그인",
  description: `${siteConfig.name} 계정으로 로그인합니다.`,
}

export default function LoginPage() {
  return (
    <main className="flex min-h-[calc(100svh-3.5rem)] w-full items-center justify-center px-4 py-10 sm:py-16">
      <Card className="w-full max-w-sm">
        <CardHeader className="gap-2 text-center">
          <CardTitle className="text-xl sm:text-2xl">로그인</CardTitle>
          <CardDescription>
            이메일과 비밀번호를 입력해 로그인하세요.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form className="flex flex-col gap-5">
            <div className="flex flex-col gap-2">
              <Label htmlFor="email">이메일</Label>
              <Input
                id="email"
                name="email"
                type="email"
                placeholder="name@example.com"
                autoComplete="email"
                required
              />
            </div>
            <div className="flex flex-col gap-2">
              <div className="flex items-center justify-between">
                <Label htmlFor="password">비밀번호</Label>
                <Link
                  href="#"
                  className="text-xs text-muted-foreground underline-offset-4 hover:underline"
                >
                  비밀번호 찾기
                </Link>
              </div>
              <Input
                id="password"
                name="password"
                type="password"
                autoComplete="current-password"
                required
              />
            </div>
            <Button type="submit" className="w-full">
              로그인하기
            </Button>
            <p className="text-center text-sm text-muted-foreground">
              계정이 없으신가요?{" "}
              <Link
                href="/signup"
                className="font-medium text-foreground underline-offset-4 hover:underline"
              >
                회원가입
              </Link>
            </p>
          </form>
        </CardContent>
      </Card>
    </main>
  )
}
