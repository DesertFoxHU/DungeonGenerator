package me.desertfox.dgen.chunk.gens;

import me.desertfox.dgen.Direction4;
import me.desertfox.dgen.chunk.ChunkGenerator;
import me.desertfox.dgen.chunk.DungeonShard;
import me.desertfox.dgen.room.AbstractRoom;
import me.desertfox.dgen.room.RoomSchematic;
import me.desertfox.gl.Commons;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MazeGenerator extends ChunkGenerator {

    public MazeGenerator(DungeonShard chunk) {
        super(chunk);
        roomPool.remove("garage_W");
    }

    public List<Location> array = new ArrayList<>();
    @Override
    public void begin(Location start) {
        AbstractRoom room = getShard().getDungeon().safeBuild("corridor_SE", start);
        array.add(start.clone().add(Direction4.EAST.vector.getX() * (room.getRegion().getSizeX()+1), 0, Direction4.EAST.vector.getZ() * room.getRegion().getSizeZ()));
        array.add(start.clone().add(Direction4.SOUTH.vector.getX() * (room.getRegion().getSizeX()+1), 0, Direction4.SOUTH.vector.getZ() * room.getRegion().getSizeZ()));
        new BukkitRunnable(){
            @Override
            public void run() {
                if(array.isEmpty()) {
                    Bukkit.getLogger().info("[Dungeon1Generator] finished the sequence");
                    this.cancel();
                    return;
                }

                Location next = array.get(0);
                array.remove(0);

                call(next);
            }
        }.runTaskTimer(getShard().getDungeon().getPlugin(), 0, 20L);
    }

    private void call(Location start){
        List<Direction4> doors = new ArrayList<>();
        for(Direction4 dir4 : Direction4.values()){
            Direction4 flip = Direction4.flip(dir4);
            AbstractRoom aroom = getShard().getNeighbor(start, dir4);
            if(aroom == null) continue;
            if(aroom.getDoors().contains(flip)){
                doors.add(flip);
            }
        }

        int missing = Direction4.values().length - doors.size();
        if(missing > 0){
            List<Direction4> missingDoors = new ArrayList<>();
            for(Direction4 dir4 : Direction4.values()) if(!doors.contains(dir4)) missingDoors.add(dir4);

            for(int i = 0; i < missing; i++){
                if(Commons.roll(40)){
                    doors.add(missingDoors.get(new Random().nextInt(missingDoors.size())));
                }
            }
        }

        List<RoomSchematic> schemas = RoomSchematic.findByExactDoors(roomPool, doors.toArray(new Direction4[0]));
        if(schemas.isEmpty()){
            Bukkit.getLogger().info("Room is empty with:");
            for(Direction4 dir : doors.toArray(new Direction4[0])){
                Bukkit.getLogger().info("" + dir);
            }
            Bukkit.getLogger().info("END");
            getShard().getDungeon().safeBuild("wall_4x4", start);
            return;
        }
        AbstractRoom room = getShard().getDungeon().safeBuild(schemas.get(new Random().nextInt(schemas.size())).getSchematicName(), start);
        if(room != null){
            for(Direction4 future : doors){
                array.add(start.clone().add(future.vector.getX() * (room.getRegion().getSizeX()+1), 0, future.vector.getZ() * room.getRegion().getSizeZ()));
            }
        }
    }
}