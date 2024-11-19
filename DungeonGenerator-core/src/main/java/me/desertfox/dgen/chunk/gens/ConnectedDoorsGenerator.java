package me.desertfox.dgen.chunk.gens;

import me.desertfox.dgen.Direction4;
import me.desertfox.dgen.chunk.ChunkGenerator;
import me.desertfox.dgen.chunk.DungeonChunk;
import me.desertfox.dgen.room.ActiveRoom;
import me.desertfox.dgen.room.RoomSchematic;
import me.desertfox.gl.Commons;
import net.minecraft.util.Tuple;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ConnectedDoorsGenerator extends ChunkGenerator {

    public ConnectedDoorsGenerator(DungeonChunk chunk) {
        super(chunk);
        roomPool.clear();
        roomPool.add("corridor_WESN");
        roomPool.add("corridor_SN");
        roomPool.add("corridor_WE");
        roomPool.add("wall_4x4");
        roomPool.add("garage_W");
    }

    public List<Tuple<Direction4, Location>> array = new ArrayList<>();
    @Override
    public void begin(Location start) {
        call("corridor_WESN", start);
        new BukkitRunnable(){
            @Override
            public void run() {
                if(array.isEmpty()) {
                    Bukkit.getLogger().info("[ConnectedDoorsGenerator] finished the sequence");
                    this.cancel();
                    return;
                }

                Tuple<Direction4, Location> key = array.get(0);
                Bukkit.getLogger().info("[Processor] " + key.a() + " from {" + key.b().getBlockX() + ";" + key.b().getBlockZ() + "}");
                array.remove(0);

                Direction4 flip = Direction4.flip(key.a());
                Bukkit.getLogger().info("[Processor] Looking for a schematic with door facing: " + flip);
                List<RoomSchematic> possibilities = RoomSchematic.findByDoors(flip);
                Bukkit.getLogger().info("[Processor] Found: " + possibilities.size());
                if(possibilities.isEmpty()) return;

                RoomSchematic chosen = possibilities.get(new Random().nextInt(possibilities.size()));
                if(Commons.roll(23)) chosen = RoomSchematic.findByName("wall_4x4");
                call(chosen.getSchematicName(), key.b());
            }
        }.runTaskTimer(getChunk().getDungeon().getPlugin(), 0, 2L);
    }

    private void call(String schematicName, Location start){
        ActiveRoom room = safeBuild(schematicName, start);
        if(room != null){
            for(Direction4 dir4 : RoomSchematic.findByName(room.getSchematicName()).getDoors()){
                Location l = start.clone().add(dir4.vector.getX() * (room.getRegion().getSizeX()+1), 0, dir4.vector.getZ() * room.getRegion().getSizeZ());
                Bukkit.getLogger().info("Adding to array {" + dir4 + ": " + l.getBlockX() + ";" + l.getBlockY() + ";" + l.getBlockZ() + "}");
                array.add(new Tuple<>(dir4, l));
            }
        }
    }
}