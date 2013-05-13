/*
 * Copyright 2013 Netherlands eScience Center
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.esciencecenter.esalsa.util;

/**
 * Neighbours is a Java port of the neighborhood functions used in the Fortran version of POP.
 * 
 * It can be used to determine the neighbors for a given block and the communication costs for these neighbors. Like the original
 * Fortran versions, the neighbor and communication functions support CLOSED, CYCLIC and TRIPOLE wrapping of coordinates.
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 * 
 */
public class Neighbours {

    /** The width of the HALO used in communication */
    public static final int HALOWIDTH = 2;

    /** Constant used to indicate TRIPOLE wrapping of the y coordinate. */
    public static final int TRIPOLE = 0;

    /** Constant used to indicate CYCLIC wrapping of the x or y coordinate. */
    public static final int CYCLIC = 1;

    /** Constant used to indicate CLOSED wrapping (i.e., no wrapping) of the x or y coordinate. */
    public static final int CLOSED = 2;

    /** The selected wrapping for the y coordinate (TRIPOLE, CYCLIC or CLOSED). */
    public final int boundaryV;

    /** The selected wrapping for the x coordinate (CYCLIC or CLOSED). */
    public final int boundaryW;

    /** The message size for messages sends to east or west neighbors. */
    public final int messageSizeEastWest;

    /** The message size for messages sends to north or south neighbors. */
    public final int messageSizeNorthSouth;

    /** The message size for messages sends to north east, north west, south east, south west neighbors. */
    public final int messageSizeCorner;

    /** The message size for messages sends to tripole neighbors. */
    public final int messageSizeTripole;

    /** The grid containing the blocks to use. */
    // private final Grid grid;
    private final int gridWidth;
    private final int gridHeight;

    
    private final int blockWidth;
    private final int blockHeight;

    private final Topography topo;
    
    public Neighbours(Topography topo, int gridWidth, int gridHeight, int blockWidth, int blockHeight, int boundaryW, int boundaryV) {

        // Ensure the boundary wrapping settings are correct. 
        if (!(boundaryW == CYCLIC || boundaryW == CLOSED)) {
            throw new IllegalArgumentException("Illegal boundaryW " + boundaryW);
        }

        if (boundaryV < 0 || boundaryV > CLOSED) {
            throw new IllegalArgumentException("Illegal boundaryV " + boundaryV);
        }
        
        this.topo = topo;
        
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;

        this.blockWidth = blockWidth;
        this.blockHeight = blockHeight;
        
        this.boundaryW = boundaryW;
        this.boundaryV = boundaryV;

        // Compute the various message sizes. 
        this.messageSizeEastWest = blockHeight * HALOWIDTH;
        this.messageSizeNorthSouth = blockWidth * HALOWIDTH;
        this.messageSizeCorner = HALOWIDTH * HALOWIDTH;
        this.messageSizeTripole = blockWidth * (HALOWIDTH + 1);
    }

    public static int parseBoundaryX(String value) { 
        
        if (value != null) {
            
            if (value.equalsIgnoreCase("CLOSED")) { 
                return CLOSED;
            }
            
            if (value.equalsIgnoreCase("CYCLIC")) { 
                return CYCLIC;
            }
        }    
            
        return -1;
    }
    
    public static int parseBoundaryY(String value) { 

        if (value != null) {

            if (value.equalsIgnoreCase("CLOSED")) { 
                return CLOSED;
            }

            if (value.equalsIgnoreCase("CYCLIC")) { 
                return CYCLIC;
            }

            if (value.equalsIgnoreCase("TRIPOLE")) { 
                return TRIPOLE;
            }
        }    

        return -1;
    }

    public static String [] getValidBoundaryX() { 
        return new String [] { "CLOSED", "CYCLIC" };
    }

    public static String [] getValidBoundaryY() { 
        return new String [] { "CLOSED", "CYCLIC", "TRIPOLE" };
    }
    
    public boolean isOcean(int x, int y) {
        return topo.getRectangleWork(x * blockWidth, y * blockHeight, blockWidth, blockHeight) > 0;
    }
    
