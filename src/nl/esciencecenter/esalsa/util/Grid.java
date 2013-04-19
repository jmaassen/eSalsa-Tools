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

import java.util.ArrayList;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Grid represents an rectangular grid of Blocks.
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 * @see Block
 * 
 */
public class Grid implements Iterable<Block> {

    /** A Logger used for debugging. */
    private static final Logger logger = LoggerFactory.getLogger(Grid.class);

    /** The width of the grid. */
    public final int width;

    /** The height of the grid. */
    public final int height;

    /** The width of a block in topography points. */
    public final int blockWidth;

    /** The height of a block in topography points. */
    public final int blockHeight;

    /** An array of length width*height to store blocks in this grid. */
    private final Block[][] blocksByLocation;

    /** An array of length width*height to store blocks in this grid. */
    private final Block[] blocksByNumber;
    
    /** The number of active blocks in this grid. */
    private int count = 0;

    /**
     * Create grid by subdividing a Topography into blocks of size blockWidth x blockHeight points.
     * 
     * Only Blocks containing at least one ocean point will be stored. As a result, after creation, some locations in the grid may
     * not contain a block.
     * 
     * @param topo
     *            the Topography to divide.
     * @param blockWidth
     *            the width of a block in topography points.
     * @param blockHeight
     *            the height of a block in topography points.
     * @throws Exception
     *             if the block size does not divide the topography equally.
     * @see Topography
     * @see Block
     */
    public Grid(int width, int height, int blockWidth, int blockHeight, Neighbours neighbours) throws Exception {

        this.width = width;
        this.height = height;
        
        this.blockWidth = blockWidth;
        this.blockHeight = blockHeight;

        if (logger.isDebugEnabled()) {
            logger.debug("Creating new grid from topography " + width + "x" + height);
        }

        blocksByLocation = new Block[height][width];
        blocksByNumber = new Block[width * height + 1];
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Block b = new Block(y * width + x + 1, new Coordinate(x, y), neighbours);

                blocksByNumber[b.blockID] = b;
                blocksByLocation[y][x] = b;
            }
        }
        
        if (logger.isDebugEnabled()) {
            logger.debug("Created new grid from topography with " + getCount() + " active elements.");
        }
    }

    public Grid(Grid g, int additionalRows) {
        this.width = g.width;
        this.height = g.height + additionalRows;
        
        this.blockWidth = g.blockWidth;
        this.blockHeight = g.blockHeight;
        
        // NOTE; this clone does NOT copy the blocks (nor should it!)
        this.blocksByNumber = g.blocksByNumber.clone();
        
        this.blocksByLocation = new Block[height][width];
        
        for (int y = 0; y < g.height; y++) {
            for (int x = 0; x < width; x++) {
                blocksByLocation[y][x] = g.blocksByLocation[y][x];
            }
        }
    }

    public void relocate(int fromX, int fromY, int toX, int toY) { 

        if (!inRange(fromX, fromY)) { 
            throw new IllegalArgumentException("Source out of bounds! (" + fromX + "x" + fromY + ")");
        }
        
        if (!inRange(toX, toY)) { 
            throw new IllegalArgumentException("Destination out of bounds! (" + toX + "x" + toY + ")");
        }

        if (blocksByLocation[fromY][fromX] == null) { 
            throw new IllegalArgumentException("Source not already in use! (" + fromX + "x" + fromY + ")");
        }
        
        if (blocksByLocation[toY][toX] != null) { 
            throw new IllegalArgumentException("Destination already in use! (" + toX + "x" + toY + ")");
        }

        Block b = blocksByLocation[fromY][fromX];
        Block newBlock = new Block(b, new Coordinate(toX, toY));
        
        blocksByNumber[b.blockID] = newBlock;
        blocksByLocation[toY][toX] = newBlock;
        blocksByLocation[fromY][fromX] = null;
    }
    
    public void insertLand() { 

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) { 
                if (blocksByLocation[y][x] == null) { 
                    blocksByLocation[y][x] = new Block(new Coordinate(x, y));
                }
            }
        }
    }
    
