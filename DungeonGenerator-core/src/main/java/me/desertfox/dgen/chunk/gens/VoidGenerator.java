package me.desertfox.dgen.chunk.gens;

import me.desertfox.dgen.chunk.ChunkGenerator;
import me.desertfox.dgen.chunk.DungeonChunk;
import org.bukkit.Location;

import java.util.Random;

public class VoidGenerator extends ChunkGenerator {

    public VoidGenerator(DungeonChunk chunk) {
        super(chunk);
        roomPool.clear();
    }

    @Override
    public void begin(Location start) {

    }
}