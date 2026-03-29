// src/commands/click_node_by_text.ts
import { ToolDefinition, CliOptions, ErrorCodes } from '../types';
import { selectDevice, execAdb, success, failure, parseUiTree, findNodeByText } from '../utils';

export const definition: ToolDefinition = {
  name: 'click_node_by_text',
  description: 'Find a UI node by text and tap its center',
  parameters: [
    { name: 'text', type: 'string', required: true, description: 'Text to search for' },
    { name: 'exact', type: 'boolean', required: false, description: 'Exact match', default: true }
  ],
  adbCommand: 'uiautomator dump + find + tap',
  async execute(text: string, exact: boolean = true, options?: CliOptions): Promise<string> {
    try {
      const device = await selectDevice(options?.device);
      await execAdb(['shell', 'uiautomator', 'dump', '/sdcard/aat_ui.xml'], { deviceId: device });
      const catResult = await execAdb(['shell', 'cat', '/sdcard/aat_ui.xml'], { deviceId: device });
      if (catResult.exitCode !== 0) {
        return failure(ErrorCodes.UI_DUMP_FAILED, 'Failed to get UI tree');
      }
      const tree = parseUiTree(catResult.stdout);
      const node = findNodeByText(tree, text, exact);
      if (!node) {
        return failure(ErrorCodes.APP_NOT_FOUND, `Node with text "${text}" not found`);
      }
      const tapResult = await execAdb(
        ['shell', 'input', 'tap', String(node.centerX), String(node.centerY)],
        { deviceId: device }
      );
      if (tapResult.exitCode === 0) {
        return success({ text: node.text, bounds: node.bounds });
      }
      return failure(ErrorCodes.TAP_FAILED, tapResult.stderr);
    } catch (err: any) {
      return failure(ErrorCodes.DEVICE_NOT_FOUND, err.message);
    }
  }
};