// src/types.ts

export interface Parameter {
  name: string;
  type: 'string' | 'number' | 'boolean';
  required: boolean;
  description?: string;
  default?: string | number | boolean;
}

export interface CliOptions {
  device?: string;
  timeout?: number;
  pretty?: boolean;
}

export interface ToolDefinition {
  name: string;
  description: string;
  parameters: Parameter[];
  adbCommand?: string;
  execute: (...args: any[]) => Promise<string>;
}

export interface AdbOptions {
  deviceId?: string;
  timeout?: number;
}

export interface AdbResult {
  exitCode: number;
  stdout: string;
  stderr: string;
}

export interface Device {
  id: string;
  status: 'device' | 'offline' | 'unauthorized' | 'unknown';
}

export const ErrorCodes = {
  PERMISSION_DENIED: 'PERMISSION_DENIED',
  INVALID_PARAMETER: 'INVALID_PARAMETER',
  UNSUPPORTED_OPERATION: 'UNSUPPORTED_OPERATION',
  DEVICE_NOT_FOUND: 'DEVICE_NOT_FOUND',
  FILE_NOT_FOUND: 'FILE_NOT_FOUND',
  READ_ERROR: 'READ_ERROR',
  WRITE_ERROR: 'WRITE_ERROR',
  APP_NOT_FOUND: 'APP_NOT_FOUND',
  LAUNCH_FAILED: 'LAUNCH_FAILED',
  UI_DUMP_FAILED: 'UI_DUMP_FAILED',
  TAP_FAILED: 'TAP_FAILED',
  TIMEOUT: 'TIMEOUT',
  EXEC_ERROR: 'EXEC_ERROR',
} as const;

export type ErrorCode = typeof ErrorCodes[keyof typeof ErrorCodes];