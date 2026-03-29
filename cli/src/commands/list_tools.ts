// src/commands/list_tools.ts
import { ToolDefinition, CliOptions } from '../types';
import { success } from '../utils';

export const definition: ToolDefinition = {
  name: 'list_tools',
  description: 'List all available tools and their status',
  parameters: [],
  async execute(options?: CliOptions): Promise<string> {
    const supported = [
      'tap', 'swipe', 'long_press', 'drag', 'press_key', 'press_back', 'press_home', 'press_recents',
      'input_text', 'read_file', 'write_file', 'list_directory', 'delete_file', 'file_exists',
      'list_apps', 'get_app_info', 'launch_app', 'install_app', 'uninstall_app', 'force_stop_app',
      'get_device_info', 'get_battery_status', 'get_ui_tree', 'take_screenshot',
      'get_current_app', 'is_app_running', 'click_node_by_text', 'click_node_by_id',
      'wait_for_element', 'list_devices', 'list_tools'
    ];
    const unsupported = ['get_clipboard', 'set_clipboard'];
    const limited = ['wait_for_element', 'wait_for_ui_stable'];
    return success({ tools: supported, unsupported, limited, total: supported.length });
  }
};