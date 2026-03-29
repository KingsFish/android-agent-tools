// tests/commands/long_press.test.ts
import { definition } from '../../src/commands/long_press';

describe('long_press command', () => {
  it('should have correct definition', () => {
    expect(definition.name).toBe('long_press');
    expect(definition.description).toContain('long press');
    expect(definition.parameters).toHaveLength(3);
    expect(definition.parameters[0].name).toBe('x');
    expect(definition.parameters[0].type).toBe('number');
    expect(definition.parameters[0].required).toBe(true);
    expect(definition.parameters[1].name).toBe('y');
    expect(definition.parameters[1].type).toBe('number');
    expect(definition.parameters[1].required).toBe(true);
    expect(definition.parameters[2].name).toBe('duration');
    expect(definition.parameters[2].type).toBe('number');
    expect(definition.parameters[2].required).toBe(false);
    expect(definition.parameters[2].default).toBe(500);
  });

  it('should have adb command defined', () => {
    expect(definition.adbCommand).toContain('swipe');
  });

  it('should have execute function', () => {
    expect(typeof definition.execute).toBe('function');
  });
});