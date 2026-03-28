// tests/types.test.ts
import { ToolDefinition, Parameter, CliOptions } from '../src/types';

describe('types', () => {
  it('ToolDefinition should have required fields', () => {
    const def: ToolDefinition = {
      name: 'test',
      description: 'Test tool',
      parameters: [],
      execute: async () => '{"success": true}'
    };
    expect(def.name).toBe('test');
    expect(def.description).toBe('Test tool');
  });

  it('Parameter should support all types', () => {
    const param: Parameter = {
      name: 'x',
      type: 'number',
      required: true,
      description: 'X coordinate'
    };
    expect(param.type).toBe('number');
  });

  it('CliOptions should have optional fields', () => {
    const opts: CliOptions = {};
    expect(opts.device).toBeUndefined();
    expect(opts.timeout).toBeUndefined();
  });
});