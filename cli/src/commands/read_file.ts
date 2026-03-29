// src/commands/read_file.ts
import { ToolDefinition, CliOptions, ErrorCodes } from '../types';
import { selectDevice, execAdb, success, failure } from '../utils';

export const definition: ToolDefinition = {
  name: 'read_file',
  description: 'Read the content of a file on the device',
  parameters: [
    { name: 'path', type: 'string', required: true, description: 'Absolute path to the file' }
  ],
  adbCommand: 'shell cat <path>',
  async execute(path: string, options?: CliOptions): Promise<string> {
    try {
      const device = await selectDevice(options?.device);
      const result = await execAdb(['shell', 'cat', path], { deviceId: device, timeout: options?.timeout });
      if (result.exitCode === 0) {
        return success({ content: result.stdout, size: Buffer.byteLength(result.stdout) });
      }
      return failure(ErrorCodes.FILE_NOT_FOUND, `File does not exist: ${path}`);
    } catch (err: any) {
      return failure(ErrorCodes.DEVICE_NOT_FOUND, err.message);
    }
  }
};