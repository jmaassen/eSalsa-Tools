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

package nl.esciencecenter.esalsa.tools;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.swing.JFrame;

import nl.esciencecenter.esalsa.util.Block;
import nl.esciencecenter.esalsa.util.Coordinate;
import nl.esciencecenter.esalsa.util.Distribution;
import nl.esciencecenter.esalsa.util.Grid;
import nl.esciencecenter.esalsa.util.Layer;
import nl.esciencecenter.esalsa.util.Layers;
import nl.esciencecenter.esalsa.util.Line;
import nl.esciencecenter.esalsa.util.Neighbours;
import nl.esciencecenter.esalsa.util.Set;
import nl.esciencecenter.esalsa.util.Topography;
import nl.esciencecenter.esalsa.util.TopographyCanvas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DistributionViewer is an application used to interactively inspect a block distribution.
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 * @see Set
 * @see Block
 * @see Topography
 * @see TopographyCanvas
 * 
 */
public class DistributionViewer {

    /** Logger used for debugging. */
    private static final Logger logger = LoggerFactory.getLogger(DistributionViewer.class);

    /** The line color used for clusters */
    private static final Color LINE_COLOR_CLUSTER = new Color(0, 0, 255, 255);

    /** The color used for cluster ocean halos */
    private static final Color HALO_COLOR_CLUSTER = new Color(128, 64, 0, 96);

    /** The color used for cluster land halos */
    private static final Color LAND_COLOR_CLUSTER = new Color(0, 64, 128, 32);

    /** The line width used for clusters */
    private static final float LINE_WIDTH_CLUSTER = 15f;

    /** The line color used for nodes */
    private static final Color LINE_COLOR_NODE = new Color(255, 0, 0, 255);

    /** The color used for node ocean halos */
    private static final Color HALO_COLOR_NODE = new Color(160, 80, 0, 160);

    /** The color used for node land halos */
    private static final Color LAND_COLOR_NODE = new Color(0, 80, 160, 80);

    /** The line width used for nodes */
    private static final float LINE_WIDTH_NODE = 13f;

    /** The line color used for cores */
    private static final Color LINE_COLOR_CORE = new Color(0, 0, 0, 255);

    /** The color used for core ocean halos */
    private static final Color HALO_COLOR_CORE = new Color(255, 128, 0, 192);

    /** The color used for core land halos */
    private static final Color LAND_COLOR_CORE = new Color(0, 128, 255, 128);

    /** The line width used for cores */
    private static final float LINE_WIDTH_CORE = 13f;

    /** The line color used for blocks */
    private static final Color LINE_COLOR_BLOCK = Color.GRAY; //new Color(200, 200, 200, 255);

    /** The color used for block ocean halos */
    private static final Color HALO_COLOR_BLOCK = new Color(255, 128, 0, 255);

    /** The color used for block land halos */
    private static final Color LAND_COLOR_BLOCK = new Color(0, 128, 255, 255);

    /** The line width used for blocks */
    private static final float LINE_WIDTH_BLOCK = 5f;

    /** The color used for selected blocks */
    private static final Color CENTER = new Color(255, 0, 0, 210);

    /** The distribution to show */
    private final Distribution distribution;

    /** The topography to use. */
    private final Topography topography;

    /** A TopographyCanvas showing the topography */
    private final TopographyCanvas view;

    /** The Grid to use */
    private final Grid grid;

    /** The Layers containing the various subdivisions of blocks into subsets */
    private final Layers layers;

