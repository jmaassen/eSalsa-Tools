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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import nl.esciencecenter.esalsa.util.Distribution;

public class TextToDistribution {

	public static void main(String [] args) { 
		
		if (args.length != 2) { 			
			System.err.println("Usage: TextToDistribution [text-file] [distribution file]");
			System.exit(1);
		}
		
		BufferedReader r = null;
		
		try { 
			r = new BufferedReader(new FileReader(new File(args[0])));
		} catch (IOException e) {  
			System.err.println("Failed to open input file: " + args[0]);
			System.err.println(e.getMessage());
			e.printStackTrace(System.err);
			System.exit(1);
		}
		
		Distribution d = null;
		
		try { 
			int topologyWidth = Integer.parseInt(r.readLine().trim());
			int topologyHeight = Integer.parseInt(r.readLine().trim());
			
			int blockWidth = Integer.parseInt(r.readLine().trim());
			int blockHeight = Integer.parseInt(r.readLine().trim());

			int clusters = Integer.parseInt(r.readLine().trim());
			int nodesPerCluster = Integer.parseInt(r.readLine().trim());
			int coresPerNode = Integer.parseInt(r.readLine().trim());
			
			int minBlocksPerCore = Integer.parseInt(r.readLine().trim());
			int maxBlocksPerCore = Integer.parseInt(r.readLine().trim());
			
			int totalBlocks = Integer.parseInt(r.readLine().trim());
			
			int [] distribution = new int[totalBlocks];
			
			for (int i=0;i<totalBlocks;i++) { 
				distribution[i] = Integer.parseInt(r.readLine().trim());
			}

			r.close();
			
			d = new Distribution(topologyWidth, topologyHeight, blockWidth, blockHeight, 
					clusters, nodesPerCluster, coresPerNode, minBlocksPerCore, maxBlocksPerCore, totalBlocks, distribution);

			d.write(args[1]);
			
		} catch (IOException e) { 		
			System.err.println("Failed to read input file: " + args[0]);
			System.err.println(e.getMessage());
			e.printStackTrace(System.err);
			System.exit(1);			
		}
	}
	
	
}
