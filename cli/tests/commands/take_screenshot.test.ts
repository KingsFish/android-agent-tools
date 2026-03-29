// tests/commands/take_screenshot.test.ts
import { definition } from '../../src/commands/take_screenshot';

describe('take_screenshot command', () => {
  it('should have correct definition', () => {
    expect(definition.name).toBe('take_screenshot');
    expect(definition.description).toContain('screenshot');
    expect(definition.parameters).toHaveLength(1);
    expect(definition.parameters[0].name).toBe('display');
    expect(definition.parameters[0].type).toBe('number');
    expect(definition.parameters[0].required).toBe(false);
  });

  it('should have correct adb command', () => {
    expect(definition.adbCommand).toBe('shell screencap -p');
  });
});