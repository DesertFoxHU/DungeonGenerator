# DungeonGenerator

__DungeonGenerator__ is a library designed to facilitate random dungeon generation in **Minecraft: Java Edition**

## Project modules
- DungeonGenerator-core (Contains essential functions and the core framework)
- DungeonGenerator-spigot (A spigot plugin for demostration purposes)

## Key features (WIP)
- Dungeon generation (obviously)
- Chunk-based system: divide the dungeon into manageable chunks (known as DungeonChunk, referred to as chunks) 
- Throttled chunk generation for better performance
- Regenerate specific chunks at any time
- Custom algorithms or use the provided algorithms
- Room-based grid claims: a grid of rooms to easily access them
- Dynamic room connection: automatically update neighboring rooms and manage doors between them
- Own schematic format (independent of WorldEdit)
- Configurable grid, chunk sizes
- Support for larger rooms (e.g., a room spanning 8x4 in a 4x4 room-grid system)
- Predefined rooms: pre-generate specific rooms, such as boss rooms, in designated locations.

## Library usage
(WIP)
