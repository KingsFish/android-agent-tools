# ADB CLI 工具命令集实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 创建 Node.js CLI 工具 `aat`，让远程 Agent 通过 ADB 命令操控 Android 设备。

**Architecture:** CLI 自动扫描 commands/ 目录注册工具，每个工具独立文件，使用 spawn 执行 ADB 命令并返回统一 JSON 格式。

**Tech Stack:** TypeScript, Commander, Node.js spawn (安全执行)

---

## Task 1: 项目初始化

**Files:**
- Create: `cli/package.json`
- Create: `cli/tsconfig.json`
- Create: `cli/bin/aat`
- Create: `cli/.gitignore`

- [ ] **Step 1: 创建 package.json**

```json
{
  "name": "android-agent-tools-cli",
  "version": "1.0.0",
  "description": "CLI tools for LLM agents to control Android devices via ADB",
  "main": "dist/index.js",
  "bin": {
    "aat": "./bin/aat"
  },
  "scripts": {
    "build": "tsc",
    "test": "jest",
    "dev": "ts-node src/index.ts"
  },
  "dependencies": {
    "commander": "^12.0.0"
  },
  "devDependencies": {
    "@types/node": "^20.0.0",
    "typescript": "^5.0.0",
    "jest": "^29.0.0",
    "@types/jest": "^29.0.0",
    "ts-jest": "^29.0.0"
  },
  "license": "MIT"
}
```

- [ ] **Step 2: 创建 tsconfig.json**

```json
{
  "compilerOptions": {
    "target": "ES2020",
    "module": "commonjs",
    "lib": ["ES2020"],
    "outDir": "./dist",
    "rootDir": "./src",
    "strict": true,
    "esModuleInterop": true,
    "skipLibCheck": true,
    "forceConsistentCasingInFileNames": true,
    "resolveJsonModule": true,
    "declaration": true,
    "declarationMap": true,
    "sourceMap": true
  },
  "include": ["src/**/*"],
  "exclude": ["node_modules", "dist", "tests"]
}
```

- [ ] **Step 3: 创建 bin/aat**

```bash
#!/usr/bin/env node
require('../dist/index.js');
```

- [ ] **Step 4: 创建 .gitignore**

```
node_modules/
dist/
*.log
.DS_Store
```

- [ ] **Step 5: 安装依赖**

Run: `cd cli && npm install`
Expected: 依赖安装成功，生成 node_modules 目录

- [ ] **Step 6: Commit**

```bash
git add cli/
git commit -m "feat: initialize CLI project structure"
```

---

## Task 2: Jest 配置

**Files:**
- Create: `cli/jest.config.js`

- [ ] **Step 1: 创建 jest.config.js**

```javascript
module.exports = {
  preset: 'ts-jest',
  testEnvironment: 'node',
  roots: ['<rootDir>/tests'],
  testMatch: ['**/*.test.ts'],
  moduleFileExtensions: ['ts', 'js'],
  collectCoverageFrom: ['src/**/*.ts'],
  coverageDirectory: 'coverage',
};
```

- [ ] **Step 2: Commit**

```bash
git add cli/jest.config.js
git commit -m "feat: add Jest configuration"
```

---

## Task 3: 类型定义

**Files:**
- Create: `cli/src/types.ts`
- Test: `cli/tests/types.test.ts`

- [ ] **Step 1: 写测试**

```typescript
// tests/types.test.ts
import { ToolDefinition, Parameter, CliOptions } from '../src/types';

describe('types', () => {
  it('ToolDefinition should have required fields', () => {
    const def: ToolDefinition = {
      name: 'test',
      description: 'Test tool',
      parameters: [],
      execute: async () => '{"success": true}'
    };
    expect(def.name).toBe('test');
    expect(def.description).toBe('Test tool');
  });

  it('Parameter should support all types', () => {
    const param: Parameter = {
      name: 'x',
      type: 'number',
      required: true,
      description: 'X coordinate'
    };
    expect(param.type).toBe('number');
  });

  it('CliOptions should have optional fields', () => {
    const opts: CliOptions = {};
    expect(opts.device).toBeUndefined();
    expect(opts.timeout).toBeUndefined();
  });
});
```

