import assert from 'node:assert/strict';
import { mkdtempSync, rmSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { test } from 'node:test';

import {
  assetAbiFromName,
  buildSections,
  classify,
  collectAssets,
  generateReleaseBody,
  parseCommitMessage,
} from './generate-release-notes.mjs';

test('parses conventional commits with a scope', () => {
  const parsed = parseCommitMessage('feat(update): add auto update');
  assert.equal(parsed.type, 'feat');
  assert.equal(parsed.scope, 'update');
  assert.equal(parsed.message, 'add auto update');
  assert.equal(parsed.breaking, false);
});

test('marks bang commits as breaking', () => {
  assert.equal(parseCommitMessage('feat!: drop old api').breaking, true);
  assert.equal(parseCommitMessage('fix(core)!: rewrite storage').breaking, true);
});

test('detects BREAKING CHANGE in the body', () => {
  const parsed = parseCommitMessage('refactor: split module', 'BREAKING CHANGE: new API');
  assert.equal(parsed.breaking, true);
});

test('keeps non-conventional commit subjects intact', () => {
  const parsed = parseCommitMessage('Merge pull request #12 from feature');
  assert.equal(parsed.type, null);
  assert.equal(parsed.message, 'Merge pull request #12 from feature');
});

test('classifies commits into the fixed sections', () => {
  assert.equal(classify({ type: 'feat', breaking: false }), 'features');
  assert.equal(classify({ type: 'fix', breaking: false }), 'fixes');
  assert.equal(classify({ type: 'perf', breaking: false }), 'changes');
  assert.equal(classify({ type: 'docs', breaking: false }), 'changes');
  assert.equal(classify({ type: null, breaking: false }), 'changes');
  assert.equal(classify({ type: 'feat', breaking: true }), 'breaking');
});

test('groups commits and formats each entry', () => {
  const sections = buildSections([
    { type: 'feat', message: 'Add search', shortSha: 'abc1234', author: 'octocat', breaking: false },
    { type: 'fix', message: 'Stop crash', shortSha: 'def5678', author: 'hubot', breaking: false },
    { type: 'feat', message: 'New API', shortSha: 'aaa1111', author: 'octocat', breaking: true },
    { type: null, message: 'Merge branch main', shortSha: 'bbb2222', author: 'toan-ngo', breaking: false },
  ]);

  assert.deepEqual(sections.map((section) => section.id), ['features', 'fixes', 'changes', 'breaking']);
  assert.deepEqual(sections[0].items, ['Add search (abc1234) by @octocat']);
  assert.deepEqual(sections[1].items, ['Stop crash (def5678) by @hubot']);
  assert.deepEqual(sections[2].items, ['Merge branch main (bbb2222) by @toan-ngo']);
  assert.deepEqual(sections[3].items, ['New API (aaa1111) by @octocat']);
});

test('generates readable markdown plus a parseable hidden JSON block', () => {
  const { body, json } = generateReleaseBody({
    version: '1.2.3',
    versionCode: 42,
    tag: 'v1.2.3',
    publishedAt: '2026-11-25T10:00:00Z',
    commits: [
      { type: 'feat', message: 'Add auto update', shortSha: 'abc1234', author: 'octocat', breaking: false },
    ],
    assets: {
      'arm64-v8a': {
        name: 'MyTestflight-1.2.3-arm64-v8a.apk',
        url: 'https://github.com/NgoMinhToan/MyAndroidTestflight/releases/download/v1.2.3/MyTestflight-1.2.3-arm64-v8a.apk',
        size: 1200,
        sha256: 'deadbeef',
      },
    },
  });

  assert.match(body, /## Version 1\.2\.3/);
  assert.match(body, /Build 42/);
  assert.match(body, /Released 2026-11-25/);
  assert.match(body, /## New features/);
  assert.match(body, /- Add auto update \(abc1234\) by @octocat/);

  const marker = /<!-- mytestflight-release (\{.*?\}) -->/s.exec(body);
  assert.ok(marker, 'hidden marker present');
  const parsed = JSON.parse(marker[1]);
  assert.equal(parsed.version, '1.2.3');
  assert.equal(parsed.versionCode, 42);
  assert.equal(parsed.tag, 'v1.2.3');
  assert.equal(parsed.publishedAt, '2026-11-25T10:00:00Z');
  assert.deepEqual(parsed.commits, [
    { shortSha: 'abc1234', author: 'octocat', message: 'Add auto update', type: 'feat' },
  ]);
  assert.equal(parsed.assets['arm64-v8a'].sha256, 'deadbeef');
  assert.equal(parsed.sections.length, 4);
});

test('omits empty sections from the markdown but keeps them in JSON', () => {
  const { body, json } = generateReleaseBody({
    version: '1.0.0',
    versionCode: 1,
    tag: 'v1.0.0',
    publishedAt: '2026-01-01T00:00:00Z',
    commits: [],
    assets: {},
  });

  assert.doesNotMatch(body, /## Bug fixes/);
  assert.deepEqual(json.sections.map((section) => section.items), [[], [], [], []]);
});

test('infers ABIs from release asset names', () => {
  assert.equal(assetAbiFromName('MyTestflight-1.2.3-arm64-v8a.apk', '1.2.3'), 'arm64-v8a');
  assert.equal(assetAbiFromName('MyTestflight-1.2.3-universal.apk', '1.2.3'), 'universal');
  assert.equal(assetAbiFromName('MyTestflight-1.2.3.aab', '1.2.3'), null);
  assert.equal(assetAbiFromName('SHA-256SUMS.txt', '1.2.3'), null);
});

test('collects assets with sizes, checksums and download URLs', () => {
  const dir = mkdtempSync(join(tmpdir(), 'release-notes-'));
  try {
    writeFileSync(join(dir, 'MyTestflight-1.2.3-arm64-v8a.apk'), 'apk-bytes');
    writeFileSync(join(dir, 'MyTestflight-1.2.3-universal.apk'), 'universal-bytes');
    writeFileSync(
      join(dir, 'SHA-256SUMS.txt'),
      [
        `${'a'.repeat(64)}  MyTestflight-1.2.3-arm64-v8a.apk`,
        `${'b'.repeat(64)}  MyTestflight-1.2.3-universal.apk`,
        `${'c'.repeat(64)}  MyTestflight-1.2.3.aab`,
      ].join('\n'),
    );

    const assets = collectAssets({
      distDir: dir,
      version: '1.2.3',
      tag: 'v1.2.3',
      repo: 'NgoMinhToan/MyAndroidTestflight',
    });

    assert.deepEqual(Object.keys(assets).sort(), ['arm64-v8a', 'universal']);
    assert.equal(assets['arm64-v8a'].size, 'apk-bytes'.length);
    assert.equal(assets['arm64-v8a'].sha256, 'a'.repeat(64));
    assert.equal(
      assets['universal'].url,
      'https://github.com/NgoMinhToan/MyAndroidTestflight/releases/download/v1.2.3/MyTestflight-1.2.3-universal.apk',
    );
  } finally {
    rmSync(dir, { recursive: true, force: true });
  }
});