//    public void map(Topography topo) { 
//        
//        System.out.println("Mapping topo " + height + " " + width);
//        
//        for (int y = 0; y < height; y++) {
//            for (int x = 0; x < width; x++) {
//
//            }
//        }
//    }
    
    //    private void setLand(int x, int y) { 
    //        
    //        Block b = get(x, y);
    //        b.setOcean(false);

    //        b.setMessageSizeNorth(0);
    //        b.setMessageSizeNorthEast(0);
    //        b.setMessageSizeEast(0);
    //        b.setMessageSizeSouthEast(0);
    //        b.setMessageSizeSouth(0);
    //        b.setMessageSizeSouthWest(0);
    //        b.setMessageSizeWest(0);
    //        b.setMessageSizeNorthWest(0);
    //
    //        Block tmp = b.getNeighbourNorth();
    //        
    //        if (tmp != null) { 
    //            tmp.setMessageSizeSouth(0);
    //        }
    //        
    //        tmp = b.getNeighbourNorthEast();
    //        
    //        if (tmp != null) { 
    //            tmp.setMessageSizeSouthWest(0);
    //        }
    //        
    //        tmp = b.getNeighbourEast();
    //        
    //        if (tmp != null) { 
    //            tmp.setMessageSizeWest(0);
    //        }
    //        
    //        tmp = b.getNeighbourSouthEast();
    //        
    //        if (tmp != null) { 
    //            tmp.setMessageSizeNorthWest(0);
    //        }
    //        
    //        tmp = b.getNeighbourSouth();
    //        
    //        if (tmp != null) { 
    //            tmp.setMessageSizeNorth(0);
    //        }
    //        
    //        tmp = b.getNeighbourSouthWest();
    //        
    //        if (tmp != null) { 
    //            tmp.setMessageSizeNorthEast(0);
    //        }
    //        
    //        tmp = b.getNeighbourWest();
    //        
    //        if (tmp != null) { 
    //            tmp.setMessageSizeEast(0);
    //        }
    //        
    //        tmp = b.getNeighbourNorthWest();
    //        
    //        if (tmp != null) { 
    //            tmp.setMessageSizeSouthEast(0);
    //        }
    //    }

//    private void setNeighbours(Neighbours n, Block b) {
//        
//        b.setNeighbourNorth(coordinateToBlockID(n.getNeighbourNorth(b.coordinate)));
//        b.setNeighbourNorthEast(coordinateToBlockID(n.getNeighbourNorthEast(b.coordinate)));
//        b.setNeighbourEast(coordinateToBlockID(n.getNeighbourEast(b.coordinate)));
//        b.setNeighbourSouthEast(coordinateToBlockID(n.getNeighbourSouthEast(b.coordinate)));
//        b.setNeighbourSouth(coordinateToBlockID(n.getNeighbourSouth(b.coordinate)));
//        b.setNeighbourSouthWest(coordinateToBlockID(n.getNeighbourSouthWest(b.coordinate)));
//        b.setNeighbourWest(coordinateToBlockID(n.getNeighbourWest(b.coordinate)));
//        b.setNeighbourNorthWest(coordinateToBlockID(n.getNeighbourNorthWest(b.coordinate)));
//
//        b.setMessageSizeNorth(n.getMessageSizeNorth(b.coordinate));
//        b.setMessageSizeNorthEast(n.getMessageSizeNorthEast(b.coordinate));
//        b.setMessageSizeEast(n.getMessageSizeEast(b.coordinate));
//        b.setMessageSizeSouthEast(n.getMessageSizeSouthEast(b.coordinate));
//        b.setMessageSizeSouth(n.getMessageSizeSouth(b.coordinate));
//        b.setMessageSizeSouthWest(n.getMessageSizeSouthWest(b.coordinate));
//        b.setMessageSizeWest(n.getMessageSizeWest(b.coordinate));
//        b.setMessageSizeNorthWest(n.getMessageSizeNorthWest(b.coordinate));
//    }

