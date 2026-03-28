// src/commands/install_app.ts
import { ToolDefinition, CliOptions, ErrorCodes } from '../types';
import { selectDevice, execAdb, success, failure } from '../utils';
import { existsSync } from 'fs';

export const definition: ToolDefinition = {
  name: 'install_app',
  description: 'Install an APK file on the device',
  parameters: [
    { name: 'apk_path', type: 'string', required: true, description: 'Local path to the APK file' },
    { name: 'reinstall', type: 'boolean', required: false, description: 'Reinstall keeping data', default: false },
    { name: 'grant_permissions', type: 'boolean', required: false, description: 'Grant all permissions on install', default: false }
  ],
  adbCommand: 'install [-r] [-g] <apk_path>',
  async execute(apkPath: string, reinstall?: boolean, grantPermissions?: boolean, options?: CliOptions): Promise<string> {
    try {
      // Check if APK file exists
      if (!existsSync(apkPath)) {
        return failure(ErrorCodes.FILE_NOT_FOUND, `APK file not found: ${apkPath}`);
      }

      const device = await selectDevice(options?.device);

      // Build install arguments
      const args = ['install'];

      if (reinstall) {
        args.push('-r'); // Reinstall keeping data
      }

      if (grantPermissions) {
        args.push('-g'); // Grant all permissions
      }

      args.push(apkPath);

      const result = await execAdb(args, {
        deviceId: device,
        timeout: options?.timeout ?? 120000 // Default 2 minutes for install
      });

      if (result.exitCode === 0 && result.stdout.includes('Success')) {
        return success({ installed: true, apk_path: apkPath });
      }

      // Parse error message
      const errorMsg = result.stderr || result.stdout || 'Failed to install app';
      if (errorMsg.includes('INSTALL_FAILED_ALREADY_EXISTS')) {
        return failure(ErrorCodes.EXEC_ERROR, 'App already installed. Use reinstall=true to replace.');
      }
      return failure(ErrorCodes.EXEC_ERROR, errorMsg);
    } catch (err: any) {
      return failure(ErrorCodes.DEVICE_NOT_FOUND, err.message);
    }
  }
};