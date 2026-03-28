// src/commands/wait_for_element.ts
import { ToolDefinition, CliOptions, ErrorCodes } from '../types';
import { selectDevice, execAdb, success, failure, parseUiTree, findNodeByText, findNodeById } from '../utils';

export const definition: ToolDefinition = {
  name: 'wait_for_element',
  description: 'Wait for a UI element to appear by text or resource-id',
  parameters: [
    { name: 'selector', type: 'string', required: true, description: 'Text or resource-id to search for' },
    { name: 'timeout', type: 'number', required: false, description: 'Timeout in seconds (default: 30)', default: 30 },
    { name: 'by', type: 'string', required: false, description: 'Search by "text" or "id" (default: text)', default: 'text' }
  ],
  adbCommand: 'uiautomator dump + find (polling)',
  async execute(selector: string, timeout: number = 30, by: string = 'text', options?: CliOptions): Promise<string> {
    try {
      const device = await selectDevice(options?.device);
      const startTime = Date.now();
      const timeoutMs = timeout * 1000;
      const pollInterval = 1000; // 1 second

      while (Date.now() - startTime < timeoutMs) {
        await execAdb(['shell', 'uiautomator', 'dump', '/sdcard/aat_ui.xml'], { deviceId: device });
        const catResult = await execAdb(['shell', 'cat', '/sdcard/aat_ui.xml'], { deviceId: device });

        if (catResult.exitCode === 0) {
          const tree = parseUiTree(catResult.stdout);
          const node = by === 'id' ? findNodeById(tree, selector) : findNodeByText(tree, selector, true);

          if (node) {
            return success({
              found: true,
              selector,
              by,
              text: node.text,
              resourceId: node.resourceId,
              bounds: node.bounds
            });
          }
        }

        // Wait before next poll
        await new Promise(resolve => setTimeout(resolve, pollInterval));
      }

      return failure(ErrorCodes.TIMEOUT, `Element "${selector}" not found within ${timeout} seconds`);
    } catch (err: any) {
      return failure(ErrorCodes.DEVICE_NOT_FOUND, err.message);
    }
  }
};