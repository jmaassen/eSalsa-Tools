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
import nl.esciencecenter.esalsa.loadbalancer.LoadBalancer;
import nl.esciencecenter.esalsa.tools.DistributionViewer;

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

        // Grid g = new Grid(grid, grid.height - index);
        
        Grid g = new Grid(grid, grid.height - index);
        
        // Simply copy all blocks below index.
//        for (int y = 0; y < index; y++) {
//            for (int x = 0; x < grid.width; x++) {
//                Block b = grid.get(x, y);
//
//                if (b != null) {
//                    g.put(b);
//                }
//            }
//        }

        // Also copy all blocks above index and left of tripole.
//        for (int y = index; y < grid.height; y++) {
//            for (int x = 0; x < grid.width / 2; x++) {
//                Block b = grid.get(x, y);
//
//                if (b != null) {
//                    g.put(b);
//                }
//            }
//        }

        // Finally relocate all blocks above index and right of tripole. 
//
//        for (int y = index; y < grid.height; y++) {
//            for (int x = grid.width / 2; x < grid.width; x++) {
//                Block b = grid.get(x, y);
//
//                if (b != null) {
//                    int newX = grid.width / 2 - (x - grid.width / 2) - 1;
//                    int newY = grid.height + (grid.height - y - 1);
//
//                    g.put(new MetaBlock(new Coordinate(newX, newY), b));
//                }
//            }
//        }

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

//            Grid g2 = new Grid(optTop, grid.blockWidth, grid.blockHeight);
//
//            // Make sure g and g2 are the same!
//            if (optGrid.width != g2.width || optGrid.height != g2.height) {
//                throw new Exception("Grid size mismatch!");
//            }
//
//            for (int x = 0; x < optGrid.width; x++) {
//                for (int y = 0; y < optGrid.height; y++) {
//
//                    Block b1 = optGrid.get(x, y);
//                    Block b2 = g2.get(x, y);
//
//                    if (b1 == null && b2 != null) {
//                        throw new Exception("Grid content mismatch at " + x + "x" + y);
//                    }
//
//                    if (b1 != null && b2 == null) {
//                        throw new Exception("Grid content mismatch at " + x + "x" + y);
//                    }
//                }
//            }
//
//            System.out.println("Grid compare OK!");
        //}

        //            Grid g = new Grid(t, grid.blockWidth, grid.blockHeight);
        //
        //            int min = Integer.MAX_VALUE;
        //            int minIndex = 0;
        //
        //            for (int x = 0; x < g.width; x++) {
        //
        //                int sum = 0;
        //
        //                for (int y = 0; y < g.height; y++) {
        //
        //                    if (g.get(x, y) != null) {
        //                        sum++;
        //                    }
        //                }
        //
        //                if (sum < min) {
        //                    min = sum;
        //                    minIndex = x;
        //                }
        //
        //                System.out.println("Active " + x + " = " + sum);
        //            }

        //            System.out.println("Minimum " + min + " at " + minIndex);
        //
        
