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

      const result = await execAdb(args, { deviceId: device, timeout: options?.timeout || 30000 });

      if (result.exitCode === 0) {
        // The stdout contains the PNG binary data
        // We need to convert it to base64 for JSON transport
        const base64Data = result.stdout; // Already base64 encoded by execAdb for binary data

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