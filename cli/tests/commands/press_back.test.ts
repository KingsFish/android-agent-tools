// tests/commands/press_back.test.ts
import { definition } from '../../src/commands/press_back';

describe('press_back command', () => {
  it('should have correct definition', () => {
    expect(definition.name).toBe('press_back');
    expect(definition.description).toContain('back');
    expect(definition.parameters).toHaveLength(0);
  });

  it('should have adb command defined', () => {
    expect(definition.adbCommand).toContain('keyevent 4');
  });

  it('should have execute function', () => {
    expect(typeof definition.execute).toBe('function');
  });
});