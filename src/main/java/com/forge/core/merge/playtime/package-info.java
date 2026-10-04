/**
 * ForgeCore merge of the standalone forge-playtime plugin: claimable
 * playtime milestone rewards.
 *
 * <p>Keeps the more advanced version of everything: playtime itself is tracked
 * by ForgeCore's {@code UserData} (no second tracker), while the milestone
 * GUI, claim-once rewards and hot reload come from forge-playtime.
 *
 * <p>Nullness convention for this package: method parameters and return values
 * are non-null unless explicitly annotated {@code @Nullable}.
 */
@NotNullByDefault
package com.forge.core.merge.playtime;

import org.jetbrains.annotations.NotNullByDefault;
