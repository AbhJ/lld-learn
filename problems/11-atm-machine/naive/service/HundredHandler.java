/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/HundredHandler.java — Chain handler for $100 notes

public class HundredHandler extends DenominationHandler {
    @Override public int denomination() { return 100; }
}
