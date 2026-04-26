import Link from "next/link"
import { GitBranch } from "lucide-react"

import { siteConfig } from "@/lib/site"
import { Separator } from "@/components/ui/separator"

export function SiteFooter() {
  return (
    <footer>
      <Separator />
      <div className="container mx-auto flex h-14 items-center justify-between px-4 text-sm text-muted-foreground">
        <p>
          © {new Date().getFullYear()} {siteConfig.name}. All rights reserved.
        </p>
        <Link
          href={siteConfig.links.github}
          target="_blank"
          rel="noreferrer"
          className="flex items-center gap-1.5 hover:text-foreground transition-colors"
        >
          <GitBranch className="size-4" />
          <span>GitHub</span>
        </Link>
      </div>
    </footer>
  )
}
