package me.desertfox.dgen.utils;

import org.bukkit.Location;

public class Utils {

    public static Location getRandomPointInCircle(Location location, double radius){
        double angle = 2 * Math.PI * Math.random();
        double u = Math.random();
        double r = radius * Math.sqrt(u);

        double xOffset = r * Math.cos(angle);
        double zOffset = r * Math.sin(angle);

        return location.clone().add(xOffset, 0, zOffset);
    }

}