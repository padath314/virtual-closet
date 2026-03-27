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

### Quick build

```bash
./build-mac.sh
```

Your app will be at `target/app/Virtual Closet.app`

### Adding an app icon

1. Create a 1024x1024 PNG icon
2. Save it as `icon.icns` in the project root (use an online PNG to ICNS converter, or macOS Preview)
3. Run `./build-mac.sh` again

### Manual icon creation (optional)

If you have a 1024x1024 PNG named `icon.png`:

```bash
mkdir MyIcon.iconset
sips -z 512 512 icon.png --out MyIcon.iconset/icon_512x512.png
sips -z 256 256 icon.png --out MyIcon.iconset/icon_256x256.png
sips -z 128 128 icon.png --out MyIcon.iconset/icon_128x128.png
iconutil -c icns MyIcon.iconset -o icon.icns
rm -rf MyIcon.iconset
```

## Notes

- Save data is written to `~/.virtual-closet/saved-outfits.json`.
- The app expects properly aligned transparent PNG layers.
- Accessories are multi-select; other categories are single-select.
