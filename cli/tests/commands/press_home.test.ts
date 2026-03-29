// tests/commands/press_home.test.ts
import { definition } from '../../src/commands/press_home';

describe('press_home command', () => {
  it('should have correct definition', () => {
    expect(definition.name).toBe('press_home');
    expect(definition.description).toContain('home');
    expect(definition.parameters).toHaveLength(0);
  });

  it('should have adb command defined', () => {
    expect(definition.adbCommand).toContain('keyevent 3');
  });

  it('should have execute function', () => {
    expect(typeof definition.execute).toBe('function');
  });
});