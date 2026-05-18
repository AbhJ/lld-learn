/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/ChangeStrategy.java — Strategy contract for change-making algorithms

import java.util.Map;

interface ChangeStrategy {
    /** Compute coin counts that sum to the requested amount in cents. */
    Map<Coin, Integer> calculateChange(int amountInCents);
}
