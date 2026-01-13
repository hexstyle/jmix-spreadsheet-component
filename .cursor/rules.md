# Cursor System Rules for Jmix Projects

## 1. Global Priority Rules
- Always prefer local workspace code over any external sources.
- Do NOT use web search unless explicitly requested by the user.
- If required information is missing in the workspace, state this explicitly.
- Do not assume project-specific conventions unless visible in the code.

## 2. Framework Context
- The project is based on:
  - Jmix (Flow UI)
  - Vaadin Flow
  - Spring Boot
- Follow Jmix architectural conventions strictly.
- Jmix abstractions have priority over raw Spring or Vaadin APIs.

## 3. API Usage Rules
- Use ONLY APIs present in the workspace or stable Jmix / Vaadin releases.
- Never invent annotations, framework classes, or helper utilities.
- If unsure about an API, ask for the relevant source file instead of guessing.

---

# =========================
# Jmix ANTI-PATTERNS
# =========================

## 4. Forbidden Architectural Anti-Patterns

### ❌ Mixing Jmix and pure Vaadin lifecycles
- Do NOT use:
  - `UI.getCurrent()`
  - `VaadinSession` directly
  - `@Route` in Jmix Flow UI views
- All navigation and lifecycle must go through Jmix View APIs.

### ❌ Business logic in UI components
- Do NOT place:
  - database queries
  - complex calculations
  - transaction logic
inside View event handlers unless explicitly required.
- UI may orchestrate, not implement business rules.

### ❌ Direct EntityManager usage
- Do NOT use `EntityManager` or `Session` directly.
- Always prefer:
  - `DataManager`
  - `CollectionLoader`
  - `SaveContext`
- Exceptions must be explicitly justified.

### ❌ Bypassing Jmix Data Containers
- Do NOT manually synchronize UI components with entities.
- Use:
  - `CollectionContainer`
  - `InstanceContainer`
- Manual `setValue()` propagation is an anti-pattern.

### ❌ Manual security bypass
- Do NOT disable or bypass Jmix security checks.
- Do NOT access data assuming unrestricted permissions.
- Security context must be respected implicitly.

### ❌ Excessive Spring abstractions
- Do NOT introduce custom repositories, services, or DTO layers
  if Jmix standard mechanisms already cover the case.
- Prefer Jmix-provided services first.

---

# =========================
# Flow UI RULES
# =========================

## 5. UI (Flow UI) Rules
- Views must extend:
  - `StandardListView`
  - `StandardDetailView`
- Prefer Jmix UI events over Vaadin listeners.
- Avoid low-level Vaadin components unless Jmix has no abstraction.
- View logic belongs in the View, not in background services.

---

# =========================
# Vaadin Spreadsheet & Add-ons
# =========================

## 6. Spreadsheet / Add-ons General Rules
- Vaadin add-ons are SECONDARY to Jmix abstractions.
- Treat add-ons as UI widgets, not architectural foundations.
- Do NOT let add-ons dictate domain or persistence design.

## 7. Spreadsheet-Specific Rules
- Spreadsheet coordinates (row/column indexes) must be abstracted.
- Do NOT hardcode business logic tied to cell positions.
- Introduce a controller / adapter layer:
  - Maps domain data → spreadsheet model
  - Maps spreadsheet events → domain actions

### Required Pattern
- Spreadsheet access must go through a dedicated adapter class.
- The adapter must:
  - isolate Vaadin Spreadsheet API
  - expose semantic operations (e.g. setColumnValue, highlightRow)

### Forbidden Patterns
- ❌ Accessing Spreadsheet directly from services
- ❌ Using Spreadsheet as a data source of truth
- ❌ Encoding business rules in cell formulas

## 8. Add-ons Lifecycle Rules
- Add-ons must be initialized:
  - after View initialization
  - respecting Jmix lifecycle hooks
- Do NOT assume add-on initialization order.
- Avoid static access to add-on components.

---

# =========================
# Error Handling & Debugging
# =========================

