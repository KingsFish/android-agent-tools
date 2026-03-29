// tests/commands/press_key.test.ts
import { definition } from '../../src/commands/press_key';

describe('press_key command', () => {
  it('should have correct definition', () => {
    expect(definition.name).toBe('press_key');
    expect(definition.description).toContain('key');
    expect(definition.parameters).toHaveLength(1);
    expect(definition.parameters[0].name).toBe('key');
    expect(definition.parameters[0].type).toBe('string');
    expect(definition.parameters[0].required).toBe(true);
  });

  it('should have adb command defined', () => {
    expect(definition.adbCommand).toContain('keyevent');
  });

  it('should have execute function', () => {
    expect(typeof definition.execute).toBe('function');
  });
});