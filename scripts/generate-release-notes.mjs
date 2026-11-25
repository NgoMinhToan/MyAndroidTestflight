#!/usr/bin/env node
// Generates the GitHub release body for MyAndroidTestflight.
//
// The body has two layers:
//   1. Markdown a person can read (version, build, date, grouped conventional commits).
//   2. A hidden HTML-comment block holding the same data as JSON, which the Android app parses
//      for update prompts. If the block is ever missing, the app falls back to the headings.
//
// Usage (from the Android repository root, after artifacts are collected into dist/):
//   node scripts/generate-release-notes.mjs \
//     --version 1.2.3 --version-code 42 --tag v1.2.3 \
//     --repo NgoMinhToan/MyAndroidTestflight \
//     --from v1.2.2 --to HEAD --dist dist --out release-notes.md

import { execFileSync } from 'node:child_process';
import { createHash } from 'node:crypto';
import { existsSync, readFileSync, readdirSync, statSync, writeFileSync } from 'node:fs';
import { join } from 'node:path';
import { pathToFileURL } from 'node:url';

export const SECTION_ORDER = ['features', 'fixes', 'changes', 'breaking'];

export const SECTION_TITLES = {
  features: 'New features',
  fixes: 'Bug fixes',
  changes: 'Changes',
  breaking: 'Breaking changes',
};

const CHANGE_TYPES = new Set([
  'perf',
  'refactor',
  'docs',
  'chore',
  'build',
  'ci',
  'style',
  'test',
]);

const KNOWN_ABIS = [
  'arm64-v8a',
  'armeabi-v7a',
  'armeabi',
  'x86_64',
  'x86',
  'riscv64',
  'mips64',
  'mips',
  'universal',
];

/** Parses a commit subject (and body) into a conventional-commit shape. */
export function parseCommitMessage(subject, body = '') {
  const trimmed = (subject ?? '').trim();
  const breakingFromBody = /(^|\n)BREAKING[ -]CHANGE:/.test(body ?? '');
  const match = /^([A-Za-z]+)(?:\(([^)]*)\))?(!)?:\s*(.+)$/.exec(trimmed);

  if (!match) {
    return {
      type: null,
      scope: null,
      message: trimmed,
      breaking: breakingFromBody,
    };
  }

  const [, rawType, scope, bang, description] = match;
  return {
    type: rawType.toLowerCase(),
    scope: scope || null,
    message: description.trim(),
    breaking: Boolean(bang) || breakingFromBody,
  };
}

/** Maps a parsed commit to one of the four release-note sections. */
export function classify(commit) {
  if (commit.breaking) return 'breaking';
  if (commit.type === 'feat') return 'features';
  if (commit.type === 'fix') return 'fixes';
  if (commit.type && CHANGE_TYPES.has(commit.type)) return 'changes';
  // Unknown or non-conventional commits still deserve a line in the notes.
  return 'changes';
}

export function sanitizeAuthor(name) {
  return (name ?? '').trim().replace(/\s+/g, '-');
}

export function formatEntry(commit) {
  return `${commit.message} (${commit.shortSha}) by @${commit.author}`;
}

export function buildSections(commits) {
  const grouped = Object.fromEntries(SECTION_ORDER.map((id) => [id, []]));
  for (const commit of commits) {
    grouped[classify(commit)].push(formatEntry(commit));
  }
  return SECTION_ORDER.map((id) => ({
    id,
    title: SECTION_TITLES[id],
    items: grouped[id],
  }));
}

/** Reads commits from git log between `from` (exclusive, optional) and `to`. */
export function collectCommits({ from, to = 'HEAD', cwd = process.cwd() }) {
  const range = from ? `${from}..${to}` : to;
  const format = '%H%x1f%an%x1f%s%x1f%b%x1e';
  const output = execFileSync('git', ['log', '--reverse', `--format=${format}`, range], {
    cwd,
    encoding: 'utf8',
    maxBuffer: 64 * 1024 * 1024,
  });

  return output
    .split('\x1e')
    .map((record) => record.trim())
    .filter((record) => record.length > 0)
    .map((record) => {
      const [sha, author, subject, body = ''] = record.split('\x1f');
      const parsed = parseCommitMessage(subject, body);
      return {
        sha,
        shortSha: sha.slice(0, 7),
        author: sanitizeAuthor(author),
        message: parsed.message,
        type: parsed.type,
        breaking: parsed.breaking,
      };
    });
}

