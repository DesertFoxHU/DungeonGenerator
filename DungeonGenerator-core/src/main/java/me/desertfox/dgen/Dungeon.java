package me.desertfox.dgen;

import lombok.Getter;
import me.desertfox.dgen.chunk.ChunkGenerator;
import me.desertfox.dgen.chunk.DungeonChunk;
import me.desertfox.dgen.chunk.gens.VoidGenerator;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Dungeon {

    @Getter private static final List<Dungeon> dungeons = new ArrayList<>();

    public static @Nullable Dungeon findByID(String id){
        return dungeons.stream().filter(x -> x.id.equalsIgnoreCase(id)).findFirst().orElse(null);
    }

    public static class Builder {
        private final JavaPlugin plugin;
        private final String id;
        private final Location start;
        private Location end;
        public int CHUNK_SIZE_X = 32;
        public int CHUNK_SIZE_Z = 32;
        public int MIN_ROOM_SIZE_XZ = 4;

        public Builder(JavaPlugin plugin, String id, Location start, int sizeX, int sizeY, int sizeZ){
            this.plugin = plugin;
            this.id = id;
            this.start = start.clone();
            this.end = start.clone().add(sizeX, sizeY, sizeZ);
        }

        public Builder(JavaPlugin plugin, String id, Location start, Location end){
            this.plugin = plugin;
            this.id = id;
            this.start = start.clone();
            this.end = end.clone();
        }

        public Dungeon.Builder chunkSizeX(int val){
            this.CHUNK_SIZE_X = val;
            return this;
        }

        public Dungeon.Builder chunkSizeZ(int val){
            this.CHUNK_SIZE_Z = val;
            return this;
        }

        /**
         * @param val How big the smallest room is? The default value is 4x4
         */
        public Dungeon.Builder setMinRoomSizeXZ(int val){
            val = Math.abs(val);
            assert(val > 0);
            this.MIN_ROOM_SIZE_XZ = val;
            return this;
        }

        public Dungeon build(){
            return new Dungeon(plugin, id, start, end, CHUNK_SIZE_X, CHUNK_SIZE_Z, MIN_ROOM_SIZE_XZ);
        }
    }

    @Getter private final JavaPlugin plugin;
    @Getter private final String id;
    @Getter private final Location start;
    @Getter private final Location end;
    @Getter private DungeonChunk[][] cells;
    @Getter private int chunkCountX = 0;
    @Getter private int chunkCountZ = 0;
    public int MAX_CHUNK_BATCH = 4;

    public final int MIN_ROOM_SIZE_XZ;

    /**
     * A Chunk's size in X,Z dimensions
     */
    private int CHUNKS_SIZE_X = 32;
    private int CHUNKS_SIZE_Z = 32;

    private final Queue<DungeonChunk> chunkQueue = new LinkedList<>();

    protected Dungeon(JavaPlugin plugin, String id, Location start, Location end, int CHUNKS_SIZE_X, int CHUNKS_SIZE_Z, int MIN_ROOM_SIZE_XZ) {
        this.plugin = plugin;
        this.id = id;
        this.start = start;
        this.end = end;
        this.MIN_ROOM_SIZE_XZ = MIN_ROOM_SIZE_XZ;
        this.CHUNKS_SIZE_X = CHUNKS_SIZE_X;
        this.CHUNKS_SIZE_Z = CHUNKS_SIZE_Z;

        chunkCountX = (int) Math.ceil(Math.abs((double)end.subtract(start).getBlockX()/CHUNKS_SIZE_X));
        chunkCountZ = (int) Math.ceil(Math.abs((double)end.subtract(start).getBlockZ()/CHUNKS_SIZE_Z));

        cells = new DungeonChunk[chunkCountX][chunkCountZ];

        setupCells();
        dungeons.add(this);

        new BukkitRunnable() {
            final int chunksPerTick = MAX_CHUNK_BATCH;
            @Override
            public void run() {
                if (chunkQueue.isEmpty()) {
                    return;
                }

                for (int i = 0; i < chunksPerTick; i++) {
                    DungeonChunk chunk = chunkQueue.poll();
                    if (chunk != null) {
                        chunk.populate();
                    }
                }
            }
        }.runTaskTimer(plugin, 10L, 10L);
    }

    private void setupCells(){
        Location curr = start.clone().add(0, 0, 0);
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                cells[i][j] = new DungeonChunk(this, VoidGenerator.class,
                        i, j, curr.getBlockX(), curr.getBlockY(), curr.getBlockZ(),
                        curr.getBlockX() + CHUNKS_SIZE_X - 1, end.getBlockY(), curr.getBlockZ() + CHUNKS_SIZE_Z - 1);
                curr = curr.add(CHUNKS_SIZE_X, 0, 0);
            }
            curr.setX(start.getBlockX());
            curr = curr.add(0, 0, CHUNKS_SIZE_Z);
        }
    }

    public World getWorld() { return start.getWorld(); }

    public void generateAll(boolean debug, Class<? extends ChunkGenerator> generator){
        for (DungeonChunk[] cell : cells) {
            for(DungeonChunk chunk : cell){
                chunk.setDebug(debug);
                chunk.setGenerator(generator);
            }
            chunkQueue.addAll(Arrays.asList(cell));
        }
    }
}