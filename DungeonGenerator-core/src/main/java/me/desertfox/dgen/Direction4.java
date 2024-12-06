package me.desertfox.dgen;

import org.bukkit.block.data.type.Jigsaw;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public enum Direction4 {
    NORTH('N', new Vector(0, 0, -1)),
    EAST('E', new Vector(1, 0, 0)),
    SOUTH('S', new Vector(0, 0, 1)),
    WEST('W', new Vector(-1, 0, 0));

    public final char prefix;
    public final Vector vector;

    Direction4(char prefix, Vector vector) {
        this.prefix = prefix;
        this.vector = vector;
    }

    public static @Nullable Direction4 getByChar(char c){
        return Arrays.stream(Direction4.values()).filter(x -> x.prefix == c).findFirst().orElse(null);
    }

    public static Direction4 flip(Direction4 dir){
        if(dir == Direction4.NORTH) return Direction4.SOUTH;
        if(dir == Direction4.SOUTH) return Direction4.NORTH;
        if(dir == Direction4.WEST) return Direction4.EAST;
        else if(dir == Direction4.EAST) return Direction4.WEST;
        return Direction4.NORTH;
    }

    public static Direction4 convertFrom(Jigsaw.Orientation orientation){
        return switch (orientation) {
            case DOWN_EAST, UP_EAST, EAST_UP -> Direction4.EAST;
            case DOWN_NORTH, UP_NORTH, NORTH_UP -> Direction4.NORTH;
            case DOWN_SOUTH, UP_SOUTH, SOUTH_UP -> Direction4.SOUTH;
            case DOWN_WEST, UP_WEST, WEST_UP -> Direction4.WEST;
            default -> throw new IllegalArgumentException("Unsupported orientation: " + orientation);
        };
    }
}