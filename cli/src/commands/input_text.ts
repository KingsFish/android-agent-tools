// src/commands/input_text.ts
import { ToolDefinition, CliOptions, ErrorCodes } from '../types';
import { selectDevice, execAdb, success, failure } from '../utils';

export const definition: ToolDefinition = {
  name: 'input_text',
  description: 'Input text into the current focused field',
  parameters: [
    { name: 'text', type: 'string', required: true, description: 'Text to input' }
  ],
  adbCommand: 'shell input text <text>',
  async execute(text: string, options?: CliOptions): Promise<string> {
    try {
      const device = await selectDevice(options?.device);
      const result = await execAdb(['shell', 'input', 'text', text], { deviceId: device, timeout: options?.timeout });
      return result.exitCode === 0 ? success({}) : failure(ErrorCodes.TAP_FAILED, result.stderr || 'Text input failed');
    } catch (err: any) {
      return failure(ErrorCodes.DEVICE_NOT_FOUND, err.message);
    }
  }
};