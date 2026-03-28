// src/commands/long_press.ts
import { ToolDefinition, CliOptions, ErrorCodes } from '../types';
import { selectDevice, execAdb, success, failure } from '../utils';

export const definition: ToolDefinition = {
  name: 'long_press',
  description: 'Perform a long press at the specified coordinates',
  parameters: [
    { name: 'x', type: 'number', required: true, description: 'X coordinate' },
    { name: 'y', type: 'number', required: true, description: 'Y coordinate' },
    { name: 'duration', type: 'number', required: false, description: 'Duration in ms', default: 500 }
  ],
  adbCommand: 'shell input swipe <x> <y> <x> <y> <duration>',
  async execute(x: number, y: number, duration?: number, options?: CliOptions): Promise<string> {
    try {
      const device = await selectDevice(options?.device);
      const dur = duration ?? 500;
      // Long press is implemented as a swipe from (x,y) to (x,y) with duration
      const result = await execAdb(
        ['shell', 'input', 'swipe', String(x), String(y), String(x), String(y), String(dur)],
        { deviceId: device, timeout: options?.timeout }
      );
      return result.exitCode === 0 ? success({}) : failure(ErrorCodes.TAP_FAILED, result.stderr || 'Long press failed');
    } catch (err: any) {
      return failure(ErrorCodes.DEVICE_NOT_FOUND, err.message);
    }
  }
};