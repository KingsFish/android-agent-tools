// src/commands/press_key.ts
import { ToolDefinition, CliOptions, ErrorCodes } from '../types';
import { selectDevice, execAdb, success, failure } from '../utils';

export const definition: ToolDefinition = {
  name: 'press_key',
  description: 'Press a key by keycode or name',
  parameters: [
    { name: 'key', type: 'string', required: true, description: 'Key code or name (e.g., 4, KEYCODE_BACK, back)' }
  ],
  adbCommand: 'shell input keyevent <key>',
  async execute(key: string, options?: CliOptions): Promise<string> {
    try {
      const device = await selectDevice(options?.device);
      const result = await execAdb(['shell', 'input', 'keyevent', key], { deviceId: device, timeout: options?.timeout });
      return result.exitCode === 0 ? success({}) : failure(ErrorCodes.TAP_FAILED, result.stderr || 'Key press failed');
    } catch (err: any) {
      return failure(ErrorCodes.DEVICE_NOT_FOUND, err.message);
    }
  }
};