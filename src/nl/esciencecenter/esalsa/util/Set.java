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
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Set represents a set of Blocks.
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 * @see Block
 * 
 */
public class Set implements Iterable<Block> {

    /** The smallest x coordinate found in all blocks of the set. */
    public final int minX;

    /** The largest x coordinate found in all blocks of the set. */
    public final int maxX;

    /** The smallest y coordinate found in all blocks of the set. */
    public final int minY;

    /** The largest y coordinate found in all blocks of the set. */
    public final int maxY;

    /** The blocks in this set. */
    private final Block[] blocks;

    /** A list of subsets of this set. */
    private ArrayList<Set> subSets;

    /** A list of blockIDs of possible neighbor blocks of this set. */
    private int[] neighbours;

    /** The amount of external (out of set) communication required by the blocks in this set. */
    private int communication = -1;

    /** The index of this set on the layer it belogs to. */
    public final int index;

    /** A Comparator used to sort blocks on their coordinates (smallest first). */
    private class BlockComparator implements Comparator<Block> {

        @Override
        public int compare(Block o1, Block o2) {

            if (o1.blockID == o2.blockID) { 
                return 0;
            }
            
            Coordinate c1 = o1.coordinate;
            Coordinate c2 = o2.coordinate;

            if (c1.y < c2.y) {
                return -1;
            } else if (c1.y == c2.y) {
                if (c1.x < c2.x) {
                    return -1;
                } else if (c1.x == c2.x) {
                    // should not happen ?
                    return 0;
                }
            }

            return 1;
        }
    }

    /**
     * Create a Set containing a single Block.
     * 
     * @param block
     *            the block to insert into this set.
     */
    public Set(Block block, int index) {
        this.index = index;
        blocks = new Block[] { block };

        Coordinate c = block.coordinate;
        
        minX = maxX = c.x;
        minY = maxY = c.y;
    }

    /**
     * Create a Set containing all Blocks in the Collection provided.
     * 
     * @param collection
     *            the blocks to add to the set.
     */
    public Set(Collection<Block> collection, int index) {

        this.index = index;

        if (collection == null || collection.size() == 0) {
            //throw new IllegalArgumentException("Empty Set not allowed!");
            blocks = new Block[0];
            minX = maxX = minY = maxY = 0;
            return;
        }

        this.blocks = collection.toArray(new Block[collection.size()]);
        Arrays.sort(this.blocks, new BlockComparator());

        int tmpMinX = Integer.MAX_VALUE;
        int tmpMinY = Integer.MAX_VALUE;

        int tmpMaxX = Integer.MIN_VALUE;
        int tmpMaxY = Integer.MIN_VALUE;

        for (int i = 0; i < this.blocks.length; i++) {

            Block block = this.blocks[i];

            Coordinate c = block.coordinate;
            
            if (c.x < tmpMinX) {
                tmpMinX = c.x;
            }

            if (c.x > tmpMaxX) {
                tmpMaxX = c.x;
            }

            if (c.y < tmpMinY) {
                tmpMinY = c.y;
            }

            if (c.y > tmpMaxY) {
                tmpMaxY = c.y;
            }
        }

        minX = tmpMinX;
        maxX = tmpMaxX;
        minY = tmpMinY;
        maxY = tmpMaxY;
    }

    /**
     * Create a Set containing all Blocks in the array provided.
     * 
     * The array may not be null or empty.
     * 
     * @param blocks
     *            the blocks to add to the set.
     */
    public Set(Block[] blocks, int index) {

        this.index = index;

        if (blocks == null || blocks.length == 0) {
            throw new IllegalArgumentException("Empty Set not allowed!");
        }

        this.blocks = blocks.clone();

        Arrays.sort(this.blocks, new BlockComparator());

        int tmpMinX = Integer.MAX_VALUE;
        int tmpMinY = Integer.MAX_VALUE;

        int tmpMaxX = Integer.MIN_VALUE;
        int tmpMaxY = Integer.MIN_VALUE;

        for (int i = 0; i < this.blocks.length; i++) {

            Block block = this.blocks[i];
            
            Coordinate c = block.coordinate;
            
            if (c.x < tmpMinX) {
                tmpMinX = c.x;
            }

            if (c.x > tmpMaxX) {
                tmpMaxX = c.x;
            }

            if (c.y < tmpMinY) {
                tmpMinY = c.y;
            }

            if (c.y > tmpMaxY) {
                tmpMaxY = c.y;
            }
        }

        minX = tmpMinX;
        maxX = tmpMaxX;
        minY = tmpMinY;
        maxY = tmpMaxY;
    }