- [ ] **Step 2: 运行测试验证失败**

Run: `cd cli && npm test`
Expected: FAIL - 模块不存在

- [ ] **Step 3: 实现类型定义**

```typescript
// src/types.ts

export interface Parameter {
  name: string;
  type: 'string' | 'number' | 'boolean';
  required: boolean;
  description?: string;
  default?: string | number | boolean;
}

export interface CliOptions {
  device?: string;
  timeout?: number;
  pretty?: boolean;
}

export interface ToolDefinition {
  name: string;
  description: string;
  parameters: Parameter[];
  adbCommand?: string;
  execute: (...args: any[], options: CliOptions) => Promise<string>;
}

export interface AdbOptions {
  deviceId?: string;
  timeout?: number;
}

export interface AdbResult {
  exitCode: number;
  stdout: string;
  stderr: string;
}

export interface Device {
  id: string;
  status: 'device' | 'offline' | 'unauthorized' | 'unknown';
}

export const ErrorCodes = {
  PERMISSION_DENIED: 'PERMISSION_DENIED',
  INVALID_PARAMETER: 'INVALID_PARAMETER',
  UNSUPPORTED_OPERATION: 'UNSUPPORTED_OPERATION',
  DEVICE_NOT_FOUND: 'DEVICE_NOT_FOUND',
  FILE_NOT_FOUND: 'FILE_NOT_FOUND',
  READ_ERROR: 'READ_ERROR',
  WRITE_ERROR: 'WRITE_ERROR',
  APP_NOT_FOUND: 'APP_NOT_FOUND',
  LAUNCH_FAILED: 'LAUNCH_FAILED',
  UI_DUMP_FAILED: 'UI_DUMP_FAILED',
  TAP_FAILED: 'TAP_FAILED',
  TIMEOUT: 'TIMEOUT',
  EXEC_ERROR: 'EXEC_ERROR',
} as const;

export type ErrorCode = typeof ErrorCodes[keyof typeof ErrorCodes];
```

- [ ] **Step 4: 运行测试验证通过**

Run: `cd cli && npm test`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add cli/src/types.ts cli/tests/types.test.ts
git commit -m "feat: add type definitions"
```

---

## Task 4: JSON 输出模块

**Files:**
- Create: `cli/src/output.ts`
- Test: `cli/tests/output.test.ts`

- [ ] **Step 1: 写测试**

```typescript
// tests/output.test.ts
import { success, failure } from '../src/output';

describe('output', () => {
  it('success should return correct JSON', () => {
    const result = success({ content: 'hello', size: 5 });
    const parsed = JSON.parse(result);
    expect(parsed.success).toBe(true);
    expect(parsed.data.content).toBe('hello');
    expect(parsed.data.size).toBe(5);
  });

  it('success with empty data', () => {
    const result = success({});
    const parsed = JSON.parse(result);
    expect(parsed.success).toBe(true);
    expect(parsed.data).toEqual({});
  });

  it('failure should return correct JSON', () => {
    const result = failure('FILE_NOT_FOUND', 'File does not exist: /test.txt');
    const parsed = JSON.parse(result);
    expect(parsed.success).toBe(false);
    expect(parsed.error.code).toBe('FILE_NOT_FOUND');
    expect(parsed.error.message).toBe('File does not exist: /test.txt');
  });

  it('output should be valid JSON string', () => {
    const result = success({ x: 500 });
    expect(() => JSON.parse(result)).not.toThrow();
  });
});
```

- [ ] **Step 2: 运行测试验证失败**

Run: `cd cli && npm test`
Expected: FAIL - 模块不存在

- [ ] **Step 3: 实现输出模块**

```typescript
// src/output.ts
import { ErrorCode } from './types';

