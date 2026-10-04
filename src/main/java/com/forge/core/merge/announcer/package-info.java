/**
 * forge-announcer merged into ForgeCore: scheduled MiniMessage announcements
 * with independent per-announcement intervals, four delivery channels (chat,
 * action bar, boss bar, title), sequential or random rotation, and manual
 * broadcast by id. Configured in {@code plugins/ForgeCore/announcer.yml}.
 *
 * <p>Nullness convention for this package: method parameters and return values
 * are non-null unless explicitly annotated {@code @Nullable}.
 */
@NotNullByDefault
package com.forge.core.merge.announcer;

import org.jetbrains.annotations.NotNullByDefault;
