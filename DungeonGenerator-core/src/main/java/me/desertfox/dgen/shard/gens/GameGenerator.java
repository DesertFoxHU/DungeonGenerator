package me.desertfox.dgen.shard.gens;

import me.desertfox.dgen.shard.ShardGenerator;
import me.desertfox.dgen.shard.DungeonShard;
import me.desertfox.dgen.room.AbstractRoom;
import me.desertfox.dgen.utils.Utils;
import org.bukkit.Location;

import java.util.Random;

public class GameGenerator extends ShardGenerator {

    long seed = new Random().nextLong();
    public GameGenerator(DungeonShard chunk) {
        super(chunk);
    }

    @Override
    public void begin(Location start, Object... params) {
        int numberOfRooms = new Random(seed).nextInt(8 - 3) + 3;
        for(int i = 0; i < numberOfRooms; i++){
            getDungeon().updateOnQueue((dg) -> {
                Location l = getDungeon().snapToGrid(Utils.getRandomPointInCircle(getShard().get2DCenter(), 32));
                AbstractRoom result = getDungeon().safeClaim("corridor_16x16", l);
                result.placeDown();
            });
        }
    }
}