interface SuccessResponse {
  success: true;
  data: any;
}

interface FailureResponse {
  success: false;
  error: {
    code: ErrorCode;
    message: string;
  };
}

export function success(data: any): string {
  const response: SuccessResponse = {
    success: true,
    data
  };
  return JSON.stringify(response);
}

export function failure(code: ErrorCode, message: string): string {
  const response: FailureResponse = {
    success: false,
    error: {
      code,
      message
    }
  };
  return JSON.stringify(response);
}

export function prettyPrint(jsonString: string): string {
  const parsed = JSON.parse(jsonString);
  return JSON.stringify(parsed, null, 2);
}
```

- [ ] **Step 4: 运行测试验证通过**

Run: `cd cli && npm test`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add cli/src/output.ts cli/tests/output.test.ts
git commit -m "feat: add JSON output module"
```

---

## Task 5: ADB 执行模块（使用 spawn 安全执行）

**Files:**
- Create: `cli/src/adb.ts`
- Test: `cli/tests/adb.test.ts`

- [ ] **Step 1: 写测试**

```typescript
// tests/adb.test.ts
import { buildAdbArgs } from '../src/adb';

describe('adb', () => {
  it('buildAdbArgs should add device selector', () => {
    const args = buildAdbArgs(['shell', 'input', 'tap', '500', '800'], 'emulator-5554');
    expect(args).toEqual(['-s', 'emulator-5554', 'shell', 'input', 'tap', '500', '800']);
  });

  it('buildAdbArgs without device should not add selector', () => {
    const args = buildAdbArgs(['shell', 'input', 'tap', '500', '800']);
    expect(args).toEqual(['shell', 'input', 'tap', '500', '800']);
  });
});
```

- [ ] **Step 2: 运行测试验证失败**

Run: `cd cli && npm test`
Expected: FAIL - 模块不存在

- [ ] **Step 3: 实现 ADB 模块（使用 spawn 安全执行）**

```typescript
// src/adb.ts
import { spawn } from 'child_process';
import { AdbOptions, AdbResult } from './types';

export function buildAdbArgs(args: string[], deviceId?: string): string[] {
  if (deviceId) {
    return ['-s', deviceId, ...args];
  }
  return args;
}

export async function execAdb(args: string[], options?: AdbOptions): Promise<AdbResult> {
  const timeout = options?.timeout ?? 30000;
  const fullArgs = buildAdbArgs(args, options?.deviceId);

  return new Promise((resolve, reject) => {
    // 使用 spawn 安全执行，避免 shell 注入
    const proc = spawn('adb', fullArgs);
    let stdout = '';
    let stderr = '';
    let killed = false;

    const timer = setTimeout(() => {
      killed = true;
      proc.kill();
      reject(new Error(`Timeout after ${timeout}ms`));
    }, timeout);

    proc.stdout.on('data', (data) => {
      stdout += data.toString();
    });

    proc.stderr.on('data', (data) => {
      stderr += data.toString();
    });

    proc.on('close', (code) => {
      clearTimeout(timer);
      if (!killed) {
        resolve({
          exitCode: code ?? 0,
          stdout,
          stderr
        });
      }
    });

    proc.on('error', (err) => {
      clearTimeout(timer);
      if (!killed) {
        reject(err);
      }
    });
  });
}
```

- [ ] **Step 4: 运行测试验证通过**

Run: `cd cli && npm test`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add cli/src/adb.ts cli/tests/adb.test.ts
git commit -m "feat: add ADB execution module using spawn (safe)"
```

---

## Task 6: 设备检测模块

**Files:**
- Create: `cli/src/device.ts`
- Test: `cli/tests/device.test.ts`

- [ ] **Step 1: 写测试**

```typescript
// tests/device.test.ts
import { parseDeviceList } from '../src/device';
import { Device } from '../src/types';

