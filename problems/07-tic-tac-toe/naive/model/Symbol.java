/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Symbol.java — Board markers (X, O) with opposite helper

enum Symbol {                           // enum = fixed set of constants; only X and O exist
    X, O;

    public Symbol opposite() { return this == X ? O : X; }

    @Override
    public String toString() { return name(); }
}
