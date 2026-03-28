// src/commands/drag.ts
import { ToolDefinition, CliOptions, ErrorCodes } from '../types';
import { selectDevice, execAdb, success, failure } from '../utils';

export const definition: ToolDefinition = {
  name: 'drag',
  description: 'Perform a drag from start to end coordinates',
  parameters: [
    { name: 'start_x', type: 'number', required: true, description: 'Start X coordinate' },
    { name: 'start_y', type: 'number', required: true, description: 'Start Y coordinate' },
    { name: 'end_x', type: 'number', required: true, description: 'End X coordinate' },
    { name: 'end_y', type: 'number', required: true, description: 'End Y coordinate' },
    { name: 'duration', type: 'number', required: false, description: 'Duration in ms', default: 300 }
  ],
  adbCommand: 'shell input drag <start_x> <start_y> <end_x> <end_y> <duration>',
  async execute(startX: number, startY: number, endX: number, endY: number, duration?: number, options?: CliOptions): Promise<string> {
    try {
      const device = await selectDevice(options?.device);
      const dur = duration ?? 300;
      const result = await execAdb(
        ['shell', 'input', 'drag', String(startX), String(startY), String(endX), String(endY), String(dur)],
        { deviceId: device, timeout: options?.timeout }
      );
      return result.exitCode === 0 ? success({}) : failure(ErrorCodes.TAP_FAILED, result.stderr || 'Drag failed');
    } catch (err: any) {
      return failure(ErrorCodes.DEVICE_NOT_FOUND, err.message);
    }
  }
};