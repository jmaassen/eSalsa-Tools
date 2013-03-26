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

import nl.esciencecenter.esalsa.loadbalancer.LoadBalancer;
import nl.esciencecenter.esalsa.util.Distribution;
import nl.esciencecenter.esalsa.util.Grid;
import nl.esciencecenter.esalsa.util.Layers;
import nl.esciencecenter.esalsa.util.Neighbours;
import nl.esciencecenter.esalsa.util.Topology;

/**
 * This is the main entry point into the eSalsa POP LoadBalancer. 
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 *
 */
public class LoadBalancing {

	/** The topologyWidth as set by user */
	private static int topologyWidth = -1;
	
	/** The topologyHeight as set by user */
	private static int topologyHeight = -1;
	
	/** The blockWidth as set by user */
	private static int blockWidth = -1;
	
	/** The blockHeight as set by user */
	private static int blockHeight = -1;

	/** The number of clusters as set by user */
	private static int clusters = 1;
	
	/** The number of nodes per cluster as set by user */
	private static int nodesPerCluster = -1;
	
	/** The number of cores per node as set by user */
	private static int coresPerNode = -1;

	/** Should we show the GUI ? */
	private static boolean showGUI = false;
	
	/** Statistics to print */
	private static String statistics = null;
	
	/** File name of topology input file to load */ 
	private static String topologyFile = null; 
	
	/** File name of output distribution file to write */
	private static String outputDistribution = null; 
	
	/** File name of output image file to write */
	private static String outputImage = null; 

	/** Name of the split methods to use */
	private static String splitMethod = "roughlyrect";
	
	/** 
	 * Check if enough parameters are available for the current option. 
	 * If not, an error is printed and the application is terminated.  
	 * 
	 * @param option the current command line option. 
	 * @param parameters the number of parameters required.
	 * @param index the current index in the command line parameter list.  
	 * @param length the length of the command line parameter list.
	 */
	private static void checkOptions(String option, int parameters, int index, int length) { 
		if (index+parameters >= length) { 
			System.err.println("Missing arguments for option " + option + " (required parameters " + parameters + ")");
			System.exit(1);
		}
	}
	
	/** 
	 * Print an error and terminate the application.  
	 * 
	 * @param error the error to print.
	 */
	private static void fatal(String error) { 
		System.err.println(error);
		System.exit(1);	 
	}
	
	/** 
	 * Print an error and terminate the application.  
	 * 
	 * @param e Exception that describes the fatal fault.
	 */
	private static void fatal(Exception e) { 
		System.err.println(e.getLocalizedMessage());
		e.printStackTrace(System.err);
		System.exit(1);	 
	}
	
	/** 
	 * Parse a string containing an integer and ensure its value is at least {@link minValue}.
	 * If the string cannot be parsed, or the integer is smaller than minValue, an error is printed 
	 * and the application is terminated. 
	 * 
	 * @param option the current command line option. 
	 * @param toParse the string to parse
	 * @param minValue the required minimal value for the integer. 
	 * @return the int value in the option string. 
	 */
	private static int parseInt(String option, String toParse, int minValue) { 
		
		int result = -1;
		
		try { 
			result = Integer.parseInt(toParse);
		} catch (Exception e) {
			fatal("Failed to read argument for option " + option + " (parameters " + toParse + " is not a number)");
		}
		
		if (result < minValue) { 
			fatal("Argument for option " + option + " must have a value of at least " + minValue + " (got " + result + ")");
		}
		
		return result;		
	}
	
	/**
	 * Print the usage on the console. 
	 */
	private static void usage() { 
		System.out.println(
				"Usage: nl.esciencecenter.esalsa.tools.LoadBalancing topology_file [OPTION]*\n" + 
				"\n" + 
				"Reads a POP topology file and determines a block distribution suitable for a number nodes each" +
				" containing a number of cores (and optionally divided into a number of clusters).\n" +
				"\n" + 
				"Mandatory arguments:\n" + 
				"   --grid WIDTH HEIGHT        dimensions of the topology file grid (WIDTHxHEIGHT).\n" + 
				"   --blocksize WIDTH HEIGHT   dimensions of the blocks to use (WIDTHxHEIGHT).\n" + 
				"   --nodes NODES              number of nodes for which the distribution must be calculated.\n" + 
				"   --cores CORES              number of cores in each node.\n" +
				"\n" + 
				"Optional arguments:\n" + 
				"   --clusters CLUSTERS        number of clusters to calculate ditribution for (default is 1).\n" +
				"   --output FILE              store the resulting distribution in FILE.\n" + 
				"   --image FILE               store an image of the resulting distribution in FILE.\n" + 
				"   --statistics LAYER         print statistics on the resulting distribution on layer LAYER. Valid" +
				" value for LAYER are CORES, NODES, CLUSTERS, ALL.\n" +
				"   --method METHOD            method used to distribute the blocks. Valid values for METHOD are" + 
				" SIMPLE, ROUGHLYRECT, and SEARCH. Default is ROUGHLYRECT." + 				
				"   --showgui                  show a graphical interface that allows the user to explore the distribution.\n" + 		
				"   --help                     show this help.");
		
			System.exit(0);
	}
	
