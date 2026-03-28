// tests/adb.test.ts
import { buildAdbArgs } from '../src/adb';

describe('adb', () => {
  it('buildAdbArgs should add device selector', () => {
    const args = buildAdbArgs(['shell', 'input', 'tap', '500', '800'], 'emulator-5554');
    expect(args).toEqual(['-s', 'emulator-5554', 'shell', 'input', 'tap', '500', '800']);
  });

  it('buildAdbArgs without device should not add selector', () => {
    const args = buildAdbArgs(['shell', 'input', 'tap', '500', '800']);
    expect(args).toEqual(['shell', 'input', 'tap', '500', '800']);
  });
});