//                Grid g = optGrid;
//                
//                    TopographyCanvas tc = new TopographyCanvas(optTop, optGrid);
//        
//                    tc.addLayer("LINES");
//                    tc.addLayer("BLOCKS");
//        
//                    JFrame frame = new JFrame("Topology");
//                    frame.setSize(900, 700);
//                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//                    frame.getContentPane().add(tc);
//                    frame.setVisible(true);
//        
//                    Color c = new Color(128, 128, 128, 128);
//        
//                    tc.draw("LINES", new Line(new Coordinate(0, 0), new Coordinate(0, g.height)), c, 7.0f);
//                    tc.draw("LINES", new Line(new Coordinate(0, 0), new Coordinate(g.width, 0)), c, 7.0f);
//        
//                    tc.draw("LINES", new Line(new Coordinate(0, g.height), new Coordinate(g.width, g.height)), c, 7.0f);
//                    tc.draw("LINES", new Line(new Coordinate(g.width, 0), new Coordinate(g.width, g.height)), c, 7.0f);
//        
//                    for (int i = 1; i < g.width; i++) {
//                        tc.draw("LINES", new Line(new Coordinate(i, 0), new Coordinate(i, g.height)), c, 7.0f);
//                    }
//        
//                    for (int i = 1; i < g.height; i++) {
//                        tc.draw("LINES", new Line(new Coordinate(0, i), new Coordinate(g.width, i)), c, 7.0f);
//                    }
//        
//                    for (int y = 0; y < g.height; y++) {
//                        for (int x = 0; x < g.width; x++) {
//                            
//                            Block b = g.get(x, y);
//                            
//                            if (b instanceof MetaBlock) { 
//                                b = ((MetaBlock) b).getOriginal();
//                            }
//                            
//                            if (b.isOcean()) {
//                                tc.fillBlock("BLOCKS", x, y, Color.WHITE);
//                            } else {
//                                tc.fillBlock("BLOCKS", x, y, Color.BLACK);
//                            }
//                        }
//                    }
//        
//        //            for (int y = 0; y < g.height; y++) {
//        //                if (g.get(minIndex, y) != null) {
//        //                    tc.fillBlock("BLOCKS", minIndex, y, Color.RED);
//        //                }
//        //            }
//        
//                    tc.repaint();
//                }
        //
        //        if (neighbours.boundaryW == Neighbours.CYCLIC) {
        //            // optimize edge blocks on east/west border. 
        //
        //        }
        } 
    }

    public static void main(String[] args) {

        try {

            int topologyWidth = Integer.parseInt(args[1]);
            int topologyHeight = Integer.parseInt(args[2]);

            Topography t = new Topography(topologyWidth, topologyHeight, args[0]);

            int blockWidth = Integer.parseInt(args[3]);
            int blockHeight = Integer.parseInt(args[4]);

            int gridWidth = topologyWidth / blockWidth;
            int gridHeight = topologyHeight / blockHeight;
            
            Neighbours n = new Neighbours(t, gridWidth, gridHeight, blockWidth, blockHeight, 
                    Neighbours.CYCLIC, Neighbours.TRIPOLE);
            
            Grid g = new Grid(gridWidth, gridHeight, blockWidth, blockHeight, n);
            
            OptimizeTopography top = new OptimizeTopography(t, g, n);
            top.optimize();

            Topography optT = top.getOptimizedTopography();
            Grid optG = top.getOptimizedGrid();
            
            Layers l = new Layers();
            LoadBalancer lb = new LoadBalancer(l, optT.width, optT.height, optG, blockWidth, blockHeight, 
                    1, 4, 64, "search");

            lb.split();
            
            int [] result = new int[gridWidth * gridHeight];
            
            for (Block b : optG) { 
                
                if (b.ocean) { 
                    result[b.blockID-1] = b.getMark()+1;
                } else {
                    result[b.blockID-1] = 0;
                }
            }
            
            Distribution d = new Distribution(topologyWidth, topologyHeight, blockWidth, blockHeight, 
                    1, 4, 64, 6, 7, gridWidth * gridHeight, result);
            
            
            DistributionViewer sv1 = new DistributionViewer(d, t, g, true);
            sv1.drawBlocks();
            sv1.drawNodes();
            sv1.drawCores();

            
            //
//            DistributionViewer sv2 = new DistributionViewer(lb.getDistribution(g.width, g.height, t.width, t.height), t, g, true);
//            sv2.drawBlocks();
//            sv2.drawNodes();
//            sv2.drawCores();
            
            //            DistributionViewer sv = new DistributionViewer(distribution, top.getOptimizedTopography(), top.getOptimizedGrid(), true);

            d.write("test.dist");
          
        } catch (Exception e) {
            System.err.println("Failed " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }
}
