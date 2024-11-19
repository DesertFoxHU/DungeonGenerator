package me.desertfox.dgen.utils;

import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.Location;

public class Utils {

    public static boolean isPowerOfTwo(int n) {
        return n > 0 && (n & (n - 1)) == 0;
    }



}