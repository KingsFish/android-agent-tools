// src/commands/take_screenshot.ts
import { ToolDefinition, CliOptions, ErrorCodes } from '../types';
import { selectDevice, execAdb, success, failure } from '../utils';

export const definition: ToolDefinition = {
  name: 'take_screenshot',
  description: 'Take a screenshot from the device. Returns base64 encoded PNG image.',
  parameters: [
    { name: 'display', type: 'number', required: false, description: 'Display ID to capture (default: all displays)', default: -1 }
  ],
  adbCommand: 'shell screencap -p',
  async execute(display?: number, options?: CliOptions): Promise<string> {
    try {
      const device = await selectDevice(options?.device);

      // Build screencap command args
      const args = ['shell', 'screencap', '-p'];

      // Add display ID if specified (for multi-display devices)
      if (typeof display === 'number' && display >= 0) {
        args.push('-d', display.toString());
      }

      const result = await execAdb(args, { deviceId: device, timeout: options?.timeout || 30000, binary: true });

      if (result.exitCode === 0) {
        // stdout is base64 encoded PNG data (binary mode enabled)
        const base64Data = result.stdout;

        return success({
          format: 'png',
          data: base64Data,
          message: 'Screenshot captured successfully'
        });
      }
      return failure(ErrorCodes.EXEC_ERROR, result.stderr || 'Failed to take screenshot');
    } catch (err: any) {
      return failure(ErrorCodes.DEVICE_NOT_FOUND, err.message);
    }
  }
};