/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/DocumentMemento.java — Memento pattern: opaque snapshot of Document state
//
// GoF Memento roles:
//   - Originator: Document (creates the memento, restores from it)
//   - Memento: DocumentMemento (this class) — opaque to outsiders, transparent to Document
//   - Caretaker: history/undo stack (holds onto mementos but never inspects them)
//
// The state is private and only Document (the originator) can read it via
// the package-private accessor. Outside callers can only pass the memento
// back into Document.restore(memento) — they cannot inspect or mutate it.
public final class DocumentMemento {
    private final String state;          // private = caretaker cannot read; only Document can
    private final long capturedAtMillis; // private = metadata for tooling/debugging

    /** Package-private ctor: only Document is allowed to create mementos. */
    DocumentMemento(String state) {
        this.state = state;
        this.capturedAtMillis = System.currentTimeMillis();
    }

    /** Package-private accessor: only Document (in the same package/default-package) reads the state. */
    String getState() { return state; }

    /** Public, non-revealing metadata — safe for caretakers. */
    public long getCapturedAtMillis() { return capturedAtMillis; }

    @Override public String toString() {
        return "DocumentMemento[" + state.length() + " chars @ " + capturedAtMillis + "]";
    }
}