    /**
     * Creates a new containing the same blocks as the set provided.
     * 
     * @param set
     *            the set to copy the blocks from.
     */
    public Set(Set set, int index) {

        this.index = index;

        minX = set.minX;
        maxX = set.maxX;
        minY = set.minY;
        maxY = set.maxY;

        blocks = set.blocks.clone();
    }

    public Line [] getEdges() { 
        
        ArrayList<Line> result = new ArrayList<Line>();
        
        for (Block b : blocks) {
            getEdges(b, result);
        }
        
        return result.toArray(new Line[result.size()]);
    }
    
    /**
     * Determines if a block may be on the edge of the set, that is, it has neighbors that are not part of the set.
     * 
     * @param b
     *            the block to check
     * @return if the block may be on the edge of the set.
     */
    private void getEdges(Block b, ArrayList<Line> result) {

        int [][] neighbours = b.getNeighbours();

        if (neighbours[0][1] > 0 && !contains(neighbours[0][1])) {
            result.add(new Line(b.coordinate.offset(0, 1), b.coordinate.offset(1, 1)));
        }

        if (neighbours[1][0] > 0 && !contains(neighbours[1][0])) {
            result.add(new Line(b.coordinate, b.coordinate.offset(0, 1)));
        }

        if (neighbours[1][2] > 0 && !contains(neighbours[1][2])) {
            result.add(new Line(b.coordinate.offset(1, 0), b.coordinate.offset(1, 1)));
        }
        
        if (b.coordinate.y == 0 || (neighbours[2][1] > 0 && !contains(neighbours[2][1]))) {
            result.add(new Line(b.coordinate, b.coordinate.offset(1, 0)));
        }
    }

