import Link from "next/link"
import { Layers } from "lucide-react"

import { siteConfig } from "@/lib/site"
import { MainNav } from "@/components/layout/main-nav"
import { MobileNav } from "@/components/layout/mobile-nav"
import { ThemeToggle } from "@/components/layout/theme-toggle"
import { Separator } from "@/components/ui/separator"

export function SiteHeader() {
  return (
    <header className="sticky top-0 z-40 w-full bg-background/80 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="container mx-auto flex h-14 items-center gap-4 px-4">
        <Link href="/" className="flex items-center gap-2 font-semibold">
          <Layers className="size-5" />
          <span>{siteConfig.name}</span>
        </Link>
        <div className="flex flex-1 items-center justify-between">
          <MainNav />
          <div className="flex items-center gap-1">
            <ThemeToggle />
            <MobileNav />
          </div>
        </div>
      </div>
      <Separator />
    </header>
  )
}
