"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useRivalry } from "@/contexts/RivalryContext";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { ArrowLeft, ShoppingCart, Search, Sword } from "lucide-react";

export function SummonerHeader() {
  const router = useRouter();
  const { openRivalryCart, getTotalPlayerCount } = useRivalry();
  const [searchInput, setSearchInput] = useState("");
  const [searchError, setSearchError] = useState("");

  const handleNewSearch = async () => {
    if (!searchInput.trim()) return;

    const parts = searchInput.trim().split("#");
    if (parts.length !== 2) {
      setSearchError("올바른 형식으로 입력해주세요 (예: Hide on bush#KR1)");
      return;
    }

    const searchKey = searchInput.trim();
    if (searchKey) {
      router.push(`/summoner/${encodeURIComponent(searchInput.trim())}`);
    } else {
      setSearchError("소환사를 찾을 수 없습니다");
    }
  };

  return (
    <header className="px-4 lg:px-6 h-16 flex items-center border-b border-slate-700/50 bg-slate-900/80 backdrop-blur-sm sticky top-0 z-50">
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
      <div className="flex-1 max-w-md mx-4">
        <div className="flex gap-2">
          <Input
            type="text"
            placeholder="다른 소환사 검색..."
            value={searchInput}
            onChange={(e) => {
              setSearchInput(e.target.value);
              setSearchError("");
            }}
            onKeyPress={(e) => e.key === "Enter" && handleNewSearch()}
            className="flex-1 bg-slate-800/50 border-slate-600 text-white placeholder:text-gray-400"
          />
          <Button
            onClick={handleNewSearch}
            size="sm"
            className="bg-indigo-600 hover:bg-indigo-700"
          >
            <Search className="w-4 h-4" />
          </Button>
        </div>
        {searchError && (
          <p className="text-xs text-red-400 mt-1">{searchError}</p>
        )}
      </div>
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
        <Button
          onClick={() => router.back()}
          variant="ghost"
          className="text-gray-300 hover:text-indigo-400"
        >
          <ArrowLeft className="w-4 h-4 mr-2" />
          뒤로가기
        </Button>
      </nav>
    </header>
  );
}
