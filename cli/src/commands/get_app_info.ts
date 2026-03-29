// src/commands/get_app_info.ts
import { ToolDefinition, CliOptions, ErrorCodes } from '../types';
import { selectDevice, execAdb, success, failure } from '../utils';

export const definition: ToolDefinition = {
  name: 'get_app_info',
  description: 'Get detailed information about an installed application',
  parameters: [
    { name: 'package_name', type: 'string', required: true, description: 'Package name of the application' }
  ],
  adbCommand: 'shell dumpsys package <package_name>',
  async execute(packageName: string, options?: CliOptions): Promise<string> {
    try {
      const device = await selectDevice(options?.device);
      const result = await execAdb(
        ['shell', 'dumpsys', 'package', packageName],
        { deviceId: device, timeout: options?.timeout }
      );

      if (result.exitCode === 0) {
        // Parse key information from dumpsys output
        const info = parsePackageInfo(result.stdout, packageName);
        return success(info);
      }
      return failure(ErrorCodes.EXEC_ERROR, result.stderr || 'Failed to get app info');
    } catch (err: any) {
      return failure(ErrorCodes.DEVICE_NOT_FOUND, err.message);
    }
  }
};

interface PackageInfo {
  package_name: string;
  version_name?: string;
  version_code?: string;
  data_dir?: string;
  target_sdk?: string;
  flags?: string[];
  permissions?: string[];
  main_activity?: string;
}

function parsePackageInfo(output: string, packageName: string): PackageInfo {
  const info: PackageInfo = { package_name: packageName };

  // Extract version info
  const versionNameMatch = output.match(/versionName=([^\s]+)/);
  if (versionNameMatch) info.version_name = versionNameMatch[1];

  const versionCodeMatch = output.match(/versionCode=(\d+)/);
  if (versionCodeMatch) info.version_code = versionCodeMatch[1];

  // Extract data directory
  const dataDirMatch = output.match(/dataDir=([^\s]+)/);
  if (dataDirMatch) info.data_dir = dataDirMatch[1];

  // Extract target SDK
  const targetSdkMatch = output.match(/targetSdk=([^\s]+)/);
  if (targetSdkMatch) info.target_sdk = targetSdkMatch[1];

  // Extract main activity
  const activityMatch = output.match(/android.intent.action.MAIN:\s*\n\s*\w+ ([^\s]+\/[^\s]+)/);
  if (activityMatch) info.main_activity = activityMatch[1];

  // Extract some permissions (limit to avoid too much data)
  const permissionMatches = output.matchAll(/permission\.([^\s,]+)/g);
  const permissions = new Set<string>();
  for (const match of permissionMatches) {
    if (permissions.size < 20) {
      permissions.add(match[1]);
    }
  }
  if (permissions.size > 0) {
    info.permissions = Array.from(permissions);
  }

  return info;
}