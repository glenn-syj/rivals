"use client";

import React, { useEffect } from "react";
import { toast } from "sonner"; // sonner 라이브러리의 toast 함수를 직접 임포트
import { useErrorStore } from "@/store/errorStore";

export function ErrorToastDisplay() {
  const { error, setError } = useErrorStore();

  useEffect(() => {
    if (error) {
      toast.error(`Error: ${error.status || "Unknown"}`, {
        description: error.message,
        // sonner는 variant 대신 type이나 style 등으로 메시지 형태를 제어합니다.
        // 여기서는 기본적으로 error 타입으로 표시합니다.
      });
      // 에러 메시지를 표시한 후 상태 초기화
      setError(null);
    }
  }, [error, setError]);

  return null; // 이 컴포넌트는 UI를 직접 렌더링하지 않고 토스트만 관리합니다.
}
