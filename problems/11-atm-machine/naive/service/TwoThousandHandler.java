/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/TwoThousandHandler.java — Chain handler for $2000 notes (head of the chain)

public class TwoThousandHandler extends DenominationHandler {
    @Override public int denomination() { return 2000; }
}
