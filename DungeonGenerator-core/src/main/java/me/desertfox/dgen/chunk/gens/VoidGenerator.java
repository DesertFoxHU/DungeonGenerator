package me.desertfox.dgen.chunk.gens;

import me.desertfox.dgen.chunk.ChunkGenerator;
import me.desertfox.dgen.chunk.DungeonShard;
import org.bukkit.Location;

public class VoidGenerator extends ChunkGenerator {

    public VoidGenerator(DungeonShard chunk) {
        super(chunk);
        roomPool.clear();
    }

    @Override
    public void begin(Location start) {

    }
}