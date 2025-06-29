"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Card,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import {
  Search,
  Users,
  Trophy,
  Target,
  BarChart3,
  Sword,
  Crown,
  Star,
  GamepadIcon,
  ShoppingCart,
} from "lucide-react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useRivalryStore } from "@/store/rivalryStore";
import TeamSelectionModal from "@/components/TeamSelectionModal";
import { RiotAccountDto } from "@/lib/types";
import { findRiotAccount } from "@/lib/api";

export type Player = RiotAccountDto;

export default function Component() {
  const [searchInput, setSearchInput] = useState("");
  const [error, setError] = useState("");
  const [selectedPlayer, setSelectedPlayer] = useState<RiotAccountDto | null>(
    null
  );
  const [isTeamModalOpen, setIsTeamModalOpen] = useState(false);
  const router = useRouter();
  const { openRivalryCart, getTotalPlayerCount } = useRivalryStore();

  const handleSearch = async () => {
    if (!searchInput.trim()) return;

    const parts = searchInput.trim().split("#");
    if (parts.length !== 2) {
      setError("올바른 형식으로 입력해주세요 (예: Hide on bush#KR1)");
      return;
    }

    try {
      const [gameName, tagLine] = parts;
      await findRiotAccount(gameName, tagLine);
      router.push(`/summoner/${encodeURIComponent(searchInput.trim())}`);
    } catch (err) {
      setError("소환사를 찾을 수 없습니다");
    }
  };

  return (
    <div className="flex flex-col min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900">
      {/* Header */}
      <header className="px-4 lg:px-6 h-16 flex items-center border-b border-slate-700/50 bg-slate-900/80 backdrop-blur-sm">
        <Link href="/" className="flex items-center justify-center">
          <div className="flex items-center space-x-2">
            <div className="w-8 h-8 bg-gradient-to-br from-slate-600 to-indigo-600 rounded-lg flex items-center justify-center">
              <Sword className="w-5 h-5 text-white" />
            </div>
            <span className="text-xl font-bold bg-gradient-to-r from-slate-400 to-indigo-400 bg-clip-text text-transparent">
              Rivals
            </span>
          </div>
        </Link>
        <nav className="ml-auto flex gap-4 sm:gap-6 items-center">
          <Button
            onClick={openRivalryCart}
            variant="ghost"
            className="text-gray-300 hover:text-indigo-400 relative"
          >
            <ShoppingCart className="w-4 h-4 mr-2" />
            라이벌리 카트
            {getTotalPlayerCount() > 0 && (
              <Badge className="absolute -top-2 -right-2 bg-indigo-600 text-white text-xs min-w-[20px] h-5 flex items-center justify-center rounded-full">
                {getTotalPlayerCount()}
              </Badge>
            )}
          </Button>
          <Link
            href="#features"
            className="text-sm font-medium text-gray-300 hover:text-indigo-400 transition-colors"
          >
            기능
          </Link>
          <Link
            href="#how-it-works"
            className="text-sm font-medium text-gray-300 hover:text-indigo-400 transition-colors"
          >
            사용법
          </Link>
        </nav>
      </header>

      <main className="flex-1">
        {/* Hero Section */}
        <section className="w-full py-12 md:py-24 lg:py-32 xl:py-48 relative overflow-hidden">
          <div className="absolute inset-0 bg-gradient-to-r from-slate-600/10 to-indigo-600/20" />
          <div className="container px-4 md:px-6 relative z-10">
            <div className="flex flex-col items-center space-y-4 text-center">
              <div className="space-y-2">
                <Badge
                  variant="secondary"
                  className="bg-slate-800/80 text-slate-300 border-slate-600"
                >
                  <Star className="w-3 h-3 mr-1" />
                  Riot Games 공식 API 연동
                </Badge>
                <h1 className="text-3xl font-bold tracking-tighter sm:text-4xl md:text-5xl lg:text-6xl/none text-white">
                  TFT{" "}
                  <span className="bg-gradient-to-r from-slate-400 to-indigo-400 bg-clip-text text-transparent">
                    라이벌 관리
                  </span>
                  의 새로운 기준
                </h1>
                <p className="mx-auto max-w-[700px] text-gray-300 md:text-xl">
                  Riot Games API 기반의 정확한 전적 분석과 유연한 라이벌 시스템.
                  TFT에서 당신이 원하는 플레이어들과 라이벌을 맺고 함께
                  성장하세요.
                </p>
              </div>
              <div className="w-full max-w-sm space-y-2">
                <div className="flex gap-2">
                  <Input
                    type="text"
                    placeholder="소환사명#태그 (예: Hide on bush#KR1)"
                    value={searchInput}
                    onChange={(e) => {
                      setSearchInput(e.target.value);
                      setError("");
                    }}
                    onKeyPress={(e) => e.key === "Enter" && handleSearch()}
                    className="flex-1 bg-slate-800/50 border-slate-600 text-white placeholder:text-gray-400"
                  />
                  <Button
                    onClick={handleSearch}
                    className="bg-gradient-to-r from-slate-600 to-indigo-600 hover:from-slate-700 hover:to-indigo-700"
                  >
                    <Search className="w-4 h-4 mr-2" />
                    검색
                  </Button>
                </div>
                {error && <p className="text-xs text-red-400">{error}</p>}
                <p className="text-xs text-gray-400">
                  무료로 시작하세요. 신용카드 필요 없음.
                </p>
              </div>
              <div className="flex flex-col sm:flex-row gap-4 mt-8">
                <Button
                  size="lg"
                  onClick={openRivalryCart}
                  className="bg-gradient-to-r from-slate-600 to-indigo-600 hover:from-slate-700 hover:to-indigo-700"
                >
                  <GamepadIcon className="w-5 h-5 mr-2" />
                  지금 시작하기
                </Button>
                <Button
                  size="lg"
                  variant="outline"
                  className="border-slate-600 text-slate-300 hover:bg-slate-800/50"
                >
                  <BarChart3 className="w-5 h-5 mr-2" />
                  데모 보기
                </Button>
              </div>
            </div>
          </div>
        </section>

        {/* Features Section */}
        <section
          id="features"
          className="w-full py-12 md:py-24 lg:py-32 bg-slate-900/50"
        >
          <div className="container px-4 md:px-6">
            <div className="flex flex-col items-center justify-center space-y-4 text-center">
              <div className="space-y-2">
                <Badge
                  variant="secondary"
                  className="bg-slate-800/80 text-slate-300 border-slate-600"
                >
                  주요 기능
                </Badge>
                <h2 className="text-3xl font-bold tracking-tighter sm:text-5xl text-white">
                  TFT 마스터가 되는 길
                </h2>
                <p className="max-w-[900px] text-gray-300 md:text-xl/relaxed lg:text-base/relaxed xl:text-xl/relaxed">
                  강력한 분석 도구와 라이벌 시스템으로 당신의 TFT 실력을 한 단계
                  끌어올리세요.
                </p>
              </div>
            </div>
            <div className="mx-auto grid max-w-5xl items-center gap-6 py-12 lg:grid-cols-3 lg:gap-12">
              <Card className="bg-slate-800/50 border-slate-700/50 hover:border-indigo-500/50 transition-colors">
                <CardHeader>
                  <div className="w-12 h-12 bg-gradient-to-br from-slate-600 to-indigo-600 rounded-lg flex items-center justify-center mb-4">
                    <Search className="w-6 h-6 text-white" />
                  </div>
                  <CardTitle className="text-white">
                    Riot API 기반 전적 분석
                  </CardTitle>
                  <CardDescription className="text-gray-400">
                    공식 Riot Games API를 통한 실시간 랭크, 승률, 최근 게임 기록
                    분석
                  </CardDescription>
                </CardHeader>
              </Card>
              <Card className="bg-slate-800/50 border-slate-700/50 hover:border-indigo-500/50 transition-colors">
                <CardHeader>
                  <div className="w-12 h-12 bg-gradient-to-br from-slate-600 to-indigo-600 rounded-lg flex items-center justify-center mb-4">
                    <Users className="w-6 h-6 text-white" />
                  </div>
                  <CardTitle className="text-white">
                    유연한 라이벌 시스템
                  </CardTitle>
                  <CardDescription className="text-gray-400">
                    1:1, 1:그룹, 그룹:그룹 등 다양한 형태로 원하는 플레이어들과
                    라이벌 관계 구성
                  </CardDescription>
                </CardHeader>
              </Card>
              <Card className="bg-slate-800/50 border-slate-700/50 hover:border-indigo-500/50 transition-colors">
                <CardHeader>
                  <div className="w-12 h-12 bg-gradient-to-br from-slate-600 to-indigo-600 rounded-lg flex items-center justify-center mb-4">
                    <Trophy className="w-6 h-6 text-white" />
                  </div>
                  <CardTitle className="text-white">라이벌 관계 관리</CardTitle>
                  <CardDescription className="text-gray-400">
                    다양한 형태의 라이벌 관계에서 대전 기록, 순위 변화, 상호
                    성장 과정을 체계적으로 관리
                  </CardDescription>
                </CardHeader>
              </Card>
              <Card className="bg-slate-800/50 border-slate-700/50 hover:border-indigo-500/50 transition-colors">
                <CardHeader>
                  <div className="w-12 h-12 bg-gradient-to-br from-slate-600 to-indigo-600 rounded-lg flex items-center justify-center mb-4">
                    <BarChart3 className="w-6 h-6 text-white" />
                  </div>
                  <CardTitle className="text-white">실시간 순위 추적</CardTitle>
                  <CardDescription className="text-gray-400">
                    라이벌들과의 실시간 LP 변화와 티어 승급/강등 알림 서비스
                  </CardDescription>
                </CardHeader>
              </Card>
              <Card className="bg-slate-800/50 border-slate-700/50 hover:border-indigo-500/50 transition-colors">
                <CardHeader>
                  <div className="w-12 h-12 bg-gradient-to-br from-slate-600 to-indigo-600 rounded-lg flex items-center justify-center mb-4">
                    <Target className="w-6 h-6 text-white" />
                  </div>
                  <CardTitle className="text-white">덱 조합 분석</CardTitle>
                  <CardDescription className="text-gray-400">
                    라이벌들의 선호 덱과 성공률을 분석하여 메타 적응 전략 제공
                  </CardDescription>
                </CardHeader>
              </Card>
              <Card className="bg-slate-800/50 border-slate-700/50 hover:border-indigo-500/50 transition-colors">
                <CardHeader>
                  <div className="w-12 h-12 bg-gradient-to-br from-slate-600 to-indigo-600 rounded-lg flex items-center justify-center mb-4">
                    <Crown className="w-6 h-6 text-white" />
                  </div>
                  <CardTitle className="text-white">
                    성과 비교 대시보드
                  </CardTitle>
                  <CardDescription className="text-gray-400">
                    개인 vs 개인, 개인 vs 그룹, 그룹 vs 그룹 등 다양한 라이벌
                    관계에서의 성과 비교 및 분석
                  </CardDescription>
                </CardHeader>
              </Card>
            </div>
          </div>
        </section>

        {/* How it Works Section */}
        <section id="how-it-works" className="w-full py-12 md:py-24 lg:py-32">
          <div className="container px-4 md:px-6">
            <div className="flex flex-col items-center justify-center space-y-4 text-center">
              <div className="space-y-2">
                <Badge
                  variant="secondary"
                  className="bg-slate-800/80 text-slate-300 border-slate-600"
                >
                  사용법
                </Badge>
                <h2 className="text-3xl font-bold tracking-tighter sm:text-5xl text-white">
                  3단계로 시작하는 라이벌 시스템
                </h2>
              </div>
            </div>
            <div className="mx-auto grid max-w-5xl items-center gap-6 py-12 lg:grid-cols-3 lg:gap-12">
              <div className="flex flex-col items-center space-y-4 text-center">
                <div className="w-16 h-16 bg-gradient-to-br from-slate-600 to-indigo-600 rounded-full flex items-center justify-center text-white text-xl font-bold">
                  1
                </div>
                <h3 className="text-xl font-bold text-white">Riot 계정 연동</h3>
                <p className="text-gray-400">
                  Riot Games 계정을 연동하여 정확한 TFT 전적과 현재 랭크 정보를
                  불러옵니다.
                </p>
              </div>
              <div className="flex flex-col items-center space-y-4 text-center">
                <div className="w-16 h-16 bg-gradient-to-br from-slate-600 to-indigo-600 rounded-full flex items-center justify-center text-white text-xl font-bold">
                  2
                </div>
                <h3 className="text-xl font-bold text-white">
                  라이벌 관계 구성
                </h3>
                <p className="text-gray-400">
                  원하는 플레이어나 그룹을 직접 선택하여 1:1, 1:그룹, 그룹:그룹
                  등 다양한 형태의 라이벌 관계를 만들 수 있습니다.
                </p>
              </div>
              <div className="flex flex-col items-center space-y-4 text-center">
                <div className="w-16 h-16 bg-gradient-to-br from-slate-600 to-indigo-600 rounded-full flex items-center justify-center text-white text-xl font-bold">
                  3
                </div>
                <h3 className="text-xl font-bold text-white">함께 성장하기</h3>
                <p className="text-gray-400">
                  라이벌들과의 순위 경쟁을 통해 동기부여를 받고 지속적으로
                  실력을 향상시키세요.
                </p>
              </div>
            </div>
          </div>
        </section>

        {/* CTA Section */}
        <section className="w-full py-12 md:py-24 lg:py-32 bg-slate-900/50">
          <div className="container px-4 md:px-6">
            <div className="flex flex-col items-center justify-center space-y-4 text-center">
              <div className="space-y-2">
                <h2 className="text-3xl font-bold tracking-tighter sm:text-4xl md:text-5xl text-white">
                  지금 바로 시작하세요
                </h2>
                <p className="mx-auto max-w-[600px] text-gray-300 md:text-xl/relaxed lg:text-base/relaxed xl:text-xl/relaxed">
                  당신의 TFT 여정을 한 단계 업그레이드하고, 새로운 라이벌들과
                  함께 성장하세요.
                </p>
              </div>
              <div className="w-full max-w-sm space-y-2">
                <div className="flex gap-2">
                  <Input
                    type="text"
                    placeholder="소환사명#태그 (예: Hide on bush#KR1)"
                    value={searchInput}
                    onChange={(e) => {
                      setSearchInput(e.target.value);
                      setError("");
                    }}
                    onKeyPress={(e) => e.key === "Enter" && handleSearch()}
                    className="flex-1 bg-slate-800/50 border-slate-600 text-white placeholder:text-gray-400"
                  />
                  <Button
                    onClick={handleSearch}
                    className="bg-gradient-to-r from-slate-600 to-indigo-600 hover:from-slate-700 hover:to-indigo-700"
                  >
                    시작하기
                  </Button>
                </div>
                {error && <p className="text-xs text-red-400">{error}</p>}
                <p className="text-xs text-gray-400">
                  무료 체험 • 언제든 취소 가능
                </p>
              </div>
            </div>
          </div>
        </section>
      </main>

      {/* Footer */}
      <footer className="flex flex-col gap-2 sm:flex-row py-6 w-full shrink-0 items-center px-4 md:px-6 border-t border-slate-700/50 bg-slate-900/80">
        <p className="text-xs text-gray-400">
          © 2024 Rivals. All rights reserved.
        </p>
        <nav className="sm:ml-auto flex gap-4 sm:gap-6">
          <Link
            href="https://github.com/glenn-syj/rivals"
            className="text-xs text-gray-400 hover:text-indigo-400 transition-colors"
          >
            GitHub
          </Link>
          <Link
            href="#"
            className="text-xs text-gray-400 hover:text-indigo-400 transition-colors"
          >
            이용약관
          </Link>
          <Link
            href="#"
            className="text-xs text-gray-400 hover:text-indigo-400 transition-colors"
          >
            개인정보처리방침
          </Link>
          <Link
            href="#"
            className="text-xs text-gray-400 hover:text-indigo-400 transition-colors"
          >
            고객지원
          </Link>
        </nav>
      </footer>

      <TeamSelectionModal
        player={null}
        isOpen={isTeamModalOpen}
        onClose={() => {
          setIsTeamModalOpen(false);
          setSelectedPlayer(null);
        }}
      />
    </div>
  );
}
