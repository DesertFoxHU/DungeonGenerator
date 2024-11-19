package me.desertfox.dgen;

import org.bukkit.util.Vector;

public enum Direction4 {
    NORTH("N", new Vector(0, 0, -1)),
    EAST("E", new Vector(1, 0, 0)),
    SOUTH("S", new Vector(0, 0, 1)),
    WEST("W", new Vector(-1, 0, 0));

    public final String prefix;
    public final Vector vector;

    Direction4(String prefix, Vector vector) {
        this.prefix = prefix;
        this.vector = vector;
    }

    public static Direction4 flip(Direction4 dir){
        if(dir == Direction4.NORTH) return Direction4.SOUTH;
        if(dir == Direction4.SOUTH) return Direction4.NORTH;
        if(dir == Direction4.WEST) return Direction4.EAST;
        else if(dir == Direction4.EAST) return Direction4.WEST;
        return Direction4.NORTH;
    }
}