describe('device', () => {
  describe('parseDeviceList', () => {
    it('should parse adb devices output', () => {
      const output = 'emulator-5554\tdevice\nABC123\tdevice\nXYZ789\toffline\n';
      const devices = parseDeviceList(output);
      expect(devices).toHaveLength(3);
      expect(devices[0].id).toBe('emulator-5554');
      expect(devices[0].status).toBe('device');
      expect(devices[2].status).toBe('offline');
    });

    it('should handle empty output', () => {
      const output = 'List of devices attached\n';
      const devices = parseDeviceList(output);
      expect(devices).toHaveLength(0);
    });

    it('should parse unauthorized devices', () => {
      const output = 'ABC123\tunauthorized\nDEF456\tdevice\n';
      const devices = parseDeviceList(output);
      expect(devices).toHaveLength(2);
      expect(devices[0].status).toBe('unauthorized');
    });
  });
});
```

- [ ] **Step 2: 运行测试验证失败**

Run: `cd cli && npm test`

- [ ] **Step 3: 实现设备模块**

```typescript
// src/device.ts
import { execAdb } from './adb';
import { Device } from './types';

export function parseDeviceList(output: string): Device[] {
  const lines = output.split('\n').filter(line => line.trim());
  const devices: Device[] = [];

  for (const line of lines) {
    if (line.startsWith('List of devices attached')) continue;

    const parts = line.split('\t');
    if (parts.length >= 2) {
      const id = parts[0].trim();
      const status = parts[1].trim() as Device['status'];
      devices.push({ id, status });
    }
  }

  return devices;
}

export async function listDevices(): Promise<Device[]> {
  const result = await execAdb(['devices']);
  return parseDeviceList(result.stdout);
}

export async function selectDevice(deviceId?: string): Promise<string> {
  if (deviceId) return deviceId;

  const envDevice = process.env.ADB_DEVICE;
  if (envDevice) return envDevice;

  const devices = await listDevices();

  if (devices.length === 0) {
    throw new Error('No devices connected. Connect a device via USB or ADB network.');
  }

  const availableDevices = devices.filter(d => d.status === 'device');

  if (availableDevices.length === 0) {
    throw new Error('No authorized devices. Check device authorization.');
  }

  if (availableDevices.length === 1) {
    return availableDevices[0].id;
  }

  const deviceIds = availableDevices.map(d => d.id).join(', ');
  throw new Error(`Multiple devices connected (${deviceIds}). Specify device with -d option or ADB_DEVICE env.`);
}
```

- [ ] **Step 4: 运行测试验证通过**

Run: `cd cli && npm test`

- [ ] **Step 5: Commit**

```bash
git add cli/src/device.ts cli/tests/device.test.ts
git commit -m "feat: add device detection module"
```

---

## Task 7: UI 树解析模块

**Files:**
- Create: `cli/src/parser.ts`
- Test: `cli/tests/parser.test.ts`

- [ ] **Step 1: 写测试**

```typescript
// tests/parser.test.ts
import { parseUiTree, findNodeByText, findNodeById } from '../src/parser';

describe('parser', () => {
  const sampleXml = `<node index="0" text="Hello" resource-id="com.app:id/title" bounds="[0,0][100,50]">
  <node index="1" text="Click Me" resource-id="com.app:id/button" bounds="[10,60][90,100]" clickable="true"/>
</node>`;

  it('parseUiTree should parse XML to JSON', () => {
    const tree = parseUiTree(sampleXml);
    expect(tree.text).toBe('Hello');
    expect(tree.resourceId).toBe('com.app:id/title');
    expect(tree.children).toHaveLength(1);
  });

  it('parseUiTree should extract bounds', () => {
    const tree = parseUiTree(sampleXml);
    expect(tree.bounds).toEqual({ left: 0, top: 0, right: 100, bottom: 50 });
  });

  it('findNodeByText should find matching node', () => {
    const tree = parseUiTree(sampleXml);
    const node = findNodeByText(tree, 'Click Me');
    expect(node?.text).toBe('Click Me');
    expect(node?.resourceId).toBe('com.app:id/button');
  });

  it('findNodeById should find matching node', () => {
    const tree = parseUiTree(sampleXml);
    const node = findNodeById(tree, 'com.app:id/button');
    expect(node?.resourceId).toBe('com.app:id/button');
  });
});
```

- [ ] **Step 2: 运行测试验证失败**

Run: `cd cli && npm test`

- [ ] **Step 3: 实现解析模块**

```typescript
// src/parser.ts