//    private int coordinateToBlockID(Coordinate c) { 
//        return c.y*width + c.x + 1;
//    }
//    
    
    /**
     * Checks if the given coordinate falls within this grid.
     * 
     * @param x
     *            the x coordinate to check.
     * @param y
     *            the y coordinate to check.
     * @return if the given coordinate falls within this grid.
     */
    private boolean inRange(int x, int y) {
        return !(x < 0 || x >= width || y < 0 || y >= height);
    }

    /**
     * Checks if the given coordinate fall within this grid.
     * 
     * @param c
     *            the Coordinate to check.
     * @return if the given coordinate falls within this grid.
     */
    private boolean inRange(Coordinate c) {
        return !(c.x < 0 || c.x >= width || c.y < 0 || c.y >= height);
    }

    /**
     * Returns the number of non-empty blocks in this grid.
     * 
     * @return the number of non-empty blocks in this grid.
     * @see Block
     */
    public int getCount() {
        return count;
    }

    /**
     * Stores a Block in the grid.
     * 
     * @param b
     *            the Block to store.
     * @see Block
     */
//    private void put(Block b) {
//
//        if (!inRange(b.coordinate)) {
//            throw new IllegalArgumentException("Coordinate out of bounds! " + b.coordinate);
//        }
//
//        Coordinate c = b.coordinate;
//        
//        if (blocks[c.y * width + c.x] == null) {
//            count++;
//        }
//
//        blocks[c.y * width + c.x] = b;
//    }

    /**
     * Stores all Blocks in a Collection in this grid.
     * 
     * @param elts
     *            the blocks to store.
     * @see Block
     * @see Collection
     */
//    public void putAll(Collection<Block> elts) {
//        for (Block b : elts) {
//            put(b);
//        }
//    }

    /**
     * Retrieves all Blocks in this grid and store them in a Collection.
     * 
     * @param out
     *            the collection to which all blocks in this grid will be added.
     * @see Block
     * @see Collection
     */
//    public void getAll(Collection<Block> out) {
//        for (Block b : blocks) {
//            if (b != null) {
//                out.add(b);
//            }
//        }
//    }

    /**
     * Retrieves a Block at a given location.
     * 
     * @param c
     *            the location of the block to retrieve, or null.
     * @return the Block at the given location, or null if the location is empty.
     * @see Block
     */
//    private Block get(Coordinate c) {
//
//        if (c == null) {
//            return null;
//        }
//
//        return get(c.x, c.y);
//    }

    /**
     * Retrieves a Block at a given location.
     * 
     * @param x
     *            the x coordinate of the location to retrieve.
     * @param y
     *            the y coordinate of the location to retrieve.
     * @return the Block at the given location, or null if the location is empty.
     * @see Block
     */
    public Block get(int x, int y) {

        if (!inRange(x, y)) {
            throw new IllegalArgumentException("Coordiate out of bounds! " + x + "x" + y);
        }

        return blocksByLocation[y][x];
    }

    /**
     * Retrieves a Block with a given blockID.
     * 
     * @param blockID
     *            the ID of the block to retrieve.

     * @return the Block at the given location, or null if the location does not exist.
     * @see Block
     */
    public Block get(int blockID) {

        if (blockID < 0 || blockID >= blocksByNumber.length) { 
            throw new IllegalArgumentException("BlockID out of bounds! " + blockID);
        }

        return blocksByNumber[blockID];
    }

    
    @Override
    public Iterator<Block> iterator() {
        return new BlockIterator(blocksByNumber);
    }

    /**
     * Retrieve all Blocks in a given rectangular subsection of this grid.
     * 
     * The subsection is an area in a coordinate space that is enclosed by the locations (x,y) (inclusive) and (x+width,y+height)
     * (exclusive) in the coordinate space.
     * 
     * @param x
     *            the x coordinate of the subsection.
     * @param y
     *            the y coordinate of the subsection.
     * @param width
     *            the width of the subsection.
     * @param height
     *            the height of the subsection.
     * @return All blocks enclosed by the specified subsection.
     * @see Block
     */
    public Block[] getRectangle(int x, int y, int width, int height) {

        ArrayList<Block> result = new ArrayList<Block>();

        for (int j = y; j < y + height; j++) {
            for (int i = x; i < x + width; i++) {
                if (inRange(x, y)) {

                    Block b = blocksByLocation[j][i];

                    if (b != null) {
                        result.add(b);
                    }
                }
            }
        }

        return result.toArray(new Block[result.size()]);
    }
}
