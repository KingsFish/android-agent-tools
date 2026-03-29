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
  const isBinary = options?.binary ?? false;

  return new Promise((resolve, reject) => {
    // 使用 spawn 安全执行，避免 shell 注入
    const proc = spawn('adb', fullArgs);
    const stdoutChunks: Buffer[] = [];
    const stderrChunks: Buffer[] = [];
    let killed = false;

    const timer = setTimeout(() => {
      killed = true;
      proc.kill();
      reject(new Error(`Timeout after ${timeout}ms`));
    }, timeout);

    proc.stdout.on('data', (data) => {
      stdoutChunks.push(Buffer.isBuffer(data) ? data : Buffer.from(data));
    });

    proc.stderr.on('data', (data) => {
      stderrChunks.push(Buffer.isBuffer(data) ? data : Buffer.from(data));
    });

    proc.on('close', (code) => {
      clearTimeout(timer);
      if (!killed) {
        const stdoutBuffer = Buffer.concat(stdoutChunks);
        resolve({
          exitCode: code ?? 0,
          stdout: isBinary ? stdoutBuffer.toString('base64') : stdoutBuffer.toString('utf-8'),
          stderr: Buffer.concat(stderrChunks).toString('utf-8')
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