    public boolean isOcean(Coordinate c) { 
        return isOcean(c.x, c.y);
    }
    
    /**
     * Retrieve the north neighbor of the given source coordinate.
     * 
     * The result will be null if:
     * 
     * - {@code includeLand} is set to false and the neighbor is a land only block. - {@code boundaryV} is set to CLOSED and the
     * source coordinate is at the top of the grid (y=0).
     * 
     * @param c
     *            the source coordinate.
     * @param includeLand
     *            should land only coordinates be returned ?
     * @return the coordinate of the north neighbor of the source, or null if no valid neighbor exists.
     */
    public int getNeighbourNorth(Coordinate c) {

        int x = c.x;
        int y = c.y + 1;

        if (y >= gridHeight) {
            switch (boundaryV) {
            case CLOSED:
                return 0;
            case CYCLIC:
                y = 0;
                break;
            case TRIPOLE:
                // POP_numBlocksX - iBlock + 1 				
                x = (gridWidth - (c.x + 1) + 1) - 1;
                y = c.y;
                break;
            }
        }

       return y*gridWidth + x + 1;
    }
    
    /**
     * Retrieve the south neighbor for the given source coordinate.
     * 
     * The result will be null if:
     * 
     * - {@code includeLand} is set to false and the neighbor is a land only block. - {@code boundaryV} is set to CLOSED or
     * TRIPOLE and source coordinate is at the bottom of the grid (y=gridHeight-1).
     * 
     * @param c
     *            the source coordinate.
     * @param includeLand
     *            should land only coordinates be returned ?
     * @return the coordinate of the south neighbor of the source, or null if no valid neighbor exists.
     */
    public int getNeighbourSouth(Coordinate c) {

        int x = c.x;
        int y = c.y - 1;

        if (y < 0) {
            switch (boundaryV) {
            case CLOSED:
            case TRIPOLE:
                return 0;
            case CYCLIC:
                y = gridHeight - 1;
                break;
            }
        }

       return y*gridWidth + x + 1;
    }

    /**
     * Retrieve the east neighbor for the given source coordinate.
     * 
     * The result will be null if:
     * 
     * - {@code includeLand} is set to false and the neighbor is a land only block. - {@code boundaryW} is set to CLOSED and
     * source coordinate is at the right edge of the grid (x=gridWidth-1).
     * 
     * @param c
     *            the source coordinate.
     * @param includeLand
     *            should land only coordinates be returned ?
     * @return the coordinate of the east neighbor of the source, or null if no valid neighbor exists.
     */
    public int getNeighbourEast(Coordinate c) {

        int x = c.x + 1;
        int y = c.y;

        if (x >= gridWidth) {
            switch (boundaryW) {
            case CLOSED:
                return 0;
            case CYCLIC:
                x = 0;
                break;
            }
        }

       return y*gridWidth + x + 1;
    }

    /**
     * Retrieve the west neighbor for the given source coordinate.
     * 
     * The result will be null if:
     * 
     * - {@code includeLand} is set to false and the neighbor is a land only block. - {@code boundaryW} is set to CLOSED and the
     * source coordinate is at the left edge of the grid (x=0).
     * 
     * @param c
     *            the source coordinate.
     * @param includeLand
     *            should land only coordinates be returned ?
     * @return the coordinate of the west neighbor of the source, or null if no valid neighbor exists.
     */
    public int getNeighbourWest(Coordinate c) {

        int x = c.x - 1;
        int y = c.y;

        if (x < 0) {
            switch (boundaryW) {
            case CLOSED:
                return 0;
            case CYCLIC:
                x = gridWidth - 1;
                break;
            }
        }

       return y*gridWidth + x + 1;
    }

