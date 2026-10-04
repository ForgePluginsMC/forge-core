package com.forge.core.cmd.playera;

/**
 * Holds pack-owned managers created by {@link PlayerASetup} so commands can
 * reach them without touching the core plugin class.
 */
final class PlayerAState {
    static TmbManager tmb;
    static CuffManager cuff;
    static DisableEnchantManager disabledEnchants;

    private PlayerAState() {
    }
}