/** Infers the ABI from a release asset name such as MyTestflight-1.2.3-arm64-v8a.apk. */
export function assetAbiFromName(name, version) {
  if (!name.toLowerCase().endsWith('.apk')) return null;
  const prefix = `MyTestflight-${version}-`;
  if (name.startsWith(prefix)) {
    return name.slice(prefix.length, -'.apk'.length);
  }
  const lower = name.toLowerCase();
  return KNOWN_ABIS.find((abi) => lower.endsWith(`-${abi}.apk`)) ?? null;
}

function readChecksums(path) {
  const sums = new Map();
  if (!existsSync(path)) return sums;
  for (const line of readFileSync(path, 'utf8').split(/\r?\n/)) {
    const match = /^([0-9a-f]{64})\s+\*?(.+)$/.exec(line.trim());
    if (match) sums.set(match[2], match[1]);
  }
  return sums;
}

function sha256File(path) {
  return createHash('sha256').update(readFileSync(path)).digest('hex');
}

/** Builds the JSON `assets` map from the collected APKs in `dist/`. */
export function collectAssets({ distDir, version, tag, repo }) {
  const assets = {};
  if (!distDir || !existsSync(distDir)) return assets;

  const sums = readChecksums(join(distDir, 'SHA-256SUMS.txt'));
  for (const name of readdirSync(distDir)) {
    const abi = assetAbiFromName(name, version);
    if (!abi) continue;
    const path = join(distDir, name);
    const stat = statSync(path);
    if (!stat.isFile()) continue;
    assets[abi] = {
      name,
      url: `https://github.com/${repo}/releases/download/${tag}/${name}`,
      size: stat.size,
      sha256: sums.get(name) ?? sha256File(path),
    };
  }
  return assets;
}

export function renderMarkdown({ version, versionCode, publishedAt, sections }) {
  const date = (publishedAt ?? new Date().toISOString()).slice(0, 10);
  const lines = [`## Version ${version}`, `Build ${versionCode}`, `Released ${date}`];

  for (const section of sections) {
    if (section.items.length === 0) continue;
    lines.push('', `## ${section.title}`);
    for (const item of section.items) {
      lines.push(`- ${item}`);
    }
  }
  return `${lines.join('\n')}\n`;
}

export function buildReleaseJson({
  version,
  versionCode,
  tag,
  publishedAt,
  assets,
  sections,
  commits,
}) {
  return {
    version,
    versionCode: Number(versionCode),
    tag,
    publishedAt,
    assets,
    sections,
    commits: commits.map((commit) => ({
      shortSha: commit.shortSha,
      author: commit.author,
      message: commit.message,
      type: commit.type,
    })),
  };
}

/** Builds the full release body: readable markdown plus the hidden JSON marker. */
export function generateReleaseBody({
  version,
  versionCode,
  tag,
  repo = 'NgoMinhToan/MyAndroidTestflight',
  publishedAt = new Date().toISOString(),
  commits,
  assets,
}) {
  const sections = buildSections(commits);
  const markdown = renderMarkdown({ version, versionCode, publishedAt, sections });
  const json = buildReleaseJson({
    version,
    versionCode,
    tag,
    publishedAt,
    assets,
    sections,
    commits,
  });
  return {
    markdown,
    json,
    body: `${markdown}\n<!-- mytestflight-release ${JSON.stringify(json)} -->\n`,
    sections,
  };
}

function parseArgs(argv) {
  const args = {};
  for (let index = 0; index < argv.length; index++) {
    const key = argv[index];
    if (!key.startsWith('--')) continue;
    const value = argv[index + 1];
    if (value === undefined || value.startsWith('--')) {
      args[key.slice(2)] = true;
    } else {
      args[key.slice(2)] = value;
      index++;
    }
  }
  return args;
}

function main() {
  const args = parseArgs(process.argv.slice(2));
  const version = args.version;
  const versionCode = args['version-code'];
  const tag = args.tag ?? `v${version}`;
  const repo = args.repo ?? 'NgoMinhToan/MyAndroidTestflight';
  const to = args.to ?? 'HEAD';
  const from = typeof args.from === 'string' && args.from.length > 0 ? args.from : undefined;

  if (!version || !versionCode) {
    console.error('Missing --version or --version-code.');
    process.exit(1);
  }

  const commits = collectCommits({ from, to });
  const assets = collectAssets({ distDir: args.dist, version, tag, repo });
  const { body, json } = generateReleaseBody({
    version,
    versionCode,
    tag,
    repo,
    commits,
    assets,
  });

  if (args.out) {
    writeFileSync(args.out, body, 'utf8');
    console.log(`Wrote ${args.out} (${commits.length} commits, ${Object.keys(assets).length} assets).`);
  } else {
    process.stdout.write(body);
  }
  console.error(`JSON sections: ${json.sections.map((s) => `${s.id}=${s.items.length}`).join(', ')}`);
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  main();
}
