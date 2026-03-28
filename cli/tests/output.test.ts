// tests/output.test.ts
import { success, failure } from '../src/output';

describe('output', () => {
  it('success should return correct JSON', () => {
    const result = success({ content: 'hello', size: 5 });
    const parsed = JSON.parse(result);
    expect(parsed.success).toBe(true);
    expect(parsed.data.content).toBe('hello');
    expect(parsed.data.size).toBe(5);
  });

  it('success with empty data', () => {
    const result = success({});
    const parsed = JSON.parse(result);
    expect(parsed.success).toBe(true);
    expect(parsed.data).toEqual({});
  });

  it('failure should return correct JSON', () => {
    const result = failure('FILE_NOT_FOUND', 'File does not exist: /test.txt');
    const parsed = JSON.parse(result);
    expect(parsed.success).toBe(false);
    expect(parsed.error.code).toBe('FILE_NOT_FOUND');
    expect(parsed.error.message).toBe('File does not exist: /test.txt');
  });

  it('output should be valid JSON string', () => {
    const result = success({ x: 500 });
    expect(() => JSON.parse(result)).not.toThrow();
  });
});