    /**
     * Retrieve the north east neighbor for the given source coordinate.
     * 
     * The result will be null if:
     * 
     * - {@code includeLand} is set to false and the neighbor is a land only block. - {@code boundaryW} is set to CLOSED and the
     * source coordinate is at the right edge of the grid (x=gridWidth-1). - {@code boundaryV} is set to CLOSED and the source
     * coordinate is at the top of the grid (y=0).
     * 
     * @param c
     *            the source coordinate.
     * @param includeLand
     *            should land only coordinates be returned ?
     * @return the coordinate of the north east neighbor of the source, or null if no valid neighbor exists.
     */
    public int getNeighbourNorthEast(Coordinate c) {

        int x = c.x + 1;
        int y = c.y + 1;

        if (x >= gridWidth) {
            switch (boundaryW) {
            case CLOSED:
                return 0;
            case CYCLIC:
                x = 0;
                break;
            }
        }

        if (y >= gridHeight) {
            switch (boundaryV) {
            case CLOSED:
                return 0;
            case CYCLIC:
                y = 0;
                break;
            case TRIPOLE:
                // inbr =  POP_numBlocksX - iBlock 
                // if (inbr == 0) inbr = POP_numBlocksX
                // jnbr = -jBlock
                x = (gridWidth - (c.x + 1)) - 1;

                if (x < 0) {
                    x = gridWidth - 1;
                }

                y = c.y;
                break;
            }
        }

       return y*gridWidth + x + 1;
    }

    /**
     * Retrieve the north west neighbor for the given source coordinate.
     * 
     * The result will be null if:
     * 
     * - {@code includeLand} is set to false and the neighbor is a land only block. - {@code boundaryW} is set to CLOSED and the
     * source coordinate is at the left edge of the grid (x=0). - {@code boundaryV} is set to CLOSED and the source coordinate is
     * at the top of the grid (y=grid-height-1).
     * 
     * @param c
     *            the source coordinate.
     * @param includeLand
     *            should land only coordinates be returned ?
     * @return the coordinate of the north west neighbor of the source, or null if no valid neighbor exists.
     */
    public int getNeighbourNorthWest(Coordinate c) {

        int x = c.x - 1;
        int y = c.y + 1;

        if (x < 0) {
            switch (boundaryW) {
            case CLOSED:
                return 0;
            case CYCLIC:
                x = gridWidth - 1;
                break;
            }
        }

        if (y >= gridHeight) {
            switch (boundaryV) {
            case CLOSED:
                return 0;
            case CYCLIC:
                y = 0;
                break;
            case TRIPOLE:
                //inbr =  POP_numBlocksX - iBlock + 2 
                //if (inbr > POP_numBlocksX) inbr = 1
                //jnbr = -jBlock
                x = (gridWidth - (c.x + 1) + 2) - 1;

                if (x >= gridWidth) {
                    x = 0;
                }

                y = c.y;
                break;
            }
        }

       return y*gridWidth + x + 1;
    }

    /**
     * Retrieve the south east neighbor for the given source coordinate.
     * 
     * The result will be null if:
     * 
     * - {@code includeLand} is set to false and the neighbor is a land only block. - {@code boundaryW} is set to CLOSED and the
     * source coordinate is at the right edge of the grid (x=gridWidth-1). - {@code boundaryV} is set to CLOSED or TRIPOLE and the
     * source coordinate is at the bottom of the grid (y=0).
     * 
     * @param c
     *            the source coordinate.
     * @param includeLand
     *            should land only coordinates be returned ?
     * @return the coordinate of the south east neighbor of the source, or null if no valid neighbor exists.
     */
    public int getNeighbourSouthEast(Coordinate c) {

        int x = c.x + 1;
        int y = c.y - 1;

        if (x >= gridWidth) {
            switch (boundaryW) {
            case CLOSED:
                return 0;
            case CYCLIC:
                x = 0;
                break;
            }
        }

        if (y < 0) {
            switch (boundaryV) {
            case CLOSED:
            case TRIPOLE:
                return 0;
            case CYCLIC:
                y = gridHeight - 1;
                break;
            }
        }

       return y*gridWidth + x + 1;
    }

