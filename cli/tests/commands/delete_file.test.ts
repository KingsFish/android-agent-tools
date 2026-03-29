// tests/commands/delete_file.test.ts
import { definition } from '../../src/commands/delete_file';

describe('delete_file command', () => {
  it('should have correct definition', () => {
    expect(definition.name).toBe('delete_file');
    expect(definition.description).toContain('Delete');
    expect(definition.parameters).toHaveLength(2);
    expect(definition.parameters[0].name).toBe('path');
    expect(definition.parameters[0].type).toBe('string');
    expect(definition.parameters[0].required).toBe(true);
    expect(definition.parameters[1].name).toBe('recursive');
    expect(definition.parameters[1].type).toBe('boolean');
    expect(definition.parameters[1].required).toBe(false);
  });

  it('should have correct adb command template', () => {
    expect(definition.adbCommand).toContain('rm');
    expect(definition.adbCommand).toContain('<path>');
  });
});