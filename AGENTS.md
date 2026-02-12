# AGENTS.md

## Testing & Validation Rules (applies to all tests in this repo)

### 1) General principles
- Prefer automated verification over manual checks.
- Any behavior change must be validated by at least one reproducible command.
- Include the exact commands and outcomes in the final report.

### 2) Unit / integration / UI tests
- Run targeted tests for changed areas first.
- If changes affect shared component logic, run broader regression tests when feasible.
- Treat warnings separately from failures; only failures block completion.

### 3) E2E rules
- Credentials for UI/e2e runs:
  - username: `admin`
  - password: `1`
- For flows that render `SpreadsheetComponent`, run app/tests in **Vaadin production mode**.
- E2E verification should include:
  1. user interaction in UI,
  2. observable UI outcome (log/message/cell state),
  3. persistence check in DB when edit/save behavior is changed.

### 4) Production-mode requirement for Spreadsheet e2e
- Do not consider Spreadsheet e2e valid when app runs in Vaadin development mode.
- A valid run must confirm production mode in runtime logs.

### 5) Evidence artifacts
- For perceptible frontend changes, attach a screenshot artifact from browser automation.
- Prefer screenshots that show both the edited UI area and verification indicator (e.g., edit log).

### 6) Database persistence checks
- When validating edit/save behavior, verify persisted data with a deterministic DB query.
- In reports, include query command and the key result value used for validation.

