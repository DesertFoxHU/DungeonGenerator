package me.desertfox.dgen.room;

import lombok.Getter;
import me.desertfox.dgen.utils.Cuboid;

public class ActiveRoom {

    @Getter private final String schematicName;
    @Getter private final Cuboid region;

    public ActiveRoom(String schematicName, Cuboid region) {
        this.schematicName = schematicName;
        this.region = region;
    }
}