interface UiNode {
  index: number;
  text: string;
  resourceId: string;
  className: string;
  packageName: string;
  clickable: boolean;
  enabled: boolean;
  bounds: { left: number; top: number; right: number; bottom: number };
  children: UiNode[];
  centerX: number;
  centerY: number;
}

function parseAttributes(attrStr: string): Record<string, string> {
  const attrs: Record<string, string> = {};
  const regex = /(\w+)="([^"]*)"/g;
  let match;
  while ((match = regex.exec(attrStr)) !== null) {
    attrs[match[1]] = match[2];
  }
  return attrs;
}

function parseBounds(boundsStr: string): { left: number; top: number; right: number; bottom: number } {
  const match = boundsStr.match(/\[(\d+),(\d+)\]\[(\d+),(\d+)\]/);
  if (!match) return { left: 0, top: 0, right: 0, bottom: 0 };
  return {
    left: parseInt(match[1]),
    top: parseInt(match[2]),
    right: parseInt(match[3]),
    bottom: parseInt(match[4])
  };
}

export function parseUiTree(xml: string): UiNode {
  return parseNode(xml, 0);
}

function parseNode(xml: string, startPos: number): UiNode {
  const nodeStart = xml.indexOf('<node', startPos);
  if (nodeStart === -1) throw new Error('Node not found');

  const tagEnd = xml.indexOf('>', nodeStart);
  const attrStr = xml.slice(nodeStart + 5, tagEnd);
  const attrs = parseAttributes(attrStr);

  const bounds = parseBounds(attrs['bounds'] || '[0,0][0,0]');
  const centerX = (bounds.left + bounds.right) / 2;
  const centerY = (bounds.top + bounds.bottom) / 2;

  const node: UiNode = {
    index: parseInt(attrs['index'] || '0'),
    text: attrs['text'] || '',
    resourceId: attrs['resource-id'] || '',
    className: attrs['class'] || '',
    packageName: attrs['package'] || '',
    clickable: attrs['clickable'] === 'true',
    enabled: attrs['enabled'] === 'true',
    bounds,
    children: [],
    centerX,
    centerY
  };

  if (xml[tagEnd - 1] === '/') return node;

  let pos = tagEnd + 1;
  while (pos < xml.length) {
    const childStart = xml.indexOf('<node', pos);
    if (childStart === -1) break;

    const childEnd = xml.indexOf('</node>', childStart);
    const selfCloseEnd = xml.indexOf('/>', childStart);

    if (selfCloseEnd !== -1 && (childEnd === -1 || selfCloseEnd < childEnd)) {
      const childXml = xml.slice(childStart, selfCloseEnd + 2);
      node.children.push(parseNode(childXml, 0));
      pos = selfCloseEnd + 2;
    } else if (childEnd !== -1) {
      const childXml = xml.slice(childStart, childEnd + 8);
      node.children.push(parseNode(childXml, 0));
      pos = childEnd + 8;
    } else {
      break;
    }
  }

  return node;
}

export function findNodeByText(tree: UiNode, text: string, exact: boolean = true): UiNode | null {
  if (exact && tree.text === text) return tree;
  if (!exact && tree.text.includes(text)) return tree;

  for (const child of tree.children) {
    const found = findNodeByText(child, text, exact);
    if (found) return found;
  }
  return null;
}

