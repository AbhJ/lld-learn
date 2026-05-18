/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/TwoHundredHandler.java — Chain handler for $200 notes

public class TwoHundredHandler extends DenominationHandler {
    @Override public int denomination() { return 200; }
}
