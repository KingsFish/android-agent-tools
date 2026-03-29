// src/commands/get_ui_tree.ts
import { ToolDefinition, CliOptions, ErrorCodes } from '../types';
import { selectDevice, execAdb, success, failure, parseUiTree } from '../utils';

export const definition: ToolDefinition = {
  name: 'get_ui_tree',
  description: 'Get the UI hierarchy tree of the current screen',
  parameters: [],
  adbCommand: 'shell uiautomator dump && shell cat',
  async execute(options?: CliOptions): Promise<string> {
    try {
      const device = await selectDevice(options?.device);
      await execAdb(['shell', 'uiautomator', 'dump', '/sdcard/aat_ui.xml'], { deviceId: device, timeout: 10000 });
      const catResult = await execAdb(['shell', 'cat', '/sdcard/aat_ui.xml'], { deviceId: device });
      if (catResult.exitCode !== 0) {
        return failure(ErrorCodes.UI_DUMP_FAILED, 'Failed to read UI dump');
      }
      const tree = parseUiTree(catResult.stdout);
      return success(tree);
    } catch (err: any) {
      return failure(ErrorCodes.DEVICE_NOT_FOUND, err.message);
    }
  }
};