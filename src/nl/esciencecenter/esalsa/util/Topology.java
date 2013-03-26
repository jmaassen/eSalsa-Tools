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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Topology represents a topology used as input for POP. 
 * 
 * A topology is stored as a matrix of integer values of size width x height. A value 
 * of 0 at any position indicates a land point, while a value > 0 indicates the depth 
 * level of the ocean at the given position.  
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 * 
 */
public class Topology {

	/** A logger used for debugging */
	private static final Logger logger = LoggerFactory.getLogger(Topology.class);
	
	/** The topology data as read from the input file */
	private final int [][] topology;
	
	/** The width of the topology */
	public final int width;
	
	/** The height of the topology */
	public final int height;
	
	/** The maximum value found in the topology */
	public final int max;
	
	/** The minimum value found in the topology */
	public final int min;
	
	/** 
	 * Create a new topology by reading the data from the input file.  
	 * 
	 * @param width the width of the topology to create.
	 * @param height the height of the topology to create.
	 * @param inputfile the input file from which to read the topology data.
	 * @throws Exception if the topology could not be created. 
	 */
	public Topology(int width, int height, String inputfile) throws Exception {
	
		int tmpMax = Integer.MIN_VALUE;
		int tmpMin = Integer.MAX_VALUE;
		
		this.width = width;
		this.height = height;
		topology = new int[width][height];

		int work = 0;
		long sum = 0;
		
		DataInputStream in = null;
		
		try { 
			in = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(inputfile))));

			for (int y=0;y<height;y++) { 
				for (int x=0;x<width;x++) {
					int tmp = in.readInt();

					//topology[x][height-y-1] = tmp;
					topology[x][y] = tmp;

					if (tmp > tmpMax) { 
						tmpMax = tmp;
					}

					if (tmp < tmpMin) { 
						tmpMin = tmp;
					}

					if (tmp > 0) { 
						work++;
					}

					sum += tmp;
				}
			}

			in.close();
		} catch (Exception e) { 
			throw new Exception("Failed to initialize Topology from file " + inputfile, e);
		} finally { 
			try { 
				in.close();
			} catch (Exception e) {
				// ignored
			}
		}
			
		max = tmpMax;
		min = tmpMin;		
		
		if (logger.isDebugEnabled()) { 
			logger.debug("Topology contains " + work + " nonzero fields (sum = " + sum + ")");
		}
	}

	/** 
	 * Create a new topology from existing data.  
	 * 
	 * @param data a square int matrix containing the data to store in the topology.
	 * @throws Exception if the topology could not be created. 
	 */
	public Topology(int [][] data) throws Exception {
	
		int tmpMax = Integer.MIN_VALUE;
		int tmpMin = Integer.MAX_VALUE;
		
		this.width = data.length;
		this.height = data[0].length;
		
		int work = 0;
		long sum = 0;
		
		topology = data.clone(); 
		
		for (int y=0;y<height;y++) { 
			for (int x=0;x<width;x++) {
					
				int tmp = topology[x][y];

				if (tmp > tmpMax) { 
					tmpMax = tmp;
				}

				if (tmp < tmpMin) { 
					tmpMin = tmp;
				}

				if (tmp > 0) { 
					work++;
				}

				sum += tmp;
			}
		}
			
		max = tmpMax;
		min = tmpMin;		
		
		if (logger.isDebugEnabled()) { 
			logger.debug("Topology contains " + work + " nonzero fields (sum = " + sum + ")");
		}
	}
	
	/** 
	 * Create a new topology by down scaling an existing one.
	 * 
	 * The down scaling will be performed block wise. The original topology will be divided in blocks of size 
	 * (blockWidth x blockHeight). Each block will become a single point in the new topology. The value of this point will be the 
	 * sum of all values in the block in the original topology.       
	 *  
	 * @param orig the original topology. 
	 * @param blockWidth the block width to use for down scaling.  
	 * @param blockHeight the block height to use for down scaling.
	 * @throws Exception if the topology could not be scaled down. 
	 */
	public Topology(Topology orig, int blockWidth, int blockHeight) throws Exception {
		
		int tmpMax = Integer.MIN_VALUE;
		int tmpMin = Integer.MAX_VALUE;
		
		if (orig.width % blockWidth != 0) { 
			throw new Exception("Illegal blockWidth");
		}
		
		if (orig.height % blockHeight != 0) { 
			throw new Exception("Illegal blockHeight");
		}
		
		this.width = orig.width / blockWidth;
		this.height = orig.height / blockHeight;
		topology = new int[width][height];
		
		int work = 0;
		long sum = 0;
		
		for (int y=0;y<height;y++) { 
			for (int x=0;x<width;x++) {

				int tmp = orig.getRectangleSum(x*blockWidth, y*blockHeight, blockWidth, blockHeight);
				 
				topology[x][y] = tmp;
						
				if (tmp > tmpMax) { 
					tmpMax = tmp;
				}
				
				if (tmp < tmpMin) { 
					tmpMin = tmp;
				}
				
				if (tmp > 0) { 
					work++;
				}
				
				sum += tmp;
			}
		}
		
		max = tmpMax;
		min = tmpMin;		
		
		if (logger.isDebugEnabled()) { 
			logger.debug("Topology contains " + work + " nonzero fields (sum = " + sum + ")");
		}
	}

	/** 
	 * Returns the maximum value found in a rectangular area of the topology.  
	 * 
	 * @param x the x position of the rectangle. 
	 * @param y the y position of the rectangle.
	 * @param w the width of the rectangle.
	 * @param h the height of the rectangle.
	 * @return the maximum value found in the specified rectangular area.
	 */
	public int getRectangleMax(int x, int y, int w, int h) {
		
		int max = Integer.MIN_VALUE;
		
		for (int i=x;i<x+w && i<width;i++) {
			for (int j=y;j<y+h && j<height;j++) {
				int tmp = topology[i][j];
				if (tmp > max) { 
					max = tmp;
				}
			}
		}
		
		return max;
	}
	
	/** 
	 * Returns the average value found in a rectangular area of the topology.  
	 * 
	 * @param x the x position of the rectangle. 
	 * @param y the y position of the rectangle.
	 * @param w the width of the rectangle.
	 * @param h the height of the rectangle.
	 * @return the average value found in the specified rectangular area.
	 */
	public int getRectangleAvg(int x, int y, int w, int h) {
	
		int sum = 0;
		int count = 0;
		
		for (int i=x;i<x+w && i<width;i++) {
			for (int j=y;j<y+h && j<height;j++) {
				sum += topology[i][j];
				count++;
			}
		}
		
		if (count > 0) { 
			return sum/count;
		}
		
		return 0;
	}
	
	/** 
	 * Returns the sum of all values found in a rectangular area of the topology.  
	 * 
	 * @param x the x position of the rectangle. 
	 * @param y the y position of the rectangle.
	 * @param w the width of the rectangle.
	 * @param h the height of the rectangle.
	 * @return the sum of all value in the specified rectangular area.
	 */
	public int getRectangleSum(int x, int y, int w, int h) {
		
		int sum = 0;
		
		for (int i=x;i<x+w && i<width;i++) {
			for (int j=y;j<y+h && j<height;j++) {
				sum += topology[i][j];
			}
		}
		
		return sum;
	}
	
	/** 
	 * Returns the amount of work (that is, non-0 values) found in a rectangular area of the topology.  
	 * 
	 * @param x the x position of the rectangle. 
	 * @param y the y position of the rectangle.
	 * @param w the width of the rectangle.
	 * @param h the height of the rectangle.
	 * @return the amount of work in the specified rectangular area.
	 */
	public int getRectangleWork(int x, int y, int w, int h) {
		
		int work = 0;
		
		for (int i=x;i<x+w && i<width;i++) {
			for (int j=y;j<y+h && j<height;j++) {
				if (topology[i][j] > 0) {
					work++;
					//work += topology[i][j];
				}
			}
		}
		
		return work;
	}

	/**
	 * Retrieves the value of a specific location of the topology. 
	 * 
	 * @param x the x coordinate of the value to retrieve.
	 * @param y the y coordinate of the value to retrieve.
	 * @return the value found at the specified location in the topology.
	 */
	public int get(int x, int y) {
		return topology[x][y];
	}
}
