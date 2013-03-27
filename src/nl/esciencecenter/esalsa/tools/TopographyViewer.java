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

import javax.swing.JFrame;

import nl.esciencecenter.esalsa.util.Coordinate;
import nl.esciencecenter.esalsa.util.Grid;
import nl.esciencecenter.esalsa.util.Line;
import nl.esciencecenter.esalsa.util.Topography;
import nl.esciencecenter.esalsa.util.TopographyCanvas;

/**
 * TopographyViewer is an application that displays a POP topography and block distribution.
 *  
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public class TopographyViewer {

	/** 
	 * Main entry point into application. 
	 * 
	 * @param args the command line arguments provided by the user. 
	 */
	public static void main(String [] args) { 

		if (args.length < 5) { 
			System.out.println("Usage: TopographyViewer topography_file topography_width topography_height " + 
					"block_width block_height [--showGUI] [--image image.png]\n" + 
					"\n" + 
					"Read a topography file of topography_width x topography_height, and divide it into blocks of size " +
					"block_width x block_height. Optionally, the result can be shown in a graphical interface or saved in an " +
					"image.\n" + 
					"\n" + 
					"  topography_file       a topography file that contains the index of the deepest ocean level at " + 
					"each gridpoint.\n" + 
					"  topography_width      the width of the topography.\n" + 
					"  topography_height     the heigth of the topography.\n" +
					"  block_width           the width of the blocks.\n" + 
					"  block_height          the heigth of the blocks.\n" + 
					"  [--showGUI]           show the result in a graphical interface.\n" + 
					"  [--showWork]          color blocks according to work.\n" + 					
					"  [--image image.png]   store the result in a png image.\n");
			
			System.exit(1);
		}

		String topographyFile = args[0];
		int width = Utils.parseInt("topography_width", args[1], 1);
		int height = Utils.parseInt("topography_height", args[2], 1);

		int blockWidth = Utils.parseInt("block_width", args[3], 1);
		int blockHeight = Utils.parseInt("block_width", args[4], 1);

		boolean showGUI = false;
		boolean showWork = false;
		
		String output = null;
		
		for (int i=5;i<args.length;i++) { 
			
			if (args[i].equals("--showGUI")) { 
				showGUI = true;
			} else if (args[i].equals("--showWork")) { 
				showWork = true;
			} else if (args[i].equals("--image")) {
				
				if ((i+1) < args.length) { 
					output = args[i+1];
					i++;
				} else { 
					Utils.fatal("Option \"--image\" requires parameter!");
				}
			} else { 
				Utils.fatal("Unknown option " + args[i]);
			}
		}
		
		try { 			
			Topography t = new Topography(width, height, topographyFile);
			Grid g = new Grid(t, blockWidth, blockHeight);

			if (!showGUI && output == null) {
				// Nothing to do!
				return;
			}
			
			Color c = new Color(128, 128, 128, 128);

			TopographyCanvas tc = new TopographyCanvas(t, g);
			
			tc.addLayer("BLOCKS");
			tc.addLayer("LINES");
						
			tc.draw("LINES", new Line(new Coordinate(0, 0), new Coordinate(0, g.height)), c, 7.0f);
			tc.draw("LINES", new Line(new Coordinate(0, 0), new Coordinate(g.width, 0)), c, 7.0f);
			
			tc.draw("LINES", new Line(new Coordinate(0, g.height), new Coordinate(g.width, g.height)), c, 7.0f);
			tc.draw("LINES", new Line(new Coordinate(g.width, 0), new Coordinate(g.width, g.height)), c, 7.0f);
			
			for (int i=1;i<g.width;i++) {
				tc.draw("LINES", new Line(new Coordinate(i, 0), new Coordinate(i, g.height)), c, 7.0f);	
			}
			
			for (int i=1;i<g.height;i++) {
				tc.draw("LINES", new Line(new Coordinate(0,i), new Coordinate(g.width,i)), c, 7.0f);	
			}

			if (showWork) { 
				for (int y=0;y<g.height;y++) { 
					for (int x=0;x<g.width;x++) { 
						if (g.get(x,y) == null) { 
							tc.fillBlock("BLOCKS", x, y, Color.BLACK);
						} else { 
							tc.fillBlock("BLOCKS", x, y, Color.WHITE);
						}
					}
				}
			}
			
			if (showGUI) { 
				JFrame frame = new JFrame("Topograpy");
				frame.setSize(1000, 667);
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.getContentPane().add(tc);
				frame.setVisible(true);			
				tc.repaint();
			}
			
			if (output != null) { 
				tc.save(output);
			}
			
		} catch (Exception e) {
			Utils.fatal("Failed to run TopographyViewer ", e);
		}
	}
}