	/** 
	 * Create  a LoadBalancer for the given topology and core configuration, run it, and save the desired output.   
	 */	
	private static void run() { 
			
		try { 
			Topology topology = new Topology(topologyWidth, topologyHeight, topologyFile);
			Grid grid = new Grid(topology, blockWidth, blockHeight);
			
			Neighbours neighbours = new Neighbours(grid, blockWidth, blockHeight, Neighbours.CYCLIC, Neighbours.TRIPOLE);
			
			Layers layers = new Layers();
			
			LoadBalancer lb = new LoadBalancer(layers, neighbours, topology, grid, blockWidth, blockHeight, 
					clusters, nodesPerCluster, coresPerNode, splitMethod);
			
			Distribution distribution = lb.split();
			
			if (showGUI || outputImage != null) {

				DistributionViewer sv = new DistributionViewer(distribution, topology, grid, neighbours, showGUI);					
				sv.drawBlocks();
					
				if (clusters > 1) { 
					sv.drawClusters();
				}
	
				if (nodesPerCluster > 1) { 
					sv.drawNodes();
				}
	
				if (coresPerNode > 1) { 
					sv.drawCores();
				}
				
				if (outputImage != null) { 
					try { 
						sv.save(outputImage);		
					} catch (Exception e) {
						System.err.println("Failed to save distribution to file " + outputDistribution + ": " 
								+ e.getLocalizedMessage());
					}
				}
			}	

			if (statistics != null) {
				try { 
					lb.printStatistics(statistics);
				} catch (Exception e) {
					System.err.println("Failed to print statistics " + statistics + ": " + e.getLocalizedMessage());
				}
			}

			if (outputDistribution != null) {
				try { 
					distribution.write(outputDistribution);
				} catch (Exception e) {
					System.err.println("Failed to save distribution to file " + outputDistribution + ": " + e);
					e.printStackTrace();
				}
			}
			
		} catch (Exception e) {
			fatal(e);
		}
	}
	
	/** 
	 * Main entry point into application. 
	 * 
	 * @param args the command line arguments provided by the user. 
	 */
	public static void main(String [] args) { 

		boolean topologySet = false;
		boolean blockSet = false;
		boolean nodesSet = false;
		boolean coresSet = false;
		
		if (args.length < 1) { 
			usage();
		}

		topologyFile = args[0];
		
		int index = 1;
		
		while (index < args.length) { 

			if (args[index].equals("--grid")) { 
				checkOptions("--grid", 2, index, args.length);
				topologyWidth = parseInt("--grid", args[index+1], 1);
				topologyHeight = parseInt("--grid", args[index+2], 1);
				index += 3;
				topologySet = true;
				
			} else if (args[index].equals("--blocksize")) { 
				checkOptions("--blocksize", 2, index, args.length);
				blockWidth = parseInt("--blocksize", args[index+1], 1);
				blockHeight = parseInt("--blocksize", args[index+2], 1);
				index += 3;
				blockSet = true;
				 
			} else if (args[index].equals("--clusters")) { 
				checkOptions("--clusters", 1, index, args.length);
				clusters = parseInt("--clusters", args[index+1], 1);
				index += 2;

			} else if (args[index].equals("--nodes")) { 
				checkOptions("--nodes", 1, index, args.length);
				nodesPerCluster = parseInt("--nodes", args[index+1], 1);
				index += 2;
				nodesSet = true;
				
			} else if (args[index].equals("--cores")) { 
				checkOptions("--cores", 1, index, args.length);
				coresPerNode = parseInt("--cores", args[index+1], 1);
				index += 2;
				coresSet = true;
				
			} else if (args[index].equals("--output")) { 
				checkOptions("--output", 1, index, args.length);
				outputDistribution = args[index+1];
				index += 2;

			} else if (args[index].equals("--image")) { 
				checkOptions("--image", 1, index, args.length);
				outputImage = args[index+1];
				index += 2;

			} else if (args[index].equals("--showGUI")) { 
				showGUI = true;
				index++;
			
			} else if (args[index].equals("--help")) { 
				usage();
				
			} else if (args[index].equals("--statistics")) { 
				checkOptions("--statistics", 1, index, args.length);
				statistics = args[index+1];
				index += 2;
				
			} else if (args[index].equals("--method")) { 
				checkOptions("--method", 1, index, args.length);
				splitMethod = args[index+1];
				index += 2;
				
			} else { 
				fatal("Unknown option: " + args[index]);
			}		
		}

		if (!topologySet) { 
			fatal("Please specify topology dimension using \"--grid WIDTH HEIGHT\"");
		}
		
		if (!blockSet) { 
			fatal("Please specify block dimension using \"--blocksize WIDTH HEIGHT\"");
		}
		
		if (!nodesSet) { 
			fatal("Please specify node count using \"--nodes NODES\"");
		}
		
		if (!coresSet) { 
			fatal("Please specify core per node using \"--cores CORES\"");
		}
		
		if (blockWidth > topologyWidth) { 
			fatal("Block width cannot be larger that grid width");
		}
		
		if (topologyWidth % blockWidth != 0) { 
			fatal("Block width must divide grid width equally!");
		}
		
		if (blockHeight > topologyHeight) { 
			fatal("Block height cannot be larger that grid height");
		}
		
		if (topologyHeight % blockHeight != 0) { 
			fatal("Block height must divide grid height equally!");
		}
		
		if (!showGUI && outputDistribution == null && outputImage == null && statistics == null) { 
			System.out.println("WARNING: This application will not produce any output, since none of the outputs is selected!");
		}
		
		run();
	}
}