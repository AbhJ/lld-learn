/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/CellObserver.java — Observer contract for cell value changes
//
// Subscribers are notified after a cell's value is set (or recalculated as a
// dependent of another cell). Examples: a console logger, an audit trail, a
// UI re-renderer. The spreadsheet fires events; observers decide what to do.

interface CellObserver {
    /** Fired after cellId's display value changed from oldValue to newValue. */
    void onCellChanged(String cellId, String oldValue, String newValue);
}
