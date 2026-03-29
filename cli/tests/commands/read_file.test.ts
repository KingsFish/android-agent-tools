// tests/commands/read_file.test.ts
import { definition } from '../../src/commands/read_file';

describe('read_file command', () => {
  it('should have correct definition', () => {
    expect(definition.name).toBe('read_file');
    expect(definition.description).toContain('file');
    expect(definition.parameters).toHaveLength(1);
    expect(definition.parameters[0].name).toBe('path');
    expect(definition.parameters[0].type).toBe('string');
    expect(definition.parameters[0].required).toBe(true);
  });

  it('should have correct adb command template', () => {
    expect(definition.adbCommand).toBe('shell cat <path>');
  });
});