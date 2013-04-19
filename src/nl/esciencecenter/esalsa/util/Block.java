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
 * Block represents a block in a POP work distribution.
 * 
 * It consists of an immutable <code>Coordinate</code>, and an <code>int</code> mark that can be used to label the block with an
 * arbitrary integer value.
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 * @see Coordinate
 */
public class Block {
    
    public final int blockID;

    /** The Coordinate of the block. */
    public final Coordinate coordinate;
    
    /** Is this an ocean block ? */
    public final boolean ocean;
    
    /** All possible neighbours */
    public final int neighbourNorth;
    public final int neighbourNorthWest;
    public final int neighbourWest;
    public final int neighbourSouthWest;
    public final int neighbourSouth;
    public final int neighbourSouthEast;
    public final int neighbourEast;
    public final int neighbourNorthEast;

    public final int messageSizeNorth;
    public final int messageSizeNorthWest;
    public final int messageSizeWest;
    public final int messageSizeSouthWest;
    public final int messageSizeSouth;
    public final int messageSizeSouthEast;
    public final int messageSizeEast;
    public final int messageSizeNorthEast;

    /** Mark use to label the block */
    private int mark = 0;

    /**
     * Constructor to create a new Block.
     * 
     * @param coordinate
     *            coordinate of the block.
     */
    public Block(int blockID, Coordinate coordinate, Neighbours n) {
        this.blockID = blockID;
        this.coordinate = coordinate;
        this.ocean = n.isOcean(coordinate);
        
        this.neighbourNorth = n.getNeighbourNorth(coordinate);
        this.neighbourNorthWest  = n.getNeighbourNorthWest(coordinate);
        this.neighbourWest  = n.getNeighbourWest(coordinate);
        this.neighbourSouthWest = n.getNeighbourSouthWest(coordinate);
        this.neighbourSouth = n.getNeighbourSouth(coordinate);
        this.neighbourSouthEast  = n.getNeighbourSouthEast(coordinate);
        this.neighbourEast = n.getNeighbourEast(coordinate);
        this.neighbourNorthEast = n.getNeighbourNorthEast(coordinate);

        if (ocean) { 
            this.messageSizeNorth = n.getMessageSizeNorth(coordinate);
            this.messageSizeNorthWest = n.getMessageSizeNorthWest(coordinate);
            this.messageSizeWest = n.getMessageSizeWest(coordinate);
            this.messageSizeSouthWest = n.getMessageSizeSouthWest(coordinate);
            this.messageSizeSouth = n.getMessageSizeSouth(coordinate);
            this.messageSizeSouthEast = n.getMessageSizeSouthEast(coordinate);
            this.messageSizeEast = n.getMessageSizeEast(coordinate);
            this.messageSizeNorthEast = n.getMessageSizeNorthEast(coordinate);
        } else { 
            this.messageSizeNorth = 0;
            this.messageSizeNorthWest = 0;
            this.messageSizeWest = 0;
            this.messageSizeSouthWest = 0;
            this.messageSizeSouth = 0;
            this.messageSizeSouthEast = 0;
            this.messageSizeEast = 0;
            this.messageSizeNorthEast = 0;
        }
    } 
    
    public Block(Block original, Coordinate newCoordinate) {
        this.blockID = original.blockID;
        this.coordinate = newCoordinate;

        this.ocean = original.ocean;
        this.mark = original.mark;
        
        this.neighbourNorth = original.neighbourNorth;
        this.neighbourNorthWest  = original.neighbourNorthWest;
        this.neighbourWest  = original.neighbourWest;
        this.neighbourSouthWest = original.neighbourSouthWest;
        this.neighbourSouth = original.neighbourSouth;
        this.neighbourSouthEast  = original.neighbourSouthEast;
        this.neighbourEast = original.neighbourEast;
        this.neighbourNorthEast = original.neighbourNorthEast;

        this.messageSizeNorth = original.messageSizeNorth;
        this.messageSizeNorthWest = original.messageSizeNorthWest;
        this.messageSizeWest = original.messageSizeWest;
        this.messageSizeSouthWest = original.messageSizeSouthWest;
        this.messageSizeSouth = original.messageSizeSouth;
        this.messageSizeSouthEast = original.messageSizeSouthEast;
        this.messageSizeEast = original.messageSizeEast;
        this.messageSizeNorthEast = original.messageSizeNorthEast;
    }
    
