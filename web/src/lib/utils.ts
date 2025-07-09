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
  let userFriendlyMessage: string = "An unexpected error occurred.";
  let status: number | undefined = undefined;
  let problemDetail: ProblemDetail | undefined = undefined;

  if (error.response) {
    problemDetail = error.response.data as ProblemDetail;
    status = error.response.status;

    if (problemDetail && problemDetail.detail) {
      // Prioritize backend detail message if available and user-friendly
      userFriendlyMessage = problemDetail.detail;
    } else if (problemDetail && problemDetail.title) {
      // Fallback to title if detail is not available
      userFriendlyMessage = problemDetail.title;
    } else if (status && status >= 500) {
      // Generic message for server errors
      userFriendlyMessage =
        "Our server is experiencing issues. Please try again later.";
    } else if (error.message) {
      // Fallback to axios error message
      userFriendlyMessage = error.message;
    }
  } else if (error.request) {
    // The request was made but no response was received (e.g., network error)
    userFriendlyMessage =
      "Network error: Could not connect to the server. Please check your internet connection.";
  } else {
    // Something happened in setting up the request that triggered an Error
    userFriendlyMessage = `An error occurred while setting up the request: ${error.message}`;
  }

  return {
    message: userFriendlyMessage,
    status: status,
    problemDetail: problemDetail,
  };
}
