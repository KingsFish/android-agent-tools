// src/commands/tap.ts
import { ToolDefinition, CliOptions, ErrorCodes } from '../types';
import { selectDevice, execAdb, success, failure } from '../utils';

export const definition: ToolDefinition = {
  name: 'tap',
  description: 'Perform a tap at the specified coordinates',
  parameters: [
    { name: 'x', type: 'number', required: true, description: 'X coordinate' },
    { name: 'y', type: 'number', required: true, description: 'Y coordinate' }
  ],
  adbCommand: 'shell input tap <x> <y>',
  async execute(x: number, y: number, options: CliOptions): Promise<string> {
    try {
      const device = await selectDevice(options.device);
      const result = await execAdb(
        ['shell', 'input', 'tap', String(x), String(y)],
        { deviceId: device, timeout: options.timeout }
      );

      if (result.exitCode === 0) {
        return success({});
      } else {
        return failure(ErrorCodes.TAP_FAILED, result.stderr || 'Tap command failed');
      }
    } catch (err: any) {
      return failure(ErrorCodes.DEVICE_NOT_FOUND, err.message);
    }
  }
};