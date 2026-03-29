// tests/commands/press_recents.test.ts
import { definition } from '../../src/commands/press_recents';

describe('press_recents command', () => {
  it('should have correct definition', () => {
    expect(definition.name).toBe('press_recents');
    expect(definition.description).toContain('recents');
    expect(definition.parameters).toHaveLength(0);
  });

  it('should have adb command defined', () => {
    expect(definition.adbCommand).toContain('keyevent 187');
  });

  it('should have execute function', () => {
    expect(typeof definition.execute).toBe('function');
  });
});