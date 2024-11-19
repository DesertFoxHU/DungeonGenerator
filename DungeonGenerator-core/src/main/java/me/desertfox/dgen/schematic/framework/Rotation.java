package me.desertfox.dgen.schematic.framework;

import org.bukkit.Axis;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.Orientable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Rotation {

    public enum Direction {
        CLOCKWISE_90,
        CLOCKWISE_180,
        CLOCKWISE_270;

        private static List<BlockFace> faces = new ArrayList<>(Arrays.asList(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST));

        public static Direction getByDegree(int degree){
            switch (degree) {
                case 90 -> { return CLOCKWISE_90; }
                case 180 -> { return CLOCKWISE_180; }
                case 270 -> { return CLOCKWISE_270; }
            }
            return null;
        }

        /**
         * Létrehoz egy olyan Vector-t ami a kezdőpontot<br>
         * csúsztatja, hogy a forgatott objektumnak<br>
         * ugyanott legyen a kezdőpontja<br>
         * @return
         */
        public static Vector shiftBack(int rotatedBy){
            if(rotatedBy == 90) return new Vector(4, 0, 0);
            else if(rotatedBy == 180) return new Vector(4, 0, 4);
            else if(rotatedBy == 270) return new Vector(0, 0, 4);
            return new Vector(0,0,0);
        }


        public static BlockFace rotate(BlockFace face, Direction direction){
            switch(direction){
                case CLOCKWISE_90 -> {
                    int index = faces.indexOf(face);
                    index++;
                    if(index >= faces.size()) index = 0;
                    return faces.get(index);
                }
                case CLOCKWISE_180 -> {
                    return face.getOppositeFace();
                }
                case CLOCKWISE_270 -> {
                    int index = faces.indexOf(face);
                    index--;
                    if(index < 0) index = faces.size()-1;
                    return faces.get(index);
                }
            }
            return face;
        }
    }

    public static BlockData rotateBlockData(BlockData data, Direction direction){
        if(data instanceof MultipleFacing facing){
            if(direction != Direction.CLOCKWISE_180) {
                List<BlockFace> origin = new ArrayList<>(facing.getFaces());
                for(BlockFace face : facing.getFaces()){
                    facing.setFace(Direction.rotate(face, direction), true);
                }
                origin.forEach(f -> facing.setFace(f, false));
            }
        }
        if(data instanceof Orientable ori){
            //Ha 180 fokos akkor nem forgatunk semmit hisz tükrözéssel is ugyanúgy néz ki
            switch(direction){
                case CLOCKWISE_90, CLOCKWISE_270 -> {
                    Axis axis = ori.getAxis();
                    if(axis == Axis.X) axis = Axis.Z;
                    else if(axis == Axis.Z) axis = Axis.X;
                    ori.setAxis(axis);
                }
            }
        }
        return data;
    }

    /**
     * A bemenő érték legyen relatív a forgatási ponthoz!<br>
     *
     * @param relative
     * @param rotation
     * @return
     */
    public static Vector rotateRelative(Vector relative, Direction rotation){
        switch(rotation) {
            case CLOCKWISE_90 -> {
                double x = 0 * relative.getX() - 1 * relative.getZ();
                double z = 1 * relative.getX() + 0 * relative.getZ();
                relative = new Vector(x, relative.getY(), z);
            }
            case CLOCKWISE_180 -> {
                double x = -1 * relative.getX();
                double z = -1 * relative.getZ();
                relative = new Vector(x, relative.getY(), z);
            }
            case CLOCKWISE_270 -> {
                double x = 0 * relative.getX() + 1 * relative.getZ();
                double z = -1 * relative.getX() + 0 * relative.getZ();
                relative = new Vector(x, relative.getY(), z);
            }
        }
        return relative;
    }

}