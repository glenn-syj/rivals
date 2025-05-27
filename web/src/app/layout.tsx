import type React from "react";
import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";
import { RivalryProvider } from "@/contexts/RivalryContext";
import RivalryCart from "@/components/RivalryCart";

const inter = Inter({ subsets: ["latin"] });

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
        <RivalryProvider>
          {children}
          <RivalryCart />
        </RivalryProvider>
      </body>
    </html>
  );
}
