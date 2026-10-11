---
name: pharmacy-web-ui
description: Standards and guidelines for preserving and enhancing the PharmaCare web dashboard, ensuring design fidelity, semantic HTML, responsive layout, and seamless Express API integration.
---

# Pharmacy Web UI Skill

This skill governs the frontend web development for PharmaCare.

## Design Philosophy & Preserved Theme
- **Branding**: PharmaCare green-and-white theme.
- **Palette**:
  - Pine: `--pine: #12372f`
  - Mint: `--mint: #dcebe3`
  - Panel / Background: `--panel: #fff`, `--bg: #f4f6f2`
  - Status chips: `--ok: #1d6b45`, `--warn: #8a5a00`, `--bad: #b3261e`
- **Typography**: Google Fonts `Bricolage Grotesque` (headings) and `IBM Plex Sans` (body, data tables).
- **Theme Support**: Maintain both light and dark mode CSS media queries and data-theme toggles.

## Frontend Architecture
1. **Separation of Concerns**: Keep clean separation between markup (`web/index.html`), styling (`web/css/`), client application logic (`web/js/`), and assets (`web/assets/`).
2. **API Communication**: All frontend views retrieve data using the `fetch` API against backend endpoints (e.g. `/api/medicines`, `/api/sales`).
3. **No Direct Database Access**: Never connect from the browser directly to MySQL.
4. **State Management**: Replace hardcoded in-memory arrays (`cat`, `co`, `sup`, `M`, `C`, `E`, `D`, `RX`, `P`, `S`) with asynchronous API calls and dynamic DOM rendering.
5. **Usability**: Provide real-time feedback, toasts, error messages, and loading spinners during network requests.