    /**
     * Retrieve the south west neighbor for the given source coordinate.
     * 
     * The result will be null if:
     * 
     * - {@code includeLand} is set to false and the neighbor is a land only block. - {@code boundaryW} is set to CLOSED and the
     * source coordinate is at the left edge of the grid (x=0). - {@code boundaryV} is set to CLOSED or TRIPOLE and the source
     * coordinate is at the bottom of the grid (y=0).
     * 
     * @param c
     *            the source coordinate.
     * @param includeLand
     *            should land only coordinates be returned ?
     * @return the coordinate of the south west neighbor of the source, or null if no valid neighbor exists.
     */
    public int getNeighbourSouthWest(Coordinate c) {

        int x = c.x - 1;
        int y = c.y - 1;

        if (x < 0) {
            switch (boundaryW) {
            case CLOSED:
                return 0;
            case CYCLIC:
                x = gridWidth - 1;
                break;
            }
        }

        if (y < 0) {
            switch (boundaryV) {
            case CLOSED:
            case TRIPOLE:
                return 0;
            case CYCLIC:
                y = gridHeight - 1;
                break;
            }
        }

       return y*gridWidth + x + 1;
    }

    /**
     * Retrieve a 3x3 matrix containing all neighbors for the given source coordinate.
     * 
     * The neighbors are stored in the matrix as follows:
     * 
     * 0 1 2 0 [NW, N, NE] 1 [ W, -, E ] 2 [SW, S, SE]
     * 
     * The middle entry (i.e., result[1][1]) is always null as it represents the source coordinate.
     * 
     * If the {@code boundaryW} is set to CLOSED and the source coordinate is at the left edge of the grid (x=0), the NW, W, and
     * SW entries will contain null.
     * 
     * If the {@code boundaryW} is set to CLOSED and the source coordinate is at the right edge of the grid (x=gridWidth-1), the
     * NE, E, and SE entries will contain null.
     * 
     * If the {@code boundaryV} setting is set to CLOSED and the source coordinate is at the top of the grid (y=gridHeight-1), the
     * NE, N, and NW entries will contain null.
     * 
     * If the {@code boundaryV} setting is set to CLOSED or TRIPOLE and the source coordinate is at the bottom of the grid (y=0),
     * the SE, S, and SW entries will contain null.
     * 
     * If {@code includeLand} is set to false, entries may be null if the neighbor represents a land-only block.
     * 
     * @param c
     *            the source coordinate.
     * @param includeLand
     *            should land only coordinates be returned ?
     * @return a 3x3 matrix containing all valid neighbors for the given source coordinate.
     */
    public int[][] getNeighbours(Coordinate c) {

        int[][] result = new int[3][3];

        result[0][0] = getNeighbourNorthWest(c);
        result[0][1] = getNeighbourNorth(c);
        result[0][2] = getNeighbourNorthEast(c);

        result[1][0] = getNeighbourWest(c);
        result[1][1] = 0;
        result[1][2] = getNeighbourEast(c);

        result[2][0] = getNeighbourSouthWest(c);
        result[2][1] = getNeighbourSouth(c);
        result[2][2] = getNeighbourSouthEast(c);

        return result;
    }

    /**
     * Retrieve the amount of communication needed to the north neighbor for the given source coordinate.
     * 
     * The result is 0 if:
     * 
     * - the neighbor is a land only block. - {@code boundaryV} is set to CLOSED and the source coordinate is at the top of the
     * grid (y=0).
     * 
     * @param c
     *            the source coordinate.
     * @return the amount of communication needed with the north neighbor of the source, or 0 if no valid neighbor exists.
     */
    public int getMessageSizeNorth(Coordinate c) {

        int x = c.x;
        int y = c.y + 1;

        int messageSize = messageSizeNorthSouth;

        if (y >= gridHeight) {
            switch (boundaryV) {
            case CLOSED:
                return 0;
            case CYCLIC:
                //y = 0;
                messageSize = messageSizeNorthSouth;
                break;
            case TRIPOLE:
                //x = (gridWidth - (c.x+1) + 1) - 1;
                //y = c.y;
                messageSize = messageSizeTripole;
                break;
            }
        }
        
        if (!isOcean(x, y)) { 
            return 0;
        }

        return messageSize;
    }

