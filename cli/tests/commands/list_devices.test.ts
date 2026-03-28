// tests/commands/list_devices.test.ts
import { definition } from '../../src/commands/list_devices';

describe('list_devices command', () => {
  it('should have correct definition', () => {
    expect(definition.name).toBe('list_devices');
    expect(definition.description).toContain('ADB devices');
    expect(definition.parameters).toHaveLength(0);
  });

  it('should have execute function', () => {
    expect(definition.execute).toBeDefined();
    expect(typeof definition.execute).toBe('function');
  });
});