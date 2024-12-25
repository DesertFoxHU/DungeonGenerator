package me.desertfox.dgen.shard.gens;

import me.desertfox.dgen.shard.ShardGenerator;
import me.desertfox.dgen.shard.DungeonShard;
import org.bukkit.Location;

public class VoidGenerator extends ShardGenerator {

    public VoidGenerator(DungeonShard chunk) {
        super(chunk);
        roomPool.clear();
    }

    @Override
    public void begin(Location start, Object... params) {

    }
}