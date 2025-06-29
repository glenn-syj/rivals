import type React from "react";
import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";
import RivalryCart from "@/components/RivalryCart";
import { dataDragonService } from "@/lib/dataDragon";

const inter = Inter({ subsets: ["latin"] });

// Initialize DataDragonService at app level
dataDragonService.initialize().catch(console.error);

export const metadata: Metadata = {
  title: "Rivals - TFT 라이벌 관리 서비스",
  description: "TFT 라이벌 관리의 새로운 기준",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="ko">
      <body className={inter.className}>
        {children}
        <RivalryCart />
      </body>
    </html>
  );
}