    /**
     * Determines if a block may be on the edge of the set, that is, it has neighbors that are not part of the set.
     * 
     * @param b
     *            the block to check
     * @return if the block may be on the edge of the set.
     */
    private boolean onEdge(Block b) {

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (!(i == 0 && j == 0)) {
                    
                    Coordinate c = b.coordinate;

                    int nx = c.x + i;
                    int ny = c.y + j;

                    if (nx < 0 || ny < 0) {
                        return true;
                    }

                    if (!contains(nx, ny)) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }

    /**
     * Retrieve the coordinates of the neighbor Blocks of this set.
     * 
     * @return the coordinates of the neighbor Blocks of this set.
     */
    public int[] getNeighbours() {
        
        if (neighbours == null) {

            this.communication = 0;

            HashSet<Integer> result = new HashSet<Integer>();

            for (Block b : blocks) {
            
                if (onEdge(b)) {

                    int[][] neighbours = b.getNeighbours();
                    int[][] communication = b.getCommunication();
                    
                    for (int y = 0; y < 3; y++) {
                        for (int x = 0; x < 3; x++) {

                    /*        if (tmp[i][j] > 0) {

                                if (!contains(tmp[i][j])) {
                                    communication += b.getCommunication(i - 1, j - 1);
                                    result.add(tmp[i][j]);
                                }
                     */           
                       
                                if (neighbours[y][x] > 0 && !contains(neighbours[y][x])) { 
                                    this.communication += communication[y][x];
                                    result.add(neighbours[y][x]);
                                }
                        }
                    }
                }
            }

            neighbours = new int[result.size()];
            
            int index = 0;
            
            for (int tmp : result) { 
                neighbours[index++] = tmp;
            }
        }

        return neighbours;
    }

    /**
     * Return the total amount of external (out of set) communication required by the Blocks in this set.
     * 
     * @return the total amount of external (out of set) communication required by this set.
     */
    public int getCommunication() {

        if (communication == -1) {
            getNeighbours();
        }

        return communication;
    }

    /**
     * Retrieve the Block at the given index. The index must be between 0 (inclusive) and {@link #size()} (exclusive).
     * 
     * @param index
     *            the index of the block to retrieve.
     * @return the block at the given index.
     */
    public Block get(int index) {

        if (index < 0 || index >= blocks.length) {
            throw new NoSuchElementException("Index out of bounds " + index);
        }

        return blocks[index];
    }

    /**
     * Retrieve all blocks of this Set.
     * 
     * @return an array containing all blocks of this set.
     */
    public Block[] getAll() {
        return blocks.clone();
    }

    /**
     * Add all blocks of this Set to the collection.
     * 
     * @param output
     *            the collection to add the blocks to.
     */
    public void getAll(Collection<Block> output) {
        for (int i = 0; i < blocks.length; i++) {
            output.add(blocks[i]);
        }
    }

    /**
     * Test if this set contains a Block at location (x,y).
     * 
     * @param x
     *            the x coordinate of the location to test.
     * @param y
     *            the y coordinate of the location to test.
     * @return if this set contains a block at the specified location.
     */
    public boolean contains(int x, int y) {
        
        // FIXME: Can this go faster ?
        
        return (get(x, y) != null);
    }

    /**
     * Test if this set contains a Block with a given blockID.
     * 
     * @param blockID
     *            the ID of the block to find.
     *            
     * @return if this set contains a block at the specified location.
     */
    public boolean contains(int blockID) {

        for (int i=0;i<blocks.length;i++) {
            if (blocks[i].blockID == blockID) { 
                return true;
            }
        }    
        
        return false;
    }

    
    /**
     * Retrieve the Block at location (x,y).
     * 
     * @param x
     *            the x coordinate of the block to retrieve.
     * @param y
     *            the y coordinate of the block to retrieve.
     * @return the specified block, or null if the specified location is empty, or not part of this set.
     */
    public Block get(int x, int y) {

        if (x < minX || x > maxX || y < minY || y > maxY) {
            return null;
        }

        // FIXME: optimize!
        for (int i = 0; i < blocks.length; i++) {
            
            Coordinate c = blocks[i].coordinate;
            
            if (c.x == x && c.y == y) {
                return blocks[i];
            }
        }

        return null;
    }

    /**
     * Returns the number of Blocks in this set.
     * 
     * @return the number of blocks in this set.
     */
    public int size() {
        return blocks.length;
    }

    /**
     * Returns the width of the area defined by the blocks in this set.
     * 
     * @return the width of this set.
     */
    public int getWidth() {
        return maxX - minX + 1;
    }

    /**
     * Returns the height of the area defined by the blocks in this set.
     * 
     * @return the height of this set.
     */
    public int getHeight() {
        return maxY - minY + 1;
    }

    /**
     * Add a set to the list of subsets of this set.
     * 
     * @param subset
     *            the set to add as a subset of this set.
     */
    public void addSubSet(Set subset) {

        if (subset == null) {
            throw new NullPointerException("Cannot add null as subset");
        }

        if (subSets == null) {
            subSets = new ArrayList<Set>();
        }

        subSets.add(subset);
    }

    /**
     * Add all sets in the provided Collection to list of subsets of this set.
     * 
     * @param collection
     *            the sets to add as subsets of this set.
     */
    public void addSubSets(Collection<Set> collection) {

        if (collection == null) {
            throw new NullPointerException("Cannot add null as subsets");
        }

        for (Set s : collection) {
            addSubSet(s);
        }
    }

    /**
     * Returns the number of subsets of this set.
     * 
     * @return the number of subsets of this set.
     */
    public int countSubSets() {
        if (subSets == null) {
            return 0;
        }

        return subSets.size();
    }

    /**
     * Returns the list subsets of this set.
     * 
     * @return the list subsets of this set.
     */
    public ArrayList<Set> getSubSets() {
        return subSets;
    }

    @Override
    public Iterator<Block> iterator() {
        return new BlockIterator(blocks);
    }
}
