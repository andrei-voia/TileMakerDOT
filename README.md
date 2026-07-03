# TileMaker DOT Application

***

## 🌟 Overview

**TileMaker DOT** is a custom map creation application designed for game developers who need a **fast, efficient, and user-friendly tool**. It provides a unique, streamlined workflow and flexible asset management system, allowing you to focus purely on map design.

**Created by:** Andrei Voia

***

## 💻 Platform Support

**TileMaker DOT** is a cross-platform application. To ensure maximum performance and portability, specific versions of the Java Runtime are included for each system.

**Windows** Fully supported (x64)

**Linux** Fully supported (Ubuntu, Mint, Debian, etc.)

**macOS** Fully supported (Intel x64 & Apple Silicon via Rosetta)

***

## 📥 Download
Get the latest version of TileMaker DOT on Itch.io:
[**Download on Itch.io**](https://crytek22.itch.io/tilemakerdot)

***

## ✨ Key Features & Benefits

TileMaker DOT addresses common limitations found in traditional map editors by offering:

| Feature Category         | TileMaker DOT Benefit | Competitive Advantage |
| ---                      | ---                   | ---                   |
| **Multi-Platform**       | Native Launchers for Windows/Mac/Linux.                                                     | Runs anywhere without needing a system-wide Java installation. |
| **Personalization & UI** | **Light and Dark Themes** Optimized for long dev sessions and better visibility in different environments. | Features a modern and professional aesthetic. |
| **Export Control**       | Universal Compatibility: Supports **CSV, XML, JSON, TMX (Tiled), TMJ, and .LVL** formats.   | Provides granular control over the final file structure and appearance. |
| **Asset Management**     | Images can be modified and added later without altering the unique ID of existing textures. | Avoids "breaking" existing map data when updating visual assets. |
| **Hot-Reloading**        | Re-scans folders and updates textures without restarting.                                   | Dramatically speeds up the art pipeline by allowing "Live Edits." |
| **Animation System**     | **Frame-Based Sync** Automatically detects frames and synchronizes them globally.           | Ensures all animated world objects (torches, water) flicker in perfect unison. |
| **Auto-ID Assistant**    | **Intelligent Prefixing** Automatically assigns unique IDs to textures missing them.        | Eliminates the tedious manual task of naming files, preventing ID conflicts automatically. |
| **Chunk Selection**      | Drag-and-drop mass manipulation for copying, moving, or exporting map sections.             | Simplifies modular map design by allowing users to save and reuse "chunks" (houses, rooms). |
| **Random Brush**         | **Scatter Painting** Select multiple IDs to paint randomized variations automatically.      | Breaks up visual repetition in natural environments like forests or grasslands. |
| **Texture Organization** | Allows the user to define custom IDs for all used textures.                                 | Ensures map data aligns perfectly with your game's object structure, rather than adapting to randomly assigned editor IDs. |
| **Layer Simplicity**     | Uses a single layer for each map type (`tiles`, `objects`, `npcs`), with automatic visual depth sorting(front-to-back sorting). This sorting is determined by analyzing the object's bottom-most non-transparent pixels. | Eliminates the complex "Photoshop-style" management of multiple layers just for depth sorting. By using pixel analysis, it provides pixel-perfect visual layering without manual configuration. |
| **Dynamic Resizing**     | Provides powerful quick-action tools for map expansion and resizing.                        | Allows easy modification of the map canvas size after creation, offering flexibility for evolving project needs. |
| **User Experience**      | A streamlined and intuitive interface makes the application significantly easier to use than similar complex tools. Reduces the learning curve and speeds up map creation for the end-user. |
| **Asset Filtering**      | Selective ID Loading: Only imports assets defined in a specific whitelist file.             | Prevents workspace clutter and memory overhead by excluding unused or legacy textures without needing to delete files. |
| **Spritesheet Slicer**   | Import a full sheet and cut it into individual assets without leaving the app.              | Eliminates the need for external image editors and automatically handles file saving and ID assignment in one workflow. |
| **Annotated Notes**      | Place persistent, color-coded pins directly onto map tiles to leave design memos, boundary and script reminders, or map out level design notes. | Keeps level design ideas, collaboration notes, and scripting instructions attached directly to the map canvas, scaling perfectly during meetings and game development. |
***

***

## 🎮 Game Engine Compatibility

TileMaker DOT provides multiple export options to fit different professional and custom workflows.

### 🛠️ Industry Standard (Godot, Unity, Tiled)
The **Export to Tiled (.tmx/.tsx)** feature generates industry-standard files that allow you to move your level design into major game engines instantly.
* **Tiled Map Editor:** Native support. Files can be opened, edited, and refined directly in Tiled.
* **Godot Engine:** Fully compatible using the [**YATI**](https://github.com/Kiamo2/YATI) plugin from the Godot Asset Library.
* **Unity:** Fully compatible using the [**SuperTiled2Unity**](https://seanba.itch.io/supertiled2unity) package.
* **General Support:** Any framework or engine (like LibGDX) that supports the XML-based TMX format can read TileMaker DOT maps.

### 🕹️ Custom Scripting & GameMaker
The **JSON Export** is optimized for developers who want total control over their loading logic:
* **GameMaker (GMS2):** Clean JSON structure perfect for `json_parse()`. Map TileMaker IDs directly to GML sprites or objects using custom scripts.
* **Web Engines:** Ideal for JavaScript/Phaser frameworks due to the lightweight, native JSON format.
* **Direct IDs:** Unlike TMX, the JSON export uses your **internal texture IDs** directly, making it easier to sync map data with your game code logic.

### 📊 Performance & Retro Formats
* **.CSV:** Universal plain-text grid of IDs. Perfect for spreadsheet analysis or retro-engine parsers.
* **.LVL:** An ultra-lightweight, zero-bloat version of CSV designed for maximum loading speed in custom C++/C# frameworks.

> **Pro-Tip:** For the best experience in Unity and Godot, always use the **relative path (`assets`)** at startup. This ensures the game engine can find your textures even if you move the project folder.

***

## 🚀 Getting Started

### Running the Application
**TileMaker DOT** is portable and includes its own environment. Use the launcher specific to your operating system:

`Windows`: Double-click TileMakerLauncher.exe

`macOS`: Double-click TileMakerDOT_macOS_Launcher.command
 Note: If blocked by security, Right-click > Open, or allow in System Settings > Privacy & Security.

`Linux / Mint`: Run TileMakerDOT_Linux_Launcher.sh
 Note: Ensure the file has "Allow executing as program" enabled in file properties.

> **⚠️ Important Path Note:** The path you enter or browse for in the **"Asset Base Path"** field at startup determines your export behavior:
> * **Relative Path:** If you keep it as `assets` (or a relative subfolder), the exported `.tmx` and `.tsx` files will use relative paths. This is recommended for moving projects between different computers or sharing with a team.
> * **Absolute Path:** If you use the "Browse" button to select a full path (e.g., `C:\Users\Name\Desktop\assets`), the exported files will hardcode that absolute path.

### Core Asset Requirements

All custom assets must adhere to the following naming and structure rules:

| Asset Type            | Requirement | Example |
| ---                   | ---         | ---    |
| **Image Format**      | Must be a **PNG** image. | `2_dark_grass_texture.png` |
| **Content**           | Each PNG must contain only a single item (tile, object, or NPC). | Tip: Use the Spritesheet Importer to slice composite sheets into single files. |
| **ID Requirement**    | Every asset name must start with its unique ID followed by an underscore (`ID_`). | `101_lightDirt.png`, `1044_villagerWithHighPants.png` |
| **Animated Asset**    | Use the suffix **`_f`** followed by the frame number. | `10_water_f1.png`, `10_water_f2.png` |
| **Unique IDs**        | All items within a map type (tiles, objects, or NPCs) must have a unique ID to avoid errors. |
| **Folder Naming**     | Add **`#hidden`** to the end of any asset subfolder name (`tiles`, `objects`, or `npcs`). | `houses#hidden`, `unreleased_items#hidden` |
| **Visibility Effect** | Assets in `#hidden` folders are **not shown** in the asset selection UI but **remain visible** in the application map view. |
| **Rendering Layer**   | Add `#B` at the end or `#b#` anywhere in the asset name (both lowercase and uppercase work) to force the object to render under all others. Add `#A` at the end or `#a#` anywhere in the asset name to force the object to render above all others. | `121_rug_round#b.png`, `200_rug#A#round.png` |

### Object and NPC Placement

Objects and NPCs are placed based on the **top-left corner** of the image. When placing an item, the top-left pixel of that item will align with the cursor's location on the map grid.

When hovering over any asset in the asset selection UI view, a concise tooltip provides immediate details, including the asset's name, unique ID, and its exact size in tiles (for Objects and NPCs).

## 🛠️ Advanced Toolset

### 🖌️ Random Scatter Brush
Select multiple items in the palette to activate **Brush Mode**. The editor will randomly cycle through your selection as you paint, creating natural, non-repetitive landscapes instantly.
**Cleanup:** Use `Tools > Cleanup Brush Selection` to reset your selection.

### 📦 Chunk Selection Tool
Enable the **Chunk Tool** to click and drag a rectangle over the canvas. 
* **Export:** Save the selected area as a standalone `.tmdot` file.
* **Import:** "Stamp" a saved chunk into any other map.

### 🤖 Auto-ID Assistant
Press **F4** to trigger the ID Assistant. It automatically scans your asset folders for files missing numeric prefixes and assigns them the next available unique ID based on your project structure.

### 🖼️ Spritesheet Importer
Access via `File > Import Spritesheet`. This tool allows you to take a large composite image (like a 1024x1024 sheet) and slice it into individual PNGs directly into your project folders.
* **Smart History:** Previously imported areas are highlighted in **Green**, so you never export the same sprite twice.
* **Auto-ID:** The tool automatically suggests the next available ID for the chosen folder (Tiles, Objects, or NPCs).
* **Navigation:**
	* `Right Click + Drag` to Pan.
    * `Mouse Wheel` to Zoom.
    * `Left Click + Drag` to select your crop area.

### 🧩 Autotile Configuration
To enable autotiling for a set of textures, place the texture image in the `assets/tiles/autotiles/` folder and name it according to this format:

`IDtile1_IDtile2_IDautotile_textureName.png`

* **IDtile1:** The ID of the primary tile.
* **IDtile2:** The ID of the secondary tile.
* **IDautotile:** The starting ID for all generated subtiles (range: `IDautotile` to `IDautotile + 15`).

**Example:** `1_3_500_grassDark_with_grassNormal.png` means the primary tile is `1`, the secondary is `3`, and the generated subtile IDs will range from `500` to `515`.

## 🎨 User Experience: Adaptive Dark Mode
TileMaker DOT features a fully integrated **Light Mode** and **Dark Mode** implementation. 
**Dynamic Theming:** The interface, including the **Legend** and **About** tabs, automatically adapts its color palette to reduce eye strain.
**Smart Persistence:** Your theme preference is saved locally. If you switch to Dark Mode, the app will remember your choice the next time you launch.

## 📝 Annotated Notes Layer
Activate the **Annotated Notes Tool** from the `Tools > Annotated Notes Tool` to place custom color-coded map markers with custom descriptions directly on the map grid.
* **Persistent Reminders:** Perfect for flagging collision boundaries, writing scripting memos, or planning out area specific level design notes.
* **Smart Zoom Scaling:** Notes calculate their position dynamically relative to your pan offsets and keep a consistent pixel size so they remain perfectly readable no matter how far you zoom in or out.
* **Color Customization:** Use the pop-up configuration panel to select custom enum based label colors with automatic UI live previews.

***

## ⚙️ Configuration Files

The **`settings/`** folder contains text files used to modify default application values:

| File Name                       | Description | Example/Notes |
| ---                             | ---         | ---           |
| `default_assets_path.txt`       | Defines the location of the assets folder. By default, it uses a relative path (`./assets`). Can be changed to an **absolute path** for custom setups. | `C:\Users\User\MyGame\Assets` |
| `default_dark_mode.txt`         | Sets the initial theme (0 for Light, 1 for Dark). | `1` (Dark Mode) |
| `default_frame_ms_duration.txt` | Sets the global animation speed (in milliseconds). | `200` (Lower is faster) |
| `default_grids.txt`             | Used to add quick-select buttons for common starting map sizes (in tiles). The first size listed is used as the default. | `50x50, 100x100` |
| `default_tile_size.txt`         | Sets the pixel size of a single tile. Objects and NPCs will be scaled proportional to this size. | A 128x128px house on a 64px tile map will occupy 2x2 tiles. |
| `default_used_ids_list.txt`     | Whitelist Filter: A list of specific IDs the application is allowed to load. Useful for large asset folders where you only need a subset of items for a specific map. | Use the **Export Used IDs List** option in the app to automatically generate this file based on your current map. |

***

## ⚙️ Smart Configuration & Persistence

TileMaker DOT features a **Live-Sync Settings System**. While you can manually edit the files in the `settings/` folder, the application automatically updates these files whenever you make changes through the **Startup UI** or **In-App Menus**. 

| Configuration File              | Controlled Via UI              | Auto-Saves? |
| ---                             | ---                            | ---         |
| `default_assets_path.txt`       | Startup "Browse" or Path Field | ✅ Yes      |
| `default_dark_mode.txt`         | Menu Toggle                    | ✅ Yes      |
| `default_frame_ms_duration.txt` | Startup Animation Field        | ✅ Yes      |
| `default_grids.txt`             | Startup Grid Size Field        | ❌ No       |
| `default_tile_size.txt`         | Startup Tile Size Field        | ✅ Yes      |
| `default_used_ids_list.txt`     | Manual File Edit Only          | ❌ No       |

### How it works:
1. **Startup:** The app reads these files to configure your workspace.
2. **Session:** You change a setting (like switching to Dark Mode or picking a new Assets folder).
3. **Instant Save:** TileMaker DOT overwrites the corresponding `.txt` file immediately.
4. **Persistence:** Next time you launch, your previous session's environment is perfectly restored.

***

✨ Tip: Instead of manually typing ID numbers into `default_used_ids_list.txt`, simply build your map first, then go to `File` > `Export Used IDs List...`. Move that exported file into the `settings/` folder to ensure only those assets load next time.

## 📂 Application Structure

The application requires a strict file structure to function correctly:

```text
TileMaker DOT/
├── assets/
│   ├── tiles/
│   │   ├── autotiles/                   (Autotile textures go here)
│   │   └── subfolders...                (Regular tile textures go here)
│   │
│   ├── objects/
│   │   └── subfolders...                (Object textures go here)
│   │
│   ├── npcs/
│   │   └── subfolders...                (NPC textures go here)
│   │
│   └── settings/                        (Configuration files are located here)
│       ├── default_assets_path.txt
│       ├── default_dark_mode.txt
│       ├── default_frame_ms_duration.txt
│       ├── default_grids.txt
│       ├── default_tile_size.txt
│       └── default_used_ids_list.txt
│
├── jdk-16.0.2/                          (Required for Windows)
├── jdk-macOS/                           (Required for macOS)
├── jdk-Linux/                           (Required for Linux/Mint)
│
├── TileMakerDOT.jar                     (The Universal Application Core)
│
├── TileMakerLauncher.exe                (Windows Launcher)
├── TileMakerDOT_macOS_Launcher.command  (macOS Launcher)
└── TileMakerDOT_Linux_Launcher.sh       (Linux Launcher)
```

***

## 💻 Building from Source

If you want to modify the code or contribute to TileMaker DOT, follow these steps to set up the workspace.

### Prerequisites
* **Java Development Kit (JDK):** Version 16 or higher.
* **IDE:** Eclipse (my personal choice), IntelliJ IDEA, or VS Code.

### IDE Setup (Manual Dependency Management)
This project manages dependencies manually rather than using a build tool like Maven or Gradle. You must link the required UI library to your build path.

**Eclipse Instructions:**
1. Clone the repository to your local machine
2. Open Eclipse and navigate to `File` > `Open Projects from File System` > `Directory`
3. Select the cloned repository folder
4. Add the FlatLaf UI library to your classpath:
   * Right-click the project in the Package Explorer > `Build Path` > `Configure Build Path...`
   * Go to the **Libraries** tab.
   * Select **Classpath** > **Add JARs...** 
   * Navigate to the project's `lib/` folder and select `flatlaf-3.5.4.jar`.
5. Run the main class to launch the editor.

***

## 📺 Learning Resources

If you prefer visual learning, check out the official TileMaker DOT tutorial series on YouTube. These videos cover everything from basic placement to advanced autotiling and export workflows.

* [**Part 1: Getting Started with TileMaker DOT**](https://www.youtube.com/watch?v=Y0J-ezoVUCw&list=PLmIeW9QZsW_M4BuJoOmxTR5y6rK-N7W3D)
* [**Part 2: World Building & Performance**](https://www.youtube.com/watch?v=atrQ6VdNxC0&list=PLmIeW9QZsW_M4BuJoOmxTR5y6rK-N7W3D&index=2)
* [**Part 3: DarkMode & Dynamic Brushes**](https://www.youtube.com/watch?v=3fiajGU32Jg&list=PLmIeW9QZsW_M4BuJoOmxTR5y6rK-N7W3D&index=3)

***

## ❓ Support & Feedback

For detailed explanations of every button and shortcut, please refer to the **Help** tab located within the application's interface. If you encounter bugs or have feature requests, please reach out via the Itch.io community page or open an issue in this repository.