    /** Used the receive mouse clicks on the TopographyView */
    class MyListener implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent e) {
            try {
                clicked(e.getPoint());
            } catch (Exception ex) {
                logger.warn("Failed to select block!", ex);
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }
    }

    /**
     * Create a new DistributionViewer for the given topography, grid and layers.
     * 
     * @param distribution
     *            the distribution to show.
     * @param topography
     *            the topography to use.
     * @param grid
     *            the grid to use.
     * @param showGUI
     *            should the GUI be shown ?
     * @throws Exception
     *             if the DistributionViewer could not be initialized.
     */
    public DistributionViewer(Distribution distribution, Topography topography, Grid grid, boolean showGUI)
            throws Exception {

        this.distribution = distribution;
        this.topography = topography;
        this.grid = grid;
        this.layers = distribution.toLayers(grid);

        view = new TopographyCanvas(topography, grid);
        view.addLayer("BLOCKS");
        view.addLayer("CORES");
        view.addLayer("NODES");
        view.addLayer("CLUSTERS");
        view.addLayer("FILL");

        if (showGUI) {
            JFrame frame = new JFrame("Topography");
            frame.setSize(1000, 667);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(view);
            frame.setVisible(true);
            view.addMouseListener(new MyListener());
        }
        
        boolean showWork = true;
        
        if (showWork) {
            for (int y = 0; y < grid.height; y++) {
                for (int x = 0; x < grid.width; x++) {
                    
                    Block b = grid.get(x, y);
                    
                    if (!b.ocean) {
                        view.fillBlock("BLOCKS", x, y, Color.GRAY);
                    } else {
                        view.fillBlock("BLOCKS", x, y, Color.WHITE);
                    }
                }
            }
        }

    }

    public void repaint() {
        view.repaint();
    }

    public void addLayer(String name) throws Exception {
        view.addLayer(name);
    }

    public void clearLayer(String name) throws Exception {
        view.clearLayer(name);
    }

    public void fillBlock(String layer, int x, int y, Color color) throws Exception {
        view.fillBlock(layer, x, y, color);
    }

    public void draw(String layer, int x, int y, Color color) throws Exception {
        view.draw(layer, x, y, color);
    }

    /**
     * Color the edge of the <code>set</set> with the provided colors for <code>ocean</code> and <code>land</code>.
     * 
     * @param layer
     *            the layer at which the edge must be drawn.
     * @param s
     *            the set to draw the edge for.
     * @param ocean
     *            the color to use for ocean neighbors.
     * @param land
     *            the color to use for land neighbors.
     * @throws Exception
     *             if the edge could not be drawn.
     */
    private void colorEdge(String layer, Set s, Color ocean, Color land) throws Exception {

        if (s == null) {
            return;
        }

        int [] tmp = s.getNeighbours();

        for (int i=0;i<tmp.length;i++) {

            Block b = grid.get(tmp[i]);
            
            if (b.ocean) {
                view.fillBlock(layer, b.coordinate.x, b.coordinate.y, ocean);
            } else {
                view.fillBlock(layer, b.coordinate.x, b.coordinate.y, land);
            }
        }
    }

    /**
     * Callback function for the <code>MouseListener</code> attached to the TopographyView.
     * 
     * @param p
     *            the Point that has been clicked.
     * @throws Exception
     *             if the mouse click could not be processed.
     */
    private void clicked(Point p) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug("Click at " + p.x + " " + p.y);
        }

        double w = view.getWidth();
        double h = view.getHeight();

        int posX = (int) ((p.x / w) * topography.width);
        int posY = topography.height - (int) ((p.y / h) * topography.height);

        int bx = posX / grid.blockWidth;
        int by = posY / grid.blockHeight;

        int commBlock = -1;
        int commCore = -1;
        int commNode = -1;
        int commCluster = -1;

        int block = -1;
        int core = -1;
        int node = -1;
        int cluster = -1;

        /*
         		Coordinate c = new Coordinate(bx, by);
        		
        		int [][] comm = neighbours.getCommunication(c);

        		System.out.println("Communication on BLOCK layer: ");

        		for (int i=0;i<3;i++) { 
        			for (int j=0;j<3;j++) {
        				System.out.print(" " + comm[i][j]);
        			}
        			System.out.println();
        		}
        */

        System.out.println("Click at " + bx + " " +  by);

        Block b = grid.get(bx, by);

        System.out.println("Block " + b);
        
        System.out.println("Block neighbours");
        
        int [][] tmp = b.getNeighbours();
        
        System.out.println("  " + Arrays.toString(tmp[0]));
        System.out.println("  " + Arrays.toString(tmp[1]));
        System.out.println("  " + Arrays.toString(tmp[2]));
        
        System.out.println("Block comm");
        
        tmp = b.getCommunication();
        
        System.out.println("  " + Arrays.toString(tmp[0]));
        System.out.println("  " + Arrays.toString(tmp[1]));
        System.out.println("  " + Arrays.toString(tmp[2]));
        
        view.clearLayer("FILL");

        if (layers.contains("BLOCKS")) {

            Layer l = layers.get("BLOCKS");
            Set s = l.locate(bx, by);

            if (s != null) {
                block = s.index;
                commBlock = s.getCommunication();
                colorEdge("FILL", s, HALO_COLOR_BLOCK, LAND_COLOR_BLOCK);
            }
        }

        if (layers.contains("CORES")) {
            Layer l = layers.get("CORES");
            Set s = l.locate(bx, by);

            if (s != null) {
                core = s.index;
                commCore = s.getCommunication();
                colorEdge("FILL", s, HALO_COLOR_CORE, LAND_COLOR_CORE);
            }
        }

        if (layers.contains("NODES")) {
            Layer l = layers.get("NODES");
            Set s = l.locate(bx, by);

            if (s != null) {
                node = s.index;
                commNode = s.getCommunication();
                colorEdge("FILL", s, HALO_COLOR_NODE, LAND_COLOR_NODE);
            }
        }

        if (layers.contains("CLUSTERS")) {
            Layer l = layers.get("CLUSTERS");
            Set s = l.locate(bx, by);

            if (s != null) {
                cluster = s.index;
                commCluster = s.getCommunication();
                colorEdge("FILL", s, HALO_COLOR_CLUSTER, LAND_COLOR_CLUSTER);
            }
        }

        System.out.print("Selected");

        if (cluster >= 0) {
            System.out.print(" cluster " + cluster);
        }

        if (node >= 0) {
            System.out.print(" node " + node);
        }

        System.out.println(" core " + core + " block " + block + " (" + bx + "x" + by + ")");

        System.out.println("Communication on layer BLOCK " + commBlock);
        System.out.println("Communication on layer CORE " + commCore);
        System.out.println("Communication on layer NODE " + commNode);
        System.out.println("Communication on layer CLUSTER " + commCluster);

        /*
        Coordinate [][] tmp = neighbours.getNeighbours(c, true);

        for (int i=0;i<3;i++) { 
        	for (int j=0;j<3;j++) {
        		if (tmp[i][j] != null) { 

        			int nx = tmp[i][j].x;
        			int ny = tmp[i][j].y;

        			//System.out.println("Fill neighbour " + nx + "x" + ny);

        			if (grid.get(nx, ny) != null) { 
        				view.fillBlock("FILL", nx, ny, HALO_COLOR_BLOCK);
        			} else { 
        				view.fillBlock("FILL", nx, ny, LAND_COLOR_BLOCK);
        			}
        		}
        	}
        }
        */

        view.fillBlock("FILL", bx, by, CENTER);
        view.repaint();
    }

    /**
     * Creates a HashMap containing all edges (Lines) of all Blocks in the given <code>set</code>.
     * <p>
     * Each edge in the HashMap maps to an Integer value which indicates how often the edge was encountered in the set. The edges
     * that have a count of <code>1</code> together form the outer edge of the set.
     * 
     * @param s
     *            the set for which to collect the edges.
     * @param out
     *            the HashMap to which the result will be added.
     */
    private void collectLines(Set s, HashMap<Line, Integer> out) {

        for (int i = 0; i < s.size(); i++) {

            Coordinate c = s.get(i).coordinate;

            if (c.x < grid.width) {
                addLine(out, new Line(c, c.offset(1, 0)));

                if (c.y < grid.height) {
                    addLine(out, new Line(c.offset(0, 1), c.offset(1, 1)));
                    addLine(out, new Line(c.offset(1, 0), c.offset(1, 1)));
                }
            }

            if (c.y < grid.height) {
                addLine(out, new Line(c, c.offset(0, 1)));
            }
        }
    }

    /**
     * Add a line to the HashMap.
     * 
     * If the line was not in the HashMap yet, it is added mapping to value <code>1</code>. Else the value it maps to is
     * incremented by one.
     * 
     * @param map
     *            the HashMap to add the line to.
     * @param line
     *            the Line to add.
     */
    private void addLine(HashMap<Line, Integer> map, Line line) {

        if (!map.containsKey(line)) {
            map.put(line, 1);
            return;
        }

        int count = map.get(line);
        map.put(line, count + 1);
    }

    /**
     * Draw all lines in the HashMap which map to the value <code>1</code> in the specified layer.
     * 
     * @param layer
     *            the layer to draw to.
     * @param map
     *            the collection of lines.
     * @param color
     *            the color to draw the lines in.
     * @param lineWidth
     *            the width of the lines to draw.
     * @throws Exception
     *             if the lines could not be drawn.
     */
    private void drawLines(String layer, HashMap<Line, Integer> map, Color color, float lineWidth) throws Exception {

        if (view == null) {
            return;
        }

        // We draw each line that has only been added to the map once. 
        for (Line l : map.keySet()) {

            int count = map.get(l);

            if (count == 1) {
                view.draw(layer, l, color, lineWidth);
            }
        }
    }

    private void drawLines(String layer, Line [] lines, Color color, float lineWidth) throws Exception {

        if (view == null) {
            return;
        }

        // We draw each line that has only been added to the map once. 
        for (Line l : lines) {
            view.draw(layer, l, color, lineWidth);
        }
    }

    
    /**
     * Draw the edges of all sets in the specified layer.
     * 
     * @param layer
     *            the layer to draw.
     * @param color
     *            the color to draw the lines in.
     * @param lineWidth
     *            the width of the lines to draw.
     * @throws Exception
     *             if the lines could not be drawn.
     */
    private void drawLayer(Layer l, Color color, float lineWidth) throws Exception {

        if (view == null || l == null) {
            return;
        }

        for (int i = 0; i < l.size(); i++) {

            Set s = l.get(i);

            //HashMap<Line, Integer> map = new HashMap<Line, Integer>();

            //collectLines(s, map);

            // drawLines(l.name, map, color, lineWidth);
            
            drawLines(l.name, s.getEdges(), color, lineWidth);
        }

        view.repaint();
    }

    /**
     * Draw the outline of all ocean blocks in the grid.
     * 
     * @throws Exception
     *             if the outline of the ocean blocks could not be drawn.
     */
    public void drawBlocks() throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug("Showing layer BLOCKS");
        }

        drawLayer(layers.get("BLOCKS"), LINE_COLOR_BLOCK, LINE_WIDTH_BLOCK);
    }

    /**
     * Draw the outline of all cluster sets.
     * 
     * @throws Exception
     *             if the outline of the cluster sets could not be drawn.
     */
    public void drawClusters() throws Exception {

        if (distribution.clusters == 1) { 
            return;
        }
        
        if (logger.isDebugEnabled()) {
            logger.debug("Showing layer CLUSTERS");
        }

        drawLayer(layers.get("CLUSTERS"), LINE_COLOR_CLUSTER, LINE_WIDTH_CLUSTER);
    }

    /**
     * Draw the outline of all node sets.
     * 
     * @throws Exception
     *             if the outline of the node sets could not be drawn.
     */
    public void drawNodes() throws Exception {

        if (distribution.nodesPerCluster == 1) { 
            return;
        }
        
        if (logger.isDebugEnabled()) {
            logger.debug("Showing layer NODES");
        }

        drawLayer(layers.get("NODES"), LINE_COLOR_NODE, LINE_WIDTH_NODE);
    }

    /**
     * Draw the outline of all core sets.
     * 
     * @throws Exception
     *             if the outline of the core sets could not be drawn.
     */
    public void drawCores() throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug("Showing layer CORES");
        }

        drawLayer(layers.get("CORES"), LINE_COLOR_CORE, LINE_WIDTH_CORE);
    }

    /**
     * Draw the outline of all blocks, cores, nodes and clusters
     * 
     * @throws Exception
     *             if the outline of the core sets could not be drawn.
     */
    public void drawAll() throws Exception {

        drawBlocks();
        drawCores();
        drawNodes();

        if (distribution.clusters > 1) {
            drawClusters();
        }
    }

    /**
     * Save the current image as a png file.
     * 
     * @param file
     *            the filename of the file to save.
     * @throws IOException
     *             if the file could not be saved.
     */
    public void save(String file) throws IOException {
        view.save(file);
    }

    /**
     * Main entry point into application.
     * 
     * @param args
     *            the command line arguments provided by the user.
     */
    public static void main(String[] args) {

        if (args.length < 2) {
            System.out.println("Usage: DistributionViewer topography_file distribution_file\n" + "\n"
                    + "Read a topography file and work distribution file and show a graphical interface that allows "
                    + "the user to interactively explore the work distribution.\n" + "\n"
                    + "  topography_file     a topography file that contains the index of the deepest ocean level at "
                    + "each gridpoint.\n" + "  distribution_file   a work distribution file.\n");
            System.exit(1);
        }

        String topographyFile = args[0];
        String distributionFile = args[1];

        try {
            Distribution d = new Distribution(distributionFile);
            Topography t = new Topography(d.topographyWidth, d.topographyHeight, topographyFile);
            
            int gridWidth = t.width / d.blockWidth;
            int gridHeight = t.height / d.blockHeight;
            
            Neighbours n = new Neighbours(t, gridWidth, gridHeight, d.blockWidth, d.blockHeight, 
                    Neighbours.CYCLIC, Neighbours.TRIPOLE);
            
            Grid g = new Grid(gridWidth, gridHeight, d.blockWidth, d.blockHeight, n);
            
            DistributionViewer dv = new DistributionViewer(d, t, g, true);

            dv.drawAll();

            dv.save("test.png");
            
        } catch (Exception e) {
            Utils.fatal("Failed to run DistributionViewer ", e);
        }
    }
}