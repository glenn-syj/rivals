"use client";

import { useState, useEffect, useMemo } from "react";
import { useParams, useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import { Users, BarChart3 } from "lucide-react";
import TeamSelectionModal from "@/components/TeamSelectionModal";
import { findRiotAccount, renewRiotAccount } from "@/lib/api";
import type { RiotAccountDto } from "@/lib/types";
import { SummonerHeader } from "./_components/SummonerHeader";
import { TftStatusCard } from "./_components/TftStatusCard";
import { TftMatchHistory } from "./_components/TftMatchHistory";
import {
  Card,
  CardDescription,
  CardHeader,
  CardTitle,
  CardContent,
} from "@/components/ui/card";
import { ArrowLeft } from "lucide-react";
import { useSummonerPageLoadStore } from "@/store/summonerPageStore";

const QUEUE_TYPES = [
  "RANKED_TFT",
  "RANKED_TFT_DOUBLE_UP",
  "RANKED_TFT_TURBO",
] as const;

const formatQueueType = (queueType: string): string => {
  switch (queueType) {
    case "RANKED_TFT":
      return "일반 랭크";
    case "RANKED_TFT_TURBO":
      return "하이퍼롤";
    case "RANKED_TFT_DOUBLE_UP":
      return "더블업";
    default:
      return queueType;
  }
};

const decodeNameAndTag = (encodedName: string): [string, string] => {
  const decodedName = decodeURIComponent(encodedName);
  const [gameName, tagLine] = decodedName.split("#");
  const trimmedGameName = gameName.replace(
    /^[\s\u00A0\u1680\u2000-\u200A\u2028\u2029\u202F\u205F\u3000\uFEFF]+|[\s\u00A0\u1680\u2000-\u200A\u2028\u2029\u202F\u205F\u3000\uFEFF]+$/g,
    ""
  );
  const trimmedTagLine = tagLine.replace(/^[\s\u00A0]+|[\s\u00A0]+$/g, "");
  return [trimmedGameName, trimmedTagLine];
};

export default function SummonerPage() {
  const params = useParams();
  const router = useRouter();
  const encodedName = params!.encodedName as string;

  const {
    accountStatus,
    isAllDataLoaded,
    setAccountStatus,
    resetLoadStatus,
    isInitialRefreshPending,
    setIsInitialRefreshPending,
  } = useSummonerPageLoadStore();

  const [error, setError] = useState<string | null>(null);
  const [account, setAccount] = useState<RiotAccountDto | null>(null);
  const [selectedQueueType, setSelectedQueueType] =
    useState<string>("RANKED_TFT");
  const [selectedPlayer, setSelectedPlayer] = useState<RiotAccountDto | null>(
    null
  );
  const [isTeamModalOpen, setIsTeamModalOpen] = useState(false);
  const [isRefreshing, setIsRefreshing] = useState(false);

  const isLoading = useMemo(
    () => accountStatus === "loading" || accountStatus === "idle",
    [accountStatus]
  );

  useEffect(() => {
    const fetchAccountData = async () => {
      try {
        setAccountStatus("loading");
        const [decodedGameName, decodedTagLine] = decodeNameAndTag(encodedName);
        const accountData = await findRiotAccount(
          decodedGameName,
          decodedTagLine
        );
        setAccount(accountData);

        if (!accountData.updatedAt) {
          setIsInitialRefreshPending(true);
        }
        setAccountStatus("success");
      } catch (error) {
        console.error("Error fetching account data:", error);
        setError(
          error instanceof Error ? error.message : "Failed to load account data"
        );
        setAccountStatus("error");
      }
    };

    fetchAccountData();

    return () => {
      resetLoadStatus();
    };
  }, [
    encodedName,
    setAccountStatus,
    resetLoadStatus,
    setIsInitialRefreshPending,
  ]);

  useEffect(() => {
    const renewDataIfNeeded = async () => {
      if (isAllDataLoaded() && isInitialRefreshPending && account) {
        try {
          setIsRefreshing(true);
          const renewedAccount = await renewRiotAccount(
            account.gameName,
            account.tagLine
          );
          setAccount(renewedAccount);
        } catch (e) {
          console.error("Failed to refresh account on initial load", e);
          setError("Failed to refresh account on initial load");
        } finally {
          setIsRefreshing(false);
          setIsInitialRefreshPending(false);
        }
      }
    };
    renewDataIfNeeded();
  }, [
    isAllDataLoaded,
    isInitialRefreshPending,
    account,
    setIsInitialRefreshPending,
  ]);

  const handleRefresh = async () => {
    if (!account || isRefreshing) return;
    try {
      setIsRefreshing(true);
      resetLoadStatus();
      const renewedAccount = await renewRiotAccount(
        account.gameName,
        account.tagLine
      );
      setAccount(renewedAccount);
      setAccountStatus("success");
    } catch (e) {
      console.error("Failed to refresh account", e);
      setAccountStatus("error");
    } finally {
      setIsRefreshing(false);
    }
  };

  const handleAddToRivalry = () => {
    if (account) {
      setSelectedPlayer(account);
      setIsTeamModalOpen(true);
    }
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 flex items-center justify-center">
        <div className="text-center">
          <div className="w-16 h-16 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin mx-auto mb-4" />
          <p className="text-white text-lg">소환사 정보를 확인하는 중...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 flex items-center justify-center">
        <div className="text-center max-w-md">
          <Card className="bg-red-900/20 border-red-700/50">
            <CardHeader>
              <CardTitle className="text-red-400 flex items-center justify-center">
                오류 발생
              </CardTitle>
              <CardDescription className="text-red-300">
                {error}
              </CardDescription>
              <Button
                onClick={() => router.push("/")}
                variant="outline"
                className="mt-4 border-red-700 text-red-300 hover:bg-red-900/50"
              >
                <ArrowLeft className="w-4 h-4 mr-2" />
                홈으로 돌아가기
              </Button>
            </CardHeader>
          </Card>
        </div>
      </div>
    );
  }

  if (!account) {
    return null; // Should be handled by isLoading or error state
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900">
      <SummonerHeader />

      <main className="container mx-auto px-4 py-8">
        <div className="text-center mb-8">
          <h1 className="text-4xl font-bold text-white mb-2">
            {account.gameName}#{account.tagLine}
          </h1>
          <p className="text-gray-400">
            최종 갱신: {account.updatedAt || "Now Loading..."}
          </p>
        </div>

        <div className="flex gap-2 mb-4">
          {QUEUE_TYPES.map((queueType) => (
            <Button
              key={queueType}
              onClick={() => setSelectedQueueType(queueType)}
              variant={selectedQueueType === queueType ? "default" : "outline"}
              className={
                selectedQueueType === queueType
                  ? "bg-gradient-to-r from-indigo-600 to-indigo-800 hover:from-indigo-700 hover:to-indigo-900 text-white shadow-lg shadow-indigo-500/20"
                  : "border-slate-600 bg-slate-800 text-white hover:bg-indigo-200 hover:border-indigo-500/50 transition-all"
              }
            >
              {formatQueueType(queueType)}
            </Button>
          ))}
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-4 gap-8 min-h-[calc(100vh-300px)]">
          <div className="lg:col-span-1 space-y-4 lg:sticky lg:top-24 lg:h-fit">
            <TftStatusCard
              account={account}
              selectedQueueType={selectedQueueType}
              formatQueueType={formatQueueType}
            />

            <Card className="bg-slate-800/50 border-slate-700/50 hover:border-purple-500/50 transition-colors">
              <CardHeader>
                <CardTitle className="text-lg text-white flex items-center gap-2">
                  <div className="w-8 h-8 bg-gradient-to-br from-purple-500 to-indigo-500 rounded-lg flex items-center justify-center">
                    <BarChart3 className="w-5 h-5 text-white" />
                  </div>
                  Match 통계
                </CardTitle>
                <CardDescription className="text-gray-400">
                  게임별 상세 분석
                </CardDescription>
              </CardHeader>
              <CardContent>
                <div className="text-center py-4 text-gray-400">
                  <BarChart3 className="w-6 h-6 mx-auto mb-2 opacity-50" />
                  <p className="text-sm">Match 데이터 수집 중...</p>
                  <p className="text-xs mt-1">
                    게임 기록 분석이 여기에 표시됩니다
                  </p>
                </div>
              </CardContent>
            </Card>

            <div className="space-y-3">
              <Button
                onClick={handleAddToRivalry}
                className="w-full bg-gradient-to-r from-slate-600 to-indigo-600 hover:from-slate-700 hover:to-indigo-700"
              >
                <Users className="w-4 h-4 mr-2" />
                라이벌리에 추가하기
              </Button>
              <Button
                variant="outline"
                className="w-full border-slate-600 text-slate-300 hover:bg-slate-800/50"
                onClick={handleRefresh}
                disabled={isRefreshing || !isAllDataLoaded()}
              >
                {isRefreshing ? "갱신 중..." : "갱신하기"}
              </Button>
            </div>
          </div>

          <div className="lg:col-span-3 space-y-6">
            <TftMatchHistory account={account} />
          </div>
        </div>
      </main>

      <TeamSelectionModal
        player={selectedPlayer}
        isOpen={isTeamModalOpen}
        onClose={() => {
          setIsTeamModalOpen(false);
          setSelectedPlayer(null);
        }}
      />
    </div>
  );
}