export function findNodeById(tree: UiNode, resourceId: string): UiNode | null {
  if (tree.resourceId === resourceId) return tree;

  for (const child of tree.children) {
    const found = findNodeById(child, resourceId);
    if (found) return found;
  }
  return null;
}
```

- [ ] **Step 4: 运行测试验证通过**

Run: `cd cli && npm test`

- [ ] **Step 5: Commit**

```bash
git add cli/src/parser.ts cli/tests/parser.test.ts
git commit -m "feat: add UI tree parser module"
```

---

## Task 8: 工具导出聚合

**Files:**
- Create: `cli/src/utils.ts`

- [ ] **Step 1: 创建工具导出**

```typescript
// src/utils.ts
export { execAdb, buildAdbArgs } from './adb';
export { listDevices, selectDevice, parseDeviceList } from './device';
export { success, failure, prettyPrint } from './output';
export { parseUiTree, findNodeByText, findNodeById } from './parser';
export * from './types';
```

- [ ] **Step 2: Commit**

```bash
git add cli/src/utils.ts
git commit -m "feat: add utils export aggregator"
```

---

## Task 9: 基础命令 - tap

**Files:**
- Create: `cli/src/commands/tap.ts`
- Test: `cli/tests/commands/tap.test.ts`

- [ ] **Step 1: 写测试**

```typescript
// tests/commands/tap.test.ts
import { definition } from '../src/commands/tap';

describe('tap command', () => {
  it('should have correct definition', () => {
    expect(definition.name).toBe('tap');
    expect(definition.description).toContain('tap');
    expect(definition.parameters).toHaveLength(2);
    expect(definition.parameters[0].name).toBe('x');
    expect(definition.parameters[0].type).toBe('number');
    expect(definition.parameters[0].required).toBe(true);
  });
});
```

- [ ] **Step 2: 运行测试验证失败**

Run: `cd cli && npm test`

- [ ] **Step 3: 实现 tap 命令**

```typescript
// src/commands/tap.ts
import { ToolDefinition, CliOptions, ErrorCodes } from '../types';
import { selectDevice, execAdb, success, failure } from '../utils';

export const definition: ToolDefinition = {
  name: 'tap',
  description: 'Perform a tap at the specified coordinates',
  parameters: [
    { name: 'x', type: 'number', required: true, description: 'X coordinate' },
    { name: 'y', type: 'number', required: true, description: 'Y coordinate' }
  ],
  adbCommand: 'shell input tap <x> <y>',
  async execute(x: number, y: number, options: CliOptions): Promise<string> {
    try {
      const device = await selectDevice(options.device);
      const result = await execAdb(
        ['shell', 'input', 'tap', String(x), String(y)],
        { deviceId: device, timeout: options.timeout }
      );

      if (result.exitCode === 0) {
        return success({});
      } else {
        return failure(ErrorCodes.TAP_FAILED, result.stderr || 'Tap command failed');
      }
    } catch (err: any) {
      return failure(ErrorCodes.DEVICE_NOT_FOUND, err.message);
    }
  }
};
```

- [ ] **Step 4: 运行测试验证通过**

Run: `cd cli && npm test`

- [ ] **Step 5: Commit**

```bash
git add cli/src/commands/tap.ts cli/tests/commands/tap.test.ts
git commit -m "feat: add tap command"
```

---

## Task 10-20: 其他基础命令批量实现

以下命令结构类似 tap，批量创建：

### Task 10: swipe
### Task 11: press_key, press_back, press_home, press_recents
### Task 12: long_press, drag
### Task 13: input_text
### Task 14: read_file, write_file
### Task 15: list_directory, delete_file, file_exists
### Task 16: list_apps, get_app_info, launch_app
### Task 17: install_app, uninstall_app, force_stop_app
### Task 18: get_device_info, get_battery_status
### Task 19: get_current_app, is_app_running
### Task 20: take_screenshot

每个命令遵循相同模式：写测试 → 实现 → 测试通过 → Commit

---

## Task 21: UI 树命令 - get_ui_tree

**Files:**
- Create: `cli/src/commands/get_ui_tree.ts`

```typescript
// src/commands/get_ui_tree.ts
import { ToolDefinition, CliOptions, ErrorCodes } from '../types';
import { selectDevice, execAdb, success, failure, parseUiTree } from '../utils';

