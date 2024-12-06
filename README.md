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
### Please note this is under heavy development, don't use in production

First initialize the SchematicController.class then load all the saved schematics there:
```java
import me.desertfox.dgen.schematic.framework.SchematicController;
import me.desertfox.dgen.room.RoomSchematic;

private static final String SCHEM_DIR = "schematics";

public void onEnable(){
  SchematicController.init(this, SCHEM_DIR);
  RoomSchematic.init(this, SCHEM_DIR);
}
```

To create your first dungeon use:
```java
... onEnable(){
  Location dungeonStart = new Location(world, 0, 100, 0);
  int size = 200;
  Dungeon dungeon = new Dungeon.Builder(this, "yourId", dungeonStart, size, size, size).build();
}
```

To generate all of your dungeon:
```java
... onEnable(){
  Dungeon dungeon = Dungeon.findById("yourId");
  dungeon.generateAll(isDebug, ChunkGenerator.class); //Replace ChunkGenerator.class with your generator class or choose one from the (me.desertfox.dgen.chunk.gens) directory
}
```

You can find more examples and an initial starting point in **DungeonGenerator-spigot** module.

## How to setup a schematic?
Initially the library can't create or delete schematics, but there is an already written controller in **DungeonGenerator-spigot** "SchematicCommand"<br>

<br>
You can set up doors by placing **Jigsaw** blocks pointing outwards<br>
![Képernyőkép 2024-12-06 235323](https://github.com/user-attachments/assets/c2f2b439-fd43-48ad-b5af-f2473b29281b)

This will save the doors direction to the schematic<br>
Schematics doesn't have physical representation of their doors only the direction (used for room's connection)<br>
So in this case the jigsaw will tell the system "there is a possibility to connect a room there"<br>

If you use the SchematicCommand class:
- It works the same way like a WorldEdit
- Set the first block by looking at a block and type /schema setPos1 then the second one by /schema setPos2
- Type /schema create [Name]

## How it works?
When you make a new dungeon instance the system will generate **DungeonShards** by your configuration<br>
every Shard is like a chunk in minecraft, it helps manage bigger dungeons for the cost of higher memory usage.<br>
<br>
By default a shard is 32x32 block (but you can configure this in the Dungeon.Builder class), shards stored grid-like in the Dungeon.class<br>
and the Shard stores the rooms also in grid-like solution<br>
<br>
The shards' room grid will have (SHARD_SIZE_XZ / ROOM_MIN_SIZE_XZ) count, the rooms' smallest size is 4x4 by default (it can be overwritten)
<br>

