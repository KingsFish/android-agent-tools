// tests/commands/file_exists.test.ts
import { definition } from '../../src/commands/file_exists';

describe('file_exists command', () => {
  it('should have correct definition', () => {
    expect(definition.name).toBe('file_exists');
    expect(definition.description).toContain('exists');
    expect(definition.parameters).toHaveLength(1);
    expect(definition.parameters[0].name).toBe('path');
    expect(definition.parameters[0].type).toBe('string');
    expect(definition.parameters[0].required).toBe(true);
  });

  it('should have correct adb command template', () => {
    expect(definition.adbCommand).toContain('test');
    expect(definition.adbCommand).toContain('<path>');
  });
});