// src/adb.ts
import { spawn } from 'child_process';
import { AdbOptions, AdbResult } from './types';

export function buildAdbArgs(args: string[], deviceId?: string): string[] {
  if (deviceId) {
    return ['-s', deviceId, ...args];
  }
  return args;
}

export async function execAdb(args: string[], options?: AdbOptions): Promise<AdbResult> {
  const timeout = options?.timeout ?? 30000;
  const fullArgs = buildAdbArgs(args, options?.deviceId);

  return new Promise((resolve, reject) => {
    // 使用 spawn 安全执行，避免 shell 注入
    const proc = spawn('adb', fullArgs);
    let stdout = '';
    let stderr = '';
    let killed = false;

    const timer = setTimeout(() => {
      killed = true;
      proc.kill();
      reject(new Error(`Timeout after ${timeout}ms`));
    }, timeout);

    proc.stdout.on('data', (data) => {
      stdout += data.toString();
    });

    proc.stderr.on('data', (data) => {
      stderr += data.toString();
    });

    proc.on('close', (code) => {
      clearTimeout(timer);
      if (!killed) {
        resolve({
          exitCode: code ?? 0,
          stdout,
          stderr
        });
      }
    });

    proc.on('error', (err) => {
      clearTimeout(timer);
      if (!killed) {
        reject(err);
      }
    });
  });
}