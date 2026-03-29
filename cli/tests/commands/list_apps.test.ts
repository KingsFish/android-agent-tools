// tests/commands/list_apps.test.ts
import { definition } from '../../src/commands/list_apps';

describe('list_apps command', () => {
  it('should have correct definition', () => {
    expect(definition.name).toBe('list_apps');
    expect(definition.description).toContain('installed applications');
    expect(definition.parameters).toHaveLength(1);
    expect(definition.parameters[0].name).toBe('include_system');
    expect(definition.parameters[0].type).toBe('boolean');
    expect(definition.parameters[0].required).toBe(false);
    expect(definition.parameters[0].default).toBe(false);
  });

  it('should have correct adb command', () => {
    expect(definition.adbCommand).toContain('pm list packages');
  });
});