    /**
     * Retrieve the amount of communication needed to the south neighbor for the given source coordinate.
     * 
     * The result is 0 if:
     * 
     * - the neighbor is a land only block. - {@code boundaryV} is set to CLOSED or TRIPOLE and the source coordinate is at the
     * bottom of the grid (y=gridHeight-1).
     * 
     * @param c
     *            the source coordinate.
     * @return the amount of communication needed with the south neighbor of the source, or 0 if no valid neighbor exists.
     */
    public int getMessageSizeSouth(Coordinate c) {
        
        int x = c.x;
        int y = c.y - 1;

        if (y < 0) {
            switch (boundaryV) {
            case CLOSED:
            case TRIPOLE:
                return 0;
            case CYCLIC:
                y = gridHeight - 1;
                break;
            }
        }

        if (!isOcean(x, y)) { 
            return 0;
        }

        return messageSizeNorthSouth;
    }

    /**
     * Retrieve the amount of communication needed to the east neighbor for the given source coordinate.
     * 
     * The result is 0 if:
     * 
     * - the neighbor is a land only block. - {@code boundaryW} is set to CLOSED and the source coordinate is at the right edge of
     * the grid (x=gridWidth-1).
     * 
     * @param c
     *            the source coordinate.
     * @return the amount of communication needed with the east neighbor of the source, or 0 if no valid neighbor exists.
     */
    public int getMessageSizeEast(Coordinate c) {
        
        int x = c.x + 1;
        int y = c.y;

        if (x >= gridWidth) {
            switch (boundaryW) {
            case CLOSED:
                return 0;
            case CYCLIC:
                x = 0;
                break;
            }
        }
        
        if (!isOcean(x, y)) { 
            return 0;
        }
        
        return messageSizeEastWest;
    }

    /**
     * Retrieve the amount of communication needed to the west neighbor for the given source coordinate.
     * 
     * The result is 0 if:
     * 
     * - the neighbor is a land only block. - {@code boundaryW} is set to CLOSED and the source coordinate is at the left edge of
     * the grid (x=0).
     * 
     * @param c
     *            the source coordinate.
     * @return the amount of communication needed with the west neighbor of the source, or 0 if no valid neighbor exists.
     */
    public int getMessageSizeWest(Coordinate c) {
        
        int x = c.x - 1;
        int y = c.y;

        if (x < 0) {
            switch (boundaryW) {
            case CLOSED:
                return 0;
            case CYCLIC:
                x = gridWidth - 1;
                break;
            }
        }

        if (!isOcean(x, y)) { 
            return 0;
        }

        return messageSizeEastWest;
    }

    /**
     * Retrieve the amount of communication needed to the north east neighbor for the given source coordinate.
     * 
     * The result is 0 if:
     * 
     * - the neighbor is a land only block. - {@code boundaryW} setting is set to CLOSED and the source coordinate is at the right
     * edge of the grid (x=gridWidth-1). - {@code boundaryV} setting is set to CLOSED and the source coordinate is at the top of
     * the grid (y=0).
     * 
     * @param c
     *            the source coordinate.
     * @return the amount of communication needed with the north east neighbor of the source, or 0 if no valid neighbor exists.
     */
    public int getMessageSizeNorthEast(Coordinate c) {

        int x = c.x + 1;
        int y = c.y + 1;

        int messageSize = messageSizeCorner;

        if (x >= gridWidth) {
            switch (boundaryW) {
            case CLOSED:
                return 0;
            case CYCLIC:
                x = 0;
                break;
            }
        }

        if (y >= gridHeight) {
            switch (boundaryV) {
            case CLOSED:
                return 0;
            case CYCLIC:
                y = 0;
                break;
            case TRIPOLE:
                // inbr =  POP_numBlocksX - iBlock 
                // if (inbr == 0) inbr = POP_numBlocksX
                // jnbr = -jBlock
                x = (gridWidth - (c.x + 1)) - 1;

                if (x < 0) {
                    x = gridWidth - 1;
                }

                y = c.y;

                messageSize = messageSizeTripole;
                break;
            }
        }

        if (!isOcean(x, y)) { 
            return 0;
        }
        
        return messageSize;
    }

