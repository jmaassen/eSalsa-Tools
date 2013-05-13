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

public class OptimizeTopography {

    private final Topography topology;
    private final Grid grid;
    private final Neighbours neighbours;

    private Topography optTop;
    private Grid optGrid;

    public OptimizeTopography(Topography topology, Grid grid, Neighbours neighbours) {
        this.topology = topology;
        this.grid = grid;
        this.neighbours = neighbours;
    }

    public Topography getOptimizedTopography() {
        return optTop;
    }

    public Grid getOptimizedGrid() {
        return optGrid;
    }

    public Topography createTopography(int index) throws Exception {

        int topoHeight = (topology.height + (topology.height - index * grid.blockHeight));

        System.out.println("New Topology dimensions " + topology.width + "x" + topoHeight);

        int xOffset = (grid.width / 2) * grid.blockWidth;

        int[][] data = new int[topology.width][topoHeight];

        for (int x = 0; x < topology.width; x++) {
            for (int y = 0; y < topoHeight; y++) {
                data[x][y] = 0;
            }
        }

        for (int y = 0; y < index * grid.blockHeight; y++) {
            for (int x = 0; x < topology.width; x++) {
                data[x][y] = topology.get(x, y);
            }
        }

        for (int y = index * grid.blockHeight; y < topology.height; y++) {
            for (int x = 0; x < xOffset; x++) {
                data[x][y] = topology.get(x, y);
            }
        }

        for (int y = 1; y < (grid.height - index) * grid.blockHeight; y++) {
            for (int x = 0; x < xOffset; x++) {
                data[xOffset - x - 1][topology.height + y - 1] = topology.get(xOffset + x, topology.height - y);
            }
        }

        return new Topography(data);
    }

    public Grid createGrid(int index) {

        System.out.println("Original Grid dimensions " + grid.width + "x" + grid.height);
        System.out.println("New Grid dimensions " + grid.width + "x" + (grid.height + (grid.height - index)));
        
        Grid g = new Grid(grid, grid.height - index);
        
        for (int y = index; y < grid.height; y++) {
            for (int x = grid.width / 2; x < grid.width; x++) {

                int newX = grid.width / 2 - (x - grid.width / 2) - 1;
                int newY = grid.height + (grid.height - y - 1);

                g.relocate(x, y, newX, newY);
            }
        }

        g.insertLand();
        
        return g;
    }

    public void optimize() throws Exception {

        // This optimization only makes sense if a tripolo grid is used.  
        if (neighbours.boundaryV == Neighbours.TRIPOLE) {
            // Optimize tripole blocks up top. 

            int communication = 0;

            for (int i = 0; i < grid.width; i++) {
                int[][] tmp = neighbours.getCommunication(new Coordinate(i, grid.height - 1));
                communication += tmp[0][0] + tmp[1][0] + tmp[2][0];
            }

            System.out.println("Communication TRIPOLE " + communication);

            // Find the narrowest point at the Bering Straight. 			
            int[] width = new int[grid.height];
            int best = Integer.MAX_VALUE;
            int index = -1;

            for (int i = 0; i < grid.height; i++) {
                for (int j = grid.width / 2; j < grid.width; j++) {
                    if (grid.get(j, i).ocean) {
                        width[i]++;
                    }
                }

                System.out.println("Width[" + i + "] = " + width[i]);

                if (width[i] < best) {
                    best = width[i];
                    index = i;
                }
            }

            System.out.println("Best " + index + " = " + best);

            optTop = createTopography(index);
            optGrid = createGrid(index);
        }
    }
}
