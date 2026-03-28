// src/output.ts
import { ErrorCode } from './types';

interface SuccessResponse {
  success: true;
  data: any;
}

interface FailureResponse {
  success: false;
  error: {
    code: ErrorCode;
    message: string;
  };
}

export function success(data: any): string {
  const response: SuccessResponse = {
    success: true,
    data
  };
  return JSON.stringify(response);
}

export function failure(code: ErrorCode, message: string): string {
  const response: FailureResponse = {
    success: false,
    error: {
      code,
      message
    }
  };
  return JSON.stringify(response);
}

export function prettyPrint(jsonString: string): string {
  const parsed = JSON.parse(jsonString);
  return JSON.stringify(parsed, null, 2);
}