    /**
     * Retrieve the amount of communication needed to the north west neighbor for the given source coordinate.
     * 
     * The result is 0 if:
     * 
     * - the neighbor is a land only block. - {@code boundaryW} is set to CLOSED and the source coordinate is at the left edge of
     * the grid (x=0). - {@code boundaryV} setting is set to CLOSED and the source coordinate is at the top of the grid (y=0).
     * 
     * @param c
     *            the source coordinate.
     * @return the amount of communication needed with the north west neighbor of the source, or 0 if no valid neighbor exists.
     */
    public int getMessageSizeNorthWest(Coordinate c) {

        int x = c.x - 1;
        int y = c.y + 1;

        int messageSize = messageSizeCorner;

        if (x <= 0) {
            switch (boundaryW) {
            case CLOSED:
                return 0;
            case CYCLIC:
                x = gridWidth - 1;
                break;
            }
        }

        if (y >= gridHeight) {
            switch (boundaryV) {
            case CLOSED:
                return 0;
            case CYCLIC:
                y = 0;
                break;
            case TRIPOLE:
                //inbr =  POP_numBlocksX - iBlock + 2 
                //if (inbr > POP_numBlocksX) inbr = 1
                //jnbr = -jBlock
                x = (gridWidth - (c.x + 1) + 2) - 1;

                if (x >= gridWidth) {
                    x = 0;
                }

                y = c.y;
                messageSize = messageSizeTripole;
                break;
            }
        }
        
        if (!isOcean(x, y)) { 
            return 0;
        }

        return messageSize;
    }

    /**
     * Retrieve the amount of communication needed to the south east neighbor for the given source coordinate.
     * 
     * The result is 0 if:
     * 
     * - the neighbor is a land only block. - {@code boundaryW} setting is set to CLOSED and the source coordinate is at the right
     * edge of the grid (x=gridWidth-1). - {@code boundaryV} setting is set to CLOSED or TRIPOLE and the source coordinate is at
     * the bottom of the grid (y=0).
     * 
     * @param c
     *            the source coordinate.
     * @return the amount of communication needed with the south east neighbor of the source, or 0 if no valid neighbor exists.
     */
    public int getMessageSizeSouthEast(Coordinate c) {
        
        int x = c.x + 1;
        int y = c.y - 1;

        if (x >= gridWidth) {
            switch (boundaryW) {
            case CLOSED:
                return 0;
            case CYCLIC:
                x = 0;
                break;
            }
        }

        if (y < 0) {
            switch (boundaryV) {
            case CLOSED:
            case TRIPOLE:
                return 0;
            case CYCLIC:
                y = gridHeight - 1;
                break;
            }
        }
        
        if (!isOcean(x, y)) { 
            return 0;
        }
        
        return messageSizeCorner;
    }

    /**
     * Retrieve the amount of communication needed to the south west neighbor for the given source coordinate.
     * 
     * The result is 0 if:
     * 
     * - the neighbor is a land only block. - {@code boundaryW} setting is set to CLOSED and the source coordinate is at the left
     * edge of the grid (x=0). - {@code boundaryV} setting is set to CLOSED or TRIPOLE and the source coordinate is at the bottom
     * of the grid (y=0).
     * 
     * @param c
     *            the source coordinate.
     * @return the amount of communication needed with the south west neighbor of the source, or 0 if no valid neighbor exists.
     */
    public int getMessageSizeSouthWest(Coordinate c) {
        
        int x = c.x - 1;
        int y = c.y - 1;

        if (x < 0) {
            switch (boundaryW) {
            case CLOSED:
                return 0;
            case CYCLIC:
                x = gridWidth - 1;
                break;
            }
        }

        if (y < 0) {
            switch (boundaryV) {
            case CLOSED:
            case TRIPOLE:
                return 0;
            case CYCLIC:
                y = gridHeight - 1;
                break;
            }
        }
        
        if (!isOcean(x, y)) { 
            return 0;
        }
        
        return messageSizeCorner;
    }

