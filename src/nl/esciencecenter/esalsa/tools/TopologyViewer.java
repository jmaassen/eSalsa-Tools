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
import nl.esciencecenter.esalsa.util.Topology;
import nl.esciencecenter.esalsa.util.TopologyCanvas;

public class TopologyViewer {

	/** 
	 * Main entry point into application. 
	 * 
	 * @param args the command line arguments provided by the user. 
	 */
	public static void main(String [] args) { 

		if (args.length < 5) { 
			System.out.println("Usage: TopologyViewer topologyFile width height blockWidth blockHeight");
			System.exit(1);
		}

		String topologyFile = args[0];
		int width = Integer.parseInt(args[1]);
		int height = Integer.parseInt(args[2]);

		int blockWidth = Integer.parseInt(args[3]);
		int blockHeight = Integer.parseInt(args[4]);
		
		String output = args[5];
		
		try { 			
			Topology t = new Topology(width, height, topologyFile);
			
			Grid g = new Grid(t, blockWidth, blockHeight);
			TopologyCanvas tc = new TopologyCanvas(t, g);
			
			tc.addLayer("BLOCKS");
			tc.addLayer("LINES");
			
			
			JFrame frame = new JFrame("Topology");
			frame.setSize(1000, 667);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.getContentPane().add(tc);
			frame.setVisible(true);			
			
			tc.draw("LINES", new Line(new Coordinate(0, 0), new Coordinate(0, g.height)), Color.GRAY, 7.0f);
			tc.draw("LINES", new Line(new Coordinate(0, 0), new Coordinate(g.width, 0)), Color.GRAY, 7.0f);
			
			tc.draw("LINES", new Line(new Coordinate(0, g.height), new Coordinate(g.width, g.height)), Color.GRAY, 7.0f);
			tc.draw("LINES", new Line(new Coordinate(g.width, 0), new Coordinate(g.width, g.height)), Color.GRAY, 7.0f);
			
			for (int i=1;i<g.width;i++) {
				tc.draw("LINES", new Line(new Coordinate(i, 0), new Coordinate(i, g.height)), Color.GRAY, 7.0f);	
			}
			
			for (int i=1;i<g.height;i++) {
				tc.draw("LINES", new Line(new Coordinate(0,i), new Coordinate(g.width,i)), Color.GRAY, 7.0f);	
			}
			
			for (int y=0;y<g.height;y++) { 
				for (int x=0;x<g.width;x++) { 
					if (g.get(x,y) == null) { 
						tc.fillBlock("BLOCKS", x, y, Color.BLACK);
					} else { 
						tc.fillBlock("BLOCKS", x, y, Color.WHITE);
					}
				}
			}
			
			frame.repaint();
			
			tc.save(output);
			
		} catch (Exception e) {
			System.err.println("Failed to run DsitributionViewer " + e);
			e.printStackTrace(System.err);
		}
	}
	
	
}
