// src/commands/delete_file.ts
import { ToolDefinition, CliOptions, ErrorCodes } from '../types';
import { selectDevice, execAdb, success, failure } from '../utils';

export const definition: ToolDefinition = {
  name: 'delete_file',
  description: 'Delete a file or directory on the device',
  parameters: [
    { name: 'path', type: 'string', required: true, description: 'Absolute path to the file or directory' },
    { name: 'recursive', type: 'boolean', required: false, description: 'Delete recursively (for directories)', default: false }
  ],
  adbCommand: 'shell rm [-r] <path>',
  async execute(path: string, recursive?: boolean, options?: CliOptions): Promise<string> {
    try {
      const device = await selectDevice(options?.device);

      // Build rm command
      const args = ['shell', 'rm'];
      if (recursive) {
        args.push('-r');
      }
      args.push(path);

      const result = await execAdb(args, { deviceId: device, timeout: options?.timeout });

      if (result.exitCode === 0) {
        return success({ path, deleted: true });
      }

      // Check if the error is because file doesn't exist
      if (result.stderr.includes('No such file or directory')) {
        return failure(ErrorCodes.FILE_NOT_FOUND, `File does not exist: ${path}`);
      }

      return failure(ErrorCodes.WRITE_ERROR, `Failed to delete: ${result.stderr || 'Unknown error'}`);
    } catch (err: any) {
      return failure(ErrorCodes.DEVICE_NOT_FOUND, err.message);
    }
  }
};