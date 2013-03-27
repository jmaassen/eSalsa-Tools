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

/**
 * This package contains the eSalsa LoadBalancer application which is used to generate a block distribution for the 
 * Parallel Ocean Program (POP). 
 * <p>
 * This block distribution is based on the ocean bottom topography, the desired block size, and the desired number of clusters, 
 * nodes per cluster, and cores per node. The LoadBalancer then attempts to generate a block distribution that:
 * <p>
 * <ul>
 * <li>distributes the work equally over the available cores.</li> 
 * <li>attempts to minimize the amount of communication required between cluster, nodes, and cores.</li>
 * </ul>  
 * <p>
 * This distribution can them be stored in a file which can be read by POP. If desired, the distribution can also be displayed on 
 * screen or saved in a PNG image. In addition, statistics on work distribution and communication for individual elements  
 * (blocks, cores, nodes, and clusters) can be shown graphically or printed on the console.    
 * 
 */
package nl.esciencecenter.esalsa.loadbalancer;

// Rest of file is left intentionally blank.
