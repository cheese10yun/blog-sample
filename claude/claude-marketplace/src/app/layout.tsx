import type { Metadata } from "next";
import { Playfair_Display, DM_Sans } from "next/font/google";
import "./globals.css";

const playfair = Playfair_Display({
  variable: "--font-playfair",
  subsets: ["latin"],
  display: "swap",
});

const dmSans = DM_Sans({
  variable: "--font-dm-sans",
  subsets: ["latin"],
  display: "swap",
});

export const metadata: Metadata = {
  title: "BrewPick — 나만의 맥주 큐레이터",
  description:
    "취향, 상황, 페어링 음식을 기반으로 최적의 맥주를 추천하는 AI 큐레이션 플랫폼",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko" className={`${playfair.variable} ${dmSans.variable}`}>
      <body className="min-h-full antialiased">{children}</body>
    </html>
  );
}
