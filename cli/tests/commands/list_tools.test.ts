// tests/commands/list_tools.test.ts
import { definition } from '../../src/commands/list_tools';

describe('list_tools command', () => {
  it('should have correct definition', () => {
    expect(definition.name).toBe('list_tools');
    expect(definition.description).toContain('available tools');
    expect(definition.parameters).toHaveLength(0);
  });

  it('should have execute function', () => {
    expect(definition.execute).toBeDefined();
    expect(typeof definition.execute).toBe('function');
  });

  it('should return tools list', async () => {
    const result = await definition.execute();
    const parsed = JSON.parse(result);
    expect(parsed.success).toBe(true);
    expect(parsed.data.tools).toBeInstanceOf(Array);
    expect(parsed.data.tools.length).toBeGreaterThan(0);
    expect(parsed.data.total).toBe(parsed.data.tools.length);
  });

  it('should include supported tools', async () => {
    const result = await definition.execute();
    const parsed = JSON.parse(result);
    expect(parsed.data.tools).toContain('tap');
    expect(parsed.data.tools).toContain('swipe');
    expect(parsed.data.tools).toContain('list_devices');
    expect(parsed.data.tools).toContain('list_tools');
  });

  it('should list unsupported tools', async () => {
    const result = await definition.execute();
    const parsed = JSON.parse(result);
    expect(parsed.data.unsupported).toContain('get_clipboard');
    expect(parsed.data.unsupported).toContain('set_clipboard');
  });
});