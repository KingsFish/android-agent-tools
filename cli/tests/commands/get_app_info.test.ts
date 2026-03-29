// tests/commands/get_app_info.test.ts
import { definition } from '../../src/commands/get_app_info';

describe('get_app_info command', () => {
  it('should have correct definition', () => {
    expect(definition.name).toBe('get_app_info');
    expect(definition.description).toContain('detailed information');
    expect(definition.parameters).toHaveLength(1);
    expect(definition.parameters[0].name).toBe('package_name');
    expect(definition.parameters[0].type).toBe('string');
    expect(definition.parameters[0].required).toBe(true);
  });

  it('should have correct adb command', () => {
    expect(definition.adbCommand).toContain('dumpsys package');
  });
});