    /**
     * Retrieve the amount of communication needed to a specified neighbor of the given source coordinate.
     * 
     * The neighbor can be selected by specifying the x and y coordinate offsets (dx, dy).
     * 
     * The result is 0 if:
     * 
     * - the selected neighbor is a land only block. - any of the offsets is larger than 1 or smaller than -1. - {@code boundaryW}
     * setting is set to CLOSED and the selected neighbor is not on the grid. - {@code boundaryV} setting is set to CLOSED and the
     * selected neighbor is not on the grid. - {@code boundaryV} setting is set to TRIPOLE and the selected neighbor is below the
     * lower edge of the grid.
     * 
     * @param c
     *            the source coordinate.
     * @param dx
     *            the x offset of the neighbor coordinate.
     * @param dy
     *            the y offset of the neighbor coordinate.
     * 
     * @return the amount of communication needed with the specified neighbor of the source, or 0 if no valid neighbor exists.
     */
    public int getCommunication(Coordinate c, int dx, int dy) {

        switch (dx) {
        case -1: {
            switch (dy) {
            case -1:
                return getMessageSizeNorthWest(c);
            case 0:
                return getMessageSizeWest(c);
            case 1:
                return getMessageSizeNorthEast(c);
            default:
                return 0;
            }
        }

        case 0: {
            switch (dy) {
            case -1:
                return getMessageSizeWest(c);
            case 0:
                return 0;
            case 1:
                return getMessageSizeEast(c);
            default:
                return 0;
            }
        }

        case 1: {
            switch (dy) {
            case -1:
                return getMessageSizeSouthWest(c);
            case 0:
                return getMessageSizeSouth(c);
            case 1:
                return getMessageSizeSouthEast(c);
            default:
                return 0;
            }
        }

        default:
            return 0;
        }
    }

    /**
     * Retrieve a 3x3 matrix containing the amount of communication needed for all neighbors for the given source coordinate.
     * 
     * The communication is stored in the matrix as follows:
     * 
     * 0 1 2 0 [NW, N, NE] 1 [ W, -, E ] 2 [SW, S, SE]
     * 
     * The middle entry (i.e., result[1][1]) is always 0 as it represents the source coordinate.
     * 
     * If the {@code boundaryW} is set to CLOSED and the source coordinate is at the left edge of the grid (x=0), the NW, W, and
     * SW entries will contain 0.
     * 
     * If the {@code boundaryW} is set to CLOSED and the source coordinate is at the right edge of the grid (x=gridWidth-1), the
     * NE, E, and SE entries will contain 0.
     * 
     * If the {@code boundaryV} setting is set to CLOSED and the source coordinate is at the top of the grid (y=gridHeight-1), the
     * NE, N, and NW entries will contain 0.
     * 
     * If the {@code boundaryV} setting is set to CLOSED or TRIPOLE and the source coordinate is at the bottom of the grid (y=0),
     * the SE, S, and SW entries will contain 0.
     * 
     * In addition, entries may be 0 if the neighbor represents a land-only block.
     * 
     * @param c
     *            the source coordinate.
     * @return a 3x3 matrix containing the amount of communication needed for each neighbor of the given source coordinate.
     */
    public int[][] getCommunication(Coordinate c) {

        int[][] result = new int[3][3];

        result[0][0] = getMessageSizeNorthWest(c);
        result[0][1] = getMessageSizeNorth(c);
        result[0][2] = getMessageSizeNorthEast(c);

        result[1][0] = getMessageSizeWest(c);
        result[1][1] = 0;
        result[1][2] = getMessageSizeEast(c);

        result[2][0] = getMessageSizeSouthWest(c);
        result[2][1] = getMessageSizeSouth(c);
        result[2][2] = getMessageSizeSouthEast(c);

        return result;
    }
}
