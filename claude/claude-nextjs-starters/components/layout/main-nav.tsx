"use client"

import Link from "next/link"
import { usePathname } from "next/navigation"

import { cn } from "@/lib/utils"
import { siteConfig } from "@/lib/site"

export function MainNav() {
  const pathname = usePathname()

  return (
    <nav className="hidden md:flex items-center gap-6 text-sm">
      {siteConfig.navItems.map((item) => (
        <Link
          key={item.href}
          href={item.href}
          className={cn(
            "transition-colors hover:text-foreground/80",
            pathname === item.href
              ? "text-foreground font-medium"
              : "text-foreground/60"
          )}
        >
          {item.label}
        </Link>
      ))}
    </nav>
  )
}