## 9. Error Handling Rules
- Errors must be explained with:
  - root cause
  - affected Jmix component
  - architectural reason
- Avoid generic Vaadin explanations if the issue is Jmix-related.

---

# =========================
# Output & Explanation Rules
# =========================

## 10. Output Rules
- Output must be deterministic.
- No speculative APIs or undocumented behavior.
- If multiple approaches exist:
  - choose the most idiomatic Jmix solution
  - explain why alternatives are inferior.

## 11. Explanation Style
- Technical, concise, architecture-first.
- No tutorials unless explicitly requested.
- Prefer referencing workspace code over abstract descriptions.

# =========================
# Custom UI Components (Vaadin-based)
# =========================

## 12. Custom Component Creation Rules (Jmix-compliant)

### General Principle
- Any Vaadin add-on (including Spreadsheet) MUST be integrated as a Jmix UI component.
- Treat Spreadsheet as an imported Vaadin component, not as a View or data model.
- Follow the same principles as Jmix Charts add-on.

### Mandatory Component Structure
When introducing a new Vaadin-based component:

- Create a dedicated Jmix UI component class that:
  - wraps the Vaadin component
  - extends an appropriate Jmix base (or delegates lifecycle explicitly)
- The component must:
  - expose semantic, domain-oriented API
  - hide raw Vaadin APIs from Views and Services

### Lifecycle Rules
- Component initialization must align with Jmix View lifecycle:
  - construction
  - dependency injection
  - post-init / attach phase
- Do NOT initialize Vaadin components statically or eagerly.

---

# =========================
# Spreadsheet as Imported Vaadin Component
# =========================

## 13. Spreadsheet Integration Rules

### Component Role
- Spreadsheet is a UI component ONLY.
- Spreadsheet must NOT:
  - act as a data source
  - own domain state
  - contain business rules

### Required Layers
Spreadsheet integration MUST include:

1. **SpreadsheetComponent**
   - Wraps `com.vaadin.flow.component.spreadsheet.Spreadsheet`
   - Contains no business logic
   - Responsible only for UI rendering and events

2. **SpreadsheetAdapter / Controller**
   - Translates domain data → spreadsheet structure
   - Translates spreadsheet events → domain-level callbacks
   - Knows NOTHING about persistence

3. **View (StandardListView / StandardDetailView)**
   - Orchestrates:
     - data loading
     - adapter calls
     - UI reactions
   - Does not operate on cell coordinates directly

### Forbidden Shortcuts
- ❌ Direct Spreadsheet usage inside Views
- ❌ Mapping entities directly to rows/cells
- ❌ Hardcoded row/column indexes in View code
- ❌ Spreadsheet formulas encoding business rules

---

# =========================
# API & Exposure Rules
# =========================

## 14. Public API Design Rules
- Public APIs of custom components must be:
  - declarative
  - semantic
  - independent of Vaadin internals

### Example (Allowed)
- setColumns(List<ColumnDefinition>)
- renderRows(Collection<RowModel>)
- onRowSelected(Consumer<RowId>)

### Example (Forbidden)
- setCellValue(int row, int col, Object value)
- spreadsheet.getCell(row, col)

---

# =========================
# Anti-patterns (Component Level)
# =========================

## 15. Custom Component Anti-Patterns

### ❌ Treating Vaadin add-ons as Views
- Do NOT annotate add-ons with:
  - `@Route`
  - `@ViewController`
- Add-ons are components, not screens.

### ❌ Leaking Vaadin API
- Do NOT expose:
  - `Component`
  - `Element`
  - `Spreadsheet` itself
outside the wrapper.

### ❌ Bypassing Jmix DI
- Do NOT create components with `new` in arbitrary places.
- Components must be created and wired in Jmix-compatible ways.

---

# =========================
# Explanation & Reasoning Rules
# =========================

## 16. Reasoning Rules for Custom Components
- Always explain:
  - why a wrapper is required
  - where lifecycle boundaries are
  - how View ↔ Component ↔ Adapter interact
- Prefer Jmix idioms over Vaadin idioms when both are possible.
