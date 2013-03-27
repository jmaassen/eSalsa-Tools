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

import nl.esciencecenter.esalsa.util.Distribution;
import nl.esciencecenter.esalsa.util.Grid;
import nl.esciencecenter.esalsa.util.Layer;
import nl.esciencecenter.esalsa.util.Layers;
import nl.esciencecenter.esalsa.util.Neighbours;
import nl.esciencecenter.esalsa.util.Set;
import nl.esciencecenter.esalsa.util.Topography;

public class PrintStatistics {

	private final Layers layers;
	private final Neighbours neighbours;
	
	/**
	 * Create a PrintStatistics for a given set of layers.
	 * 
	 * @param layers the layer for which the statistics must be printed.
	 * @param neighbours the neighbour function to use.
	 */
	public PrintStatistics(Layers layers, Neighbours neighbours) { 
		this.layers = layers;
		this.neighbours = neighbours;
	}
	
	/** 
	 * Print statistics on work distribution and communication in <code>layer</code> to console. 
	 *   
	 * @param layer the layer to print statistics for. 
	 */
	private void printStatistics(Layer layer) { 
		
		if (layer == null) { 
			return;
		}

		System.out.println("Statistics for layer: " + layer.name);		
		System.out.println("  Sets: " + layer.size());
	
		for (int i=0;i<layer.size();i++) { 
			Set tmp = layer.get(i);
			System.out.println("   " + i + " (" + tmp.minX + "," + tmp.minY + ") - (" + tmp.maxX + "," + tmp.maxY + ") " + 
			tmp.size() + " " + tmp.getCommunication(neighbours));
		}
	}

	/** 
	 * Print statistics on work distribution and communication in <code>layer</code> to console. 
	 *   
	 * @param layer the name of the layer to print statistics for, or <code>ALL</code> to print statistics on all layers.  
	 */
	public void printStatistics(String layer) throws Exception {
		
		if (layer.equalsIgnoreCase("ALL")) { 
			printStatistics(layers.get("CLUSTERS"));
			printStatistics(layers.get("NODES"));
			printStatistics(layers.get("CORES"));
		} else { 
			Layer l = layers.get(layer);

			if (l == null) { 
				throw new Exception("Layer " + layer + " not found!");
			}
			
			printStatistics(l);
		}
	}	
	
	public static void main(String [] args) { 

		try { 			
			Distribution d = new Distribution(args[0]);
			
			Topography t = new Topography(d.topographyWidth, d.topographyHeight, args[1]);
			Grid g = new Grid(t, d.blockWidth, d.blockHeight);
			
			Neighbours n = new Neighbours(g, d.blockWidth, d.blockHeight, Neighbours.CYCLIC, Neighbours.TRIPOLE);
		
			PrintStatistics p = new PrintStatistics(d.toLayers(), n);

			p.printStatistics(args[2]);
			
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace(System.err);
		}
	}
}
