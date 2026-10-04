# ForgeCore UI Resource Pack

Dark web-dashboard theme for ForgeCore's GUI system.

## Theme
- **Background**: Near-black `#0d1117`
- **Header/Footer**: `#161b22` with ember-orange `#ff7b2f` accents
- **Style**: Modern web dashboard, not a chest full of items

## Contents

### GUI Background (`textures/gui/container/generic_54.png`)
Custom 256x256 background with:
- Header bar (y=0-17) with ember accent line
- Tab row background (y=17-36)
- Content area (y=36-126) with subtle row separators
- Footer bar (y=126-144) with top border
- Dark-themed player inventory slots

### Buttons & Icons (`textures/item/forge/`)
25 custom textures, accessed via `custom_model_data` strings on paper items:

**Buttons**: `forge_button_primary`, `forge_button_secondary`, `forge_button_danger`,
`forge_button_success`, `forge_button_back`, `forge_arrow_left`, `forge_arrow_right`

**Indicators**: `forge_dot_active`, `forge_dot_inactive`, `forge_status_online`,
`forge_status_offline`, `forge_status_busy`, `forge_status_away`

**Category icons**: `forge_icon_teleport`, `forge_icon_moderation`, `forge_icon_economy`,
`forge_icon_tools`, `forge_icon_guild`, `forge_icon_quest`, `forge_icon_permission`,
`forge_icon_npc`, `forge_icon_warp`, `forge_icon_home`, `forge_icon_kit`, `forge_icon_mail`

### Item Models
- `assets/minecraft/items/paper.json` — `minecraft:select` on `custom_model_data` strings
- `assets/minecraft/models/item/forge/*.json` — individual model definitions

### Font Glyphs (`font/forge_ui.json`)
Custom 8x8 bitmap glyphs in Private Use Area:
- U+E000: ← arrow_left
- U+E001: → arrow_right
- U+E002: ● dot_filled
- U+E003: ○ dot_empty
- U+E004: ✓ check
- U+E005: ✗ cross
- U+E006: ★ star
- U+E007: ─ divider

Use with font `minecraft:forge_ui` in MiniMessage: `<font:minecraft:forge_ui>\uE000</font>`

## Usage in Code

```java
// Custom icon button
GuiItem.of(Material.PAPER)
    .model(ForgeIcons.ICON_TELEPORT)
    .name("<gold><bold>Teleport")
    .lore("<gray>Homes and warps")
    .action(p -> /* ... */);

// Card-style item
GuiItem.card(ForgeIcons.ICON_HOME, "<gold><bold>My Homes", "Teleport to saved homes")
    .action(p -> new HomeGui(plugin, parent).open(p));
```

## Distribution
Players get the pack via `/resourcepack` (requires `resourcepack-url` in config.yml).

For testing, the pack ZIP is at `/home/chris/test-server/forgecore-ui.zip` on bigpc.
