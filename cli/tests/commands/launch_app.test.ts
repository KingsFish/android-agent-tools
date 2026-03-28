// tests/commands/launch_app.test.ts
import { definition } from '../../src/commands/launch_app';

describe('launch_app command', () => {
  it('should have correct definition', () => {
    expect(definition.name).toBe('launch_app');
    expect(definition.description).toContain('Launch an application');
    expect(definition.parameters).toHaveLength(2);
    expect(definition.parameters[0].name).toBe('package_name');
    expect(definition.parameters[0].type).toBe('string');
    expect(definition.parameters[0].required).toBe(true);
    expect(definition.parameters[1].name).toBe('activity');
    expect(definition.parameters[1].type).toBe('string');
    expect(definition.parameters[1].required).toBe(false);
  });

  it('should have correct adb command', () => {
    expect(definition.adbCommand).toContain('am start');
    expect(definition.adbCommand).toContain('monkey');
  });
});