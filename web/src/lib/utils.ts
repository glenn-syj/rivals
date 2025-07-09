import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";
import { AxiosError } from "axios";
import { ProblemDetail, BackendError } from "./types";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export function handleAxiosError(
  error: AxiosError<ProblemDetail | any>
): BackendError {
  if (error.response) {
    const problemDetail = error.response.data as ProblemDetail;
    const status = error.response.status;

    let message = "알 수 없는 오류가 발생했습니다.";

    if (problemDetail && problemDetail.detail) {
      message = problemDetail.detail;
    } else if (problemDetail && problemDetail.title) {
      message = problemDetail.title;
    } else if (error.message) {
      message = error.message;
    }

    return {
      message: message,
      status: status,
      problemDetail: problemDetail,
    };
  } else if (error.request) {
    // 요청이 이루어졌으나 응답을 받지 못한 경우
    return {
      message:
        "서버로부터 응답을 받지 못했습니다. 네트워크 연결을 확인해주세요.",
      status: undefined,
    };
  } else {
    // 요청을 설정하는 중에 발생한 오류
    return {
      message: `요청을 보내는 중 오류가 발생했습니다: ${error.message}`,
      status: undefined,
    };
  }
}
