// src/commands/press_back.ts
import { ToolDefinition, CliOptions, ErrorCodes } from '../types';
import { selectDevice, execAdb, success, failure } from '../utils';

export const definition: ToolDefinition = {
  name: 'press_back',
  description: 'Press the back button',
  parameters: [],
  adbCommand: 'shell input keyevent 4',
  async execute(options?: CliOptions): Promise<string> {
    try {
      const device = await selectDevice(options?.device);
      const result = await execAdb(['shell', 'input', 'keyevent', '4'], { deviceId: device, timeout: options?.timeout });
      return result.exitCode === 0 ? success({}) : failure(ErrorCodes.TAP_FAILED, result.stderr || 'Back press failed');
    } catch (err: any) {
      return failure(ErrorCodes.DEVICE_NOT_FOUND, err.message);
    }
  }
};