    public Block(Coordinate coordinate) {
        
        this.blockID = -1;
        this.coordinate = coordinate;
        this.ocean = false;
        
        this.neighbourNorth = 0;
        this.neighbourNorthWest = 0;
        this.neighbourWest = 0;
        this.neighbourSouthWest = 0;
        this.neighbourSouth = 0;
        this.neighbourSouthEast = 0;
        this.neighbourEast = 0;
        this.neighbourNorthEast = 0;

        this.messageSizeNorth = 0;
        this.messageSizeNorthWest = 0;
        this.messageSizeWest = 0;
        this.messageSizeSouthWest = 0;
        this.messageSizeSouth = 0;
        this.messageSizeSouthEast = 0;
        this.messageSizeEast = 0;
        this.messageSizeNorthEast = 0;
    }
    /**
     * Constructor to move a block.
     * 
     * @param coordinate
     *            coordinate of the block.
     */
//    public void move(Coordinate coordinate) {
//        
//        if (this.coordinate.equals(coordinate)) {
//            // Move to current position doe nothing. 
//            return;
//        }
//        
//        if (!moved) {
//            // First move saves original postion. 
//            originalCoordinate = this.coordinate;
//            this.coordinate = coordinate;
//            moved = true;
//        } else { 
//            if (originalCoordinate.equals(coordinate)) { 
//                // Move back to original position clears move. 
//                originalCoordinate = null;
//                this.coordinate= coordinate;
//                moved = false;
//            } else { 
//                // Additional move loses intermediate position
//                this.coordinate= coordinate;
//            }
//        } 
//    }
//
//    public boolean hasMoved() {
//        return moved;
//    }
//    
//    public Coordinate getCoordinate() { 
//        return coordinate;
//    }
//    
//    public Coordinate getOriginalCoordinate() {
//        
//        if (moved) { 
//            return originalCoordinate;
//        }
//        
//        return coordinate;
//    }
    
    /**
     * Mark the block with the given value.
     * 
     * @param value
     *            the value to mark the block with.
     */
    public void setMark(int value) {
        mark = value;
    }

    /**
     * Add the given value to the current mark.
     * 
     * @param value
     *            value to add to the current mark.
     */
    public void addToMark(int value) {
        mark += value;
    }

    /**
     * Retrieve the value of the current mark.
     * 
     * @return the current value of the mark.
     */
    public int getMark() {
        return mark;
    }

    public int[][] getNeighbours() {

        int[][] result = new int[3][3];

        result[0][0] = neighbourNorthWest;
        result[0][1] = neighbourNorth;
        result[0][2] = neighbourNorthEast;

        result[1][0] = neighbourWest;
        result[1][1] = 0;
        result[1][2] = neighbourEast;

        result[2][0] = neighbourSouthWest;
        result[2][1] = neighbourSouth;
        result[2][2] = neighbourSouthEast;

        return result;
    }

    public int[][] getCommunication() {

        int[][] result = new int[3][3];

        result[0][0] = messageSizeNorthWest;
        result[0][1] = messageSizeNorth;
        result[0][2] = messageSizeNorthEast;

        result[1][0] = messageSizeWest;
        result[1][1] = 0;
        result[1][2] = messageSizeEast;

        result[2][0] = messageSizeSouthWest;
        result[2][1] = messageSizeSouth;
        result[2][2] = messageSizeSouthEast;

        return result;
    }

//    
//    public int getCommunication(int dx, int dy) {
//
//        switch (dx) {
//        case -1: {
//            switch (dy) {
//            case -1:
//                return messageSizeSouthWest;
//            case 0:
//                return messageSizeWest;
//            case 1:
//                return messageSizeNorthWest;
//            default:
//                return 0;
//            }
//        }
//
//        case 0: {
//            switch (dy) {
//            case -1:
//                return messageSizeSouth;
//            case 0:
//                return 0;
//            case 1:
//                return messageSizeNorth;
//            default:
//                return 0;
//            }
//        }
//
//        case 1: {
//            switch (dy) {
//            case -1:
//                return messageSizeSouthEast;
//            case 0:
//                return messageSizeEast;
//            case 1:
//                return messageSizeNorthEast;
//            default:
//                return 0;
//            }
//        }
//
//        default:
//            return 0;
//        }
//    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + coordinate.x;
        result = prime * result + coordinate.y;
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;

        if (obj == null || getClass() != obj.getClass())
            return false;

        return blockID == ((Block) obj).blockID;
    }
    
    public String toString() { 
        return "Block(" + coordinate + ", " + ocean + ")";
    }
}
