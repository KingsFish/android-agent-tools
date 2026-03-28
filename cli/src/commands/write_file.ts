// src/commands/write_file.ts
import { ToolDefinition, CliOptions, ErrorCodes } from '../types';
import { selectDevice, execAdb, success, failure } from '../utils';

export const definition: ToolDefinition = {
  name: 'write_file',
  description: 'Write content to a file on the device',
  parameters: [
    { name: 'path', type: 'string', required: true, description: 'Absolute path to the file' },
    { name: 'content', type: 'string', required: true, description: 'Content to write to the file' }
  ],
  adbCommand: 'shell echo "<content>" > <path>',
  async execute(path: string, content: string, options?: CliOptions): Promise<string> {
    try {
      const device = await selectDevice(options?.device);

      // Escape special characters for shell
      const escapedContent = content
        .replace(/\\/g, '\\\\')
        .replace(/'/g, "'\\''");

      // Use printf for more reliable writing (handles special characters better)
      const result = await execAdb(
        ['shell', 'printf', '%s', escapedContent, '>', path],
        { deviceId: device, timeout: options?.timeout }
      );

      // shell printf doesn't set exit code reliably, so we verify by checking file exists
      const verifyResult = await execAdb(
        ['shell', 'test', '-f', path, '&&', 'echo', 'exists'],
        { deviceId: device, timeout: options?.timeout }
      );

      if (verifyResult.stdout.includes('exists')) {
        return success({ path, bytesWritten: Buffer.byteLength(content) });
      }

      return failure(ErrorCodes.WRITE_ERROR, `Failed to write to file: ${path}`);
    } catch (err: any) {
      return failure(ErrorCodes.DEVICE_NOT_FOUND, err.message);
    }
  }
};