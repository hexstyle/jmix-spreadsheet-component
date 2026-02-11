# Предложения по задачам после обзора кодовой базы

## 1) Исправление опечатки
**Проблема:** в английском сообщении `databaseUniqueConstraintViolation.IDX_SCM_USER_TRACK_TYPE_ACCESS_UNQ` используется грамматически некорректная фраза `Access is already exists`.

**Где:** `src/main/resources/com/digtp/scm/messages_en.properties`.

**Решение:** заменить текст на корректный вариант `Access already exists`.

**Статус:** ✅ Исправлено.

---

## 2) Исправление ошибки
**Проблема:** метод `validateModel(...)` в `DefaultSpreadsheetController` был пустым (placeholder), поэтому несоответствие типа `SpreadsheetTableModel` и контейнера не выявлялось рано и могло приводить к поздним runtime-ошибкам при рендеринге/редактировании.

**Где:** `src/main/java/com/hexstyle/jmixspreadsheet/internal/DefaultSpreadsheetController.java`.

**Решение:** реализована проверка совместимости: при bind валидируется `model.getEntityClass()` и фактические элементы контейнера; при конфликте выбрасывается понятное `IllegalArgumentException`.

**Статус:** ✅ Исправлено.

---

## 3) Комментарий по документации
Пункт про `io/jmix/flowui` не требует исправления: это классы из ядра Jmix, и перечислять все внешние/ядровые библиотеки в структуре нашей библиотеки не нужно.

**Статус:** ℹ️ Оставлено без изменений по согласованию.

---

## 4) Улучшение теста
**Проблема:** `MessageResolutionTest` проверял только один ключ локализации (`applicationTitle.text`), из-за чего регрессии в остальных ключах и fallback-локализации могли пройти незамеченными.

**Где:** `src/test/java/com/digtp/scm/messages/MessageResolutionTest.java`.

**Решение:** тест расширен до parameterized-проверок:
- набор критичных ключей из разных областей;
- проверка, что не возвращается "сырой" ключ;
- проверка fallback-поведения для неподдерживаемой локали.

**Статус:** ✅ Исправлено.