export const definition: ToolDefinition = {
  name: 'get_ui_tree',
  description: 'Get the UI hierarchy tree of the current screen',
  parameters: [],
  adbCommand: 'shell uiautomator dump && shell cat',
  async execute(options: CliOptions): Promise<string> {
    try {
      const device = await selectDevice(options.device);

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
```

---

## Task 22: click_node_by_text

**Files:**
- Create: `cli/src/commands/click_node_by_text.ts`

```typescript
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
  async execute(text: string, exact: boolean = true, options: CliOptions): Promise<string> {
    try {
      const device = await selectDevice(options.device);

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
      } else {
        return failure(ErrorCodes.TAP_FAILED, tapResult.stderr);
      }
    } catch (err: any) {
      return failure(ErrorCodes.DEVICE_NOT_FOUND, err.message);
    }
  }
};
```

---

## Task 23: click_node_by_id

与 click_node_by_text 类似，使用 findNodeById。

---

## Task 24: wait_for_element（受限）

**Files:**
- Create: `cli/src/commands/wait_for_element.ts`

```typescript
// src/commands/wait_for_element.ts
import { ToolDefinition, CliOptions, ErrorCodes } from '../types';
import { selectDevice, execAdb, success, failure, parseUiTree, findNodeByText } from '../utils';

export const definition: ToolDefinition = {
  name: 'wait_for_element',
  description: 'Wait for an element with specified text to appear (polling-based)',
  parameters: [
    { name: 'text', type: 'string', required: true, description: 'Text to wait for' },
    { name: 'exact', type: 'boolean', required: false, description: 'Exact match', default: true },
    { name: 'timeout', type: 'number', required: false, description: 'Timeout in ms', default: 10000 }
  ],
  async execute(text: string, exact: boolean = true, timeout: number = 10000, options: CliOptions): Promise<string> {
    try {
      const device = await selectDevice(options.device);
      const startTime = Date.now();
      const pollInterval = 1000;

      while (Date.now() - startTime < timeout) {
        await execAdb(['shell', 'uiautomator', 'dump', '/sdcard/aat_ui.xml'], { deviceId: device, timeout: 10000 });
        const catResult = await execAdb(['shell', 'cat', '/sdcard/aat_ui.xml'], { deviceId: device });

        if (catResult.exitCode === 0) {
          try {
            const tree = parseUiTree(catResult.stdout);
            const node = findNodeByText(tree, text, exact);
            if (node) {
              return success({ found: true, waited_ms: Date.now() - startTime, text: node.text });
            }
          } catch { }
        }

        await new Promise(resolve => setTimeout(resolve, pollInterval));
      }

      return failure(ErrorCodes.TIMEOUT, `Element "${text}" not found within ${timeout}ms`);
    } catch (err: any) {
      return failure(ErrorCodes.DEVICE_NOT_FOUND, err.message);
    }
  }
};
```

---

## Task 25: 元命令 - list_devices, list_tools

**Files:**
- Create: `cli/src/commands/list_devices.ts`
- Create: `cli/src/commands/list_tools.ts`

```typescript
// src/commands/list_devices.ts
import { ToolDefinition, CliOptions } from '../types';
import { listDevices, success } from '../utils';

export const definition: ToolDefinition = {
  name: 'list_devices',
  description: 'List all connected ADB devices',
  parameters: [],
  async execute(options: CliOptions): Promise<string> {
    const devices = await listDevices();
    return success({ devices });
  }
};
```

```typescript
// src/commands/list_tools.ts
import { ToolDefinition, CliOptions } from '../types';
import { success } from '../utils';

export const definition: ToolDefinition = {
  name: 'list_tools',
  description: 'List all available tools and their status',
  parameters: [],
  async execute(options: CliOptions): Promise<string> {
    const supported = ['tap', 'swipe', 'press_key', 'get_ui_tree', 'click_node_by_text', ...];
    const unsupported = ['get_clipboard', 'set_clipboard'];
    const limited = ['wait_for_ui_stable', 'wait_for_element'];
    return success({ tools: supported, unsupported, limited, total: supported.length });
  }
};
```

---

## Task 26: CLI 入口 - 自动命令注册

**Files:**
- Create: `cli/src/index.ts`

- [ ] **Step 1: 实现 CLI 入口**

```typescript
// src/index.ts
import { Command } from 'commander';
import * as fs from 'fs';
import * as path from 'path';
import { ToolDefinition, CliOptions } from './types';
import { success, failure, prettyPrint, ErrorCodes } from './output';

const program = new Command();

program
  .name('aat')
  .description('Android Agent Tools - ADB-based tool commands for LLM agents')
  .version('1.0.0')
  .option('-d, --device <id>', 'Specify device ID')
  .option('--pretty', 'Pretty print JSON output')
  .option('-t, --timeout <ms>', 'Timeout in milliseconds', '30000');

function getCliOptions(): CliOptions {
  const opts = program.opts();
  return {
    device: opts.device,
    timeout: parseInt(opts.timeout),
    pretty: opts.pretty
  };
}

function registerCommand(def: ToolDefinition): void {
  const positionalArgs = def.parameters
    .filter(p => p.required)
    .map(p => `<${p.name}>`)
    .join(' ');

  const cmd = program.command(`${def.name}${positionalArgs ? ' ' + positionalArgs : ''}`)
    .description(def.description);

  def.parameters.filter(p => !p.required).forEach(p => {
    const flag = p.type === 'boolean' ? `--${p.name}` : `--${p.name} <value>`;
    cmd.option(flag, p.description ?? '', p.default);
  });

  cmd.action(async (...args) => {
    const cmdOpts = cmd.opts();
    const combinedOpts: CliOptions = { ...getCliOptions(), ...cmdOpts };
    const result = await def.execute(...args, combinedOpts);
    const output = combinedOpts.pretty ? prettyPrint(result) : result;
    console.log(output);
  });
}

function loadCommands(): void {
  const commandsDir = path.join(__dirname, 'commands');
  if (!fs.existsSync(commandsDir)) return;

  const files = fs.readdirSync(commandsDir).filter(f => f.endsWith('.js'));
  for (const file of files) {
    try {
      const { definition } = require(path.join(commandsDir, file));
      if (definition?.name) registerCommand(definition);
    } catch { }
  }
}

loadCommands();
program.parse();
```

- [ ] **Step 2: Commit**

```bash
git add cli/src/index.ts
git commit -m "feat: add CLI entry with auto command registration"
```

---

## Task 27: 构建测试

- [ ] **Step 1: 构建项目**

Run: `cd cli && npm run build`

- [ ] **Step 2: 测试 CLI 入口**

Run: `cd cli && node bin/aat --help`

- [ ] **Step 3: Commit**

```bash
git commit --allow-empty -m "test: verify CLI build"
```

---

## Task 28: 更新项目文档

**Files:**
- Modify: `docs/design.md`

- [ ] **Step 1: 更新设计文档**

将 ADB 工具命令集状态从"📋 规划中"改为"✅ 已完成"

- [ ] **Step 2: Commit**

```bash
git add docs/design.md
git commit -m "docs: update documentation for CLI tools"
```

---

## Spec Coverage Check

| Spec Section | Task Coverage |
|--------------|---------------|
| 项目结构 | Task 1, 2 |
| 类型定义 | Task 3 |
| JSON 输出 | Task 4 |
| ADB 执行 | Task 5 |
| 设备检测 | Task 6 |
| UI 树解析 | Task 7 |
| 基础命令 | Task 9-20 |
| UI 树命令 | Task 21-23 |
| 受限命令 | Task 24 |
| 元命令 | Task 25 |
| CLI 入口 | Task 26 |
| 文档更新 | Task 28 |