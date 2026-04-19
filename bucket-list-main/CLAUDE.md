# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A pure frontend bucket list web app with no build step, no package manager, and no server. Open `index.html` directly in a browser to run it.

## Development

No build, lint, or test commands exist. To develop:

- Open `index.html` in a browser (or use a local static server: `python3 -m http.server 8000`)
- All data persists in the browser's `localStorage` under the key `bucketList`

## Architecture

The app uses a strict two-layer separation loaded via `<script>` tags in order:

1. **`js/storage.js`** — Data layer. `BucketStorage` is a plain object (not a class) that owns all `localStorage` reads/writes and exposes CRUD methods (`addItem`, `updateItem`, `deleteItem`, `toggleComplete`), `getStats()`, and `getFilteredList(filter)`. It never touches the DOM.

2. **`js/app.js`** — UI layer. `BucketListApp` is an ES6 class instantiated as the global `app` on `DOMContentLoaded`. It calls into `BucketStorage` for all data operations, then re-renders the whole list on every change (`render()`). Inline `onclick` handlers in generated HTML call `app.*` directly (e.g., `app.handleToggle(id)`).

**Data model** (stored as JSON array in localStorage):
```js
{ id: string, title: string, completed: boolean, createdAt: ISO string, completedAt: ISO string | null }
```

Tailwind CSS is loaded from CDN; `css/styles.css` only adds animations, filter-button active states, responsive overrides, and dark-mode support on top of Tailwind utility classes.
