package supersymmetry.api.util;

import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.util.RelativeDirection;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a 3D grid for drawing a multiblock pattern. The origin point (0, 0, 0) is
 * at the left, bottom, front corner.
 */
public class Grid3D
{
    private final char[][][] grid;
    final int width;
    final int height;
    final int depth;
    private final Map<Character, TraceabilityPredicate> symbolMap = new HashMap<>();

    /**
     * Initializes the grid with the given dimensions and sets all blocks to a space character.
     *
     * @param width X dimension, left-right
     * @param height Y dimension, bottom-top
     * @param depth Z dimension, front-back
     */
    public Grid3D(int width, int height, int depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;

        grid = new char[width][height][depth];
        for ( int x = 0; x < width; x++ ) {
            for ( int y = 0; y < height; y++ ) {
                Arrays.fill(grid[x][y], ' ');
            }
        }
    }

    public Grid3D set(int x, int y, int z, char c) {
        if ( y < 0 ) {
            y += height;
        }
        if ( x < 0 ) {
            x += width;
        }
        if ( z < 0 ) {
            z += depth;
        }
        grid[x][y][z] = c;
        return this;
    }

    public Grid3D where(char symbol, TraceabilityPredicate blockMatcher) {
        this.symbolMap.put(symbol, new TraceabilityPredicate(blockMatcher).sort());
        return this;
    }

    /**
     * Draws a line on the X axis.
     *
     * @param x1
     * @param y
     * @param z
     * @param x2
     * @param c
     * @return
     */
    public Grid3D lineX(int x1, int y, int z, int x2, char c) {
        if ( x1 < 0 ) {
            x1 += width;
        }
        if ( x2 < 0 ) {
            x2 += width;
        }
        if ( y < 0 ) {
            y += height;
        }
        if ( z < 0 ) {
            z += depth;
        }
        if ( x1 > x2 ) {
            int t = x2;
            x2 = x1;
            x1 = t;
        }
        for ( int x = x1; x <= x2; x++ ) {
            grid[x][y][z] = c;
        }
        return this;
    }

    /**
     * Draws a line on the Z axis.
     *
     * @param x
     * @param y
     * @param z1
     * @param z2
     * @param c
     * @return
     */
    public Grid3D lineZ(int x, int y, int z1, int z2, char c) {
        if ( z1 < 0 ) {
            z1 += depth;
        }
        if ( z2 < 0 ) {
            z2 += depth;
        }
        if ( x < 0 ) {
            x += width;
        }
        if ( y < 0 ) {
            y += height;
        }
        if ( z1 > z2 ) {
            int t = z2;
            z2 = z1;
            z1 = t;
        }
        for ( int z = z1; z <= z2; z++ ) {
            grid[x][y][z] = c;
        }
        return this;
    }

    /**
     * Draws a line on the Y axis.
     *
     * @param x
     * @param y1
     * @param z
     * @param y2
     * @param c
     * @return
     */
    public Grid3D lineY(int x, int y1, int z, int y2, char c) {
        if ( y1 < 0 ) {
            y1 += height;
        }
        if ( y2 < 0 ) {
            y2 += height;
        }
        if ( x < 0 ) {
            x += width;
        }
        if ( z < 0 ) {
            z += depth;
        }
        if ( y1 > y2 ) {
            int t = y2;
            y2 = y1;
            y1 = t;
        }
        for ( int y = y1; y <= y2; y++ ) {
            grid[x][y][z] = c;
        }
        return this;
    }

    public Grid3D lineX(int x, int y, int z, String chars) {
        if ( x < 0 ) {
            x += width;
        }
        if ( y < 0 ) {
            y += height;
        }
        if ( z < 0 ) {
            z += depth;
        }
        for ( int i = x; i < x + chars.length() && i < width; i++ ) {
            grid[i][y][z] = chars.charAt(i - x);
        }
        return this;
    }

    public Grid3D lineY(int x, int y, int z, String chars) {
        if ( x < 0 ) {
            x += width;
        }
        if ( y < 0 ) {
            y += height;
        }
        if ( z < 0 ) {
            z += depth;
        }
        for ( int i = y; i < y + chars.length() && i < height; i++ ) {
            grid[x][i][z] = chars.charAt(i - y);
        }
        return this;
    }

    public Grid3D lineZ(int x, int y, int z, String chars) {
        if ( x < 0 ) {
            x += width;
        }
        if ( y < 0 ) {
            y += height;
        }
        if ( z < 0 ) {
            z += depth;
        }
        for ( int i = z; i < z + chars.length() && i < depth; i++ ) {
            grid[x][y][i] = chars.charAt(i - z);
        }
        return this;
    }

    public Grid3D rectXZ(int y, int x1, int z1, int x2, int z2, char c) {
        lineX(x1, y, z1, x2, c);
        lineX(x1, y, z2, x2, c);
        lineZ(x1, y, z1, z2, c);
        lineZ(x2, y, z1, z2, c);
        return this;
    }

    public BlockPattern build() {
        // characters in string are front-to-back (z direction)
        // strings in aisle() are bottom-to-top (y direction)
        // each aisle() call goes from left-to-right (x direction)
        var pattern = FactoryBlockPattern.start(RelativeDirection.FRONT, RelativeDirection.UP, RelativeDirection.RIGHT);
        for ( int x = 0; x < grid.length; x++ ) {
            String[] yzPlane = new String[grid[x].length];
            for ( int y = 0; y < grid[x].length; y++ ) {
                yzPlane[y] = String.valueOf(grid[x][y]);
            }

            pattern.aisle(yzPlane);
        }
        this.symbolMap.forEach(pattern::where);

        return pattern.build();
    }
}
