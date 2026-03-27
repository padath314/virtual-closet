# Virtual Closet

A JavaFX desktop app for building outfits from layerable PNGs, searching by tags/colors, and saving favorite outfits.

## Features

- Layered outfit preview using a base PNG plus clothing PNG overlays
- Single-select categories for tops, bottoms, dresses, and shoes
- Multi-select accessories
- Search by item name, color, and tags
- Save outfits and mark them as favorites
- Project structure ready for later Mac packaging with `jpackage`

## Folder structure for your assets

Put your files here inside `src/main/resources/closet-data`:

```text
closet-data/
  base/
    base.png
  wardrobe/
    tops/
    bottoms/
    dresses/
    shoes/
    accessories/
  icons/
    wardrobe/
      tops/
      bottoms/
      dresses/
      shoes/
      accessories/
  metadata/
    items.json
```

### How image pairing works

- `wardrobe/.../*.png` is the full-size layer used in the outfit preview.
- `icons/wardrobe/.../*.png` is the zoomed-in thumbnail shown in the app.
- Use the same relative folder and filename in both places.
- Example:
  - preview layer: `wardrobe/tops/red-sweater.png`
  - icon: `icons/wardrobe/tops/red-sweater.png`

If a thumbnail PNG is missing, the app falls back to the main clothing PNG.

## Item metadata

Edit `src/main/resources/closet-data/metadata/items.json` and add one entry per item. Match the `id` to:

- `category-folder/filename-without-extension`
- Example: `tops/red-sweater`

Example item:

```json
{
  "id": "tops/red-sweater",
  "name": "Red Sweater",
  "category": "TOPS",
  "colors": ["red", "maroon"],
  "tags": ["cozy", "winter", "knit"],
  "layerOrder": 10
}
```

## Run locally

Requirements:

- Java 21+
- Maven 3.9+

Run:

```bash
mvn javafx:run
```

## Package as a Mac App (.app)

**Requirements:** macOS with JDK 21+ installed

1. Build the JAR:

```bash
mvn clean package
```

2. Create the app bundle with jpackage:

```bash
jpackage \
  --type app-image \
  --name "Virtual Closet" \
  --app-version "1.0.0" \
  --input target \
  --main-jar virtual-closet-1.0.0-SNAPSHOT.jar \
  --main-class com.virtualcloset.app.VirtualClosetApp \
  --dest target/app \
  --java-options "-Xmx2g" \
  --mac-package-name "VirtualCloset"
```

3. Your app will be at `target/app/Virtual Closet.app`

### Optional: Add a custom icon

Add `--icon path/to/icon.icns` to the jpackage command.

### Optional: Create a DMG

```bash
hdiutil create -volname "Virtual Closet" \
  -srcfolder "target/app/Virtual Closet.app" \
  -ov -format UDZO \
  VirtualCloset.dmg
```

## Notes

- Save data is written to `~/.virtual-closet/saved-outfits.json`.
- The app expects properly aligned transparent PNG layers.
- Accessories are multi-select; other categories are single-select.
