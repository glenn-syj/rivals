import type React from "react";
import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";
import RivalryCart from "@/components/RivalryCart";
import { dataDragonService } from "@/lib/dataDragon";
import { Toaster } from "@/components/ui/sonner"; // sonner의 Toaster 컴포넌트 임포트
import { ErrorToastDisplay } from "@/components/ErrorToastDisplay"; // ErrorToastDisplay 컴포넌트 임포트

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
        <Toaster /> {/* 전역 토스트 메시지를 표시할 Toaster 컴포넌트 추가 */}
        <ErrorToastDisplay />{" "}
        {/* 에러 발생 시 토스트를 트리거할 컴포넌트 추가 */}
      </body>
    </html>
  );
}
