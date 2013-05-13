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

package nl.esciencecenter.esalsa.loadbalancer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import nl.esciencecenter.esalsa.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A SearchSplit is an extension of a RoughlyRectangularSplit that generates all valid roughly rectangular grid of sets and tests
 * each of them to see which solution offers the minimal amount of communication between nodes.
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 * 
 */
public class SearchSplit extends RoughlyRectangularSplit {

    /** A logger used for debugging. */
    private static final Logger logger = LoggerFactory.getLogger(SearchSplit.class);

    private class Solution {
        final Set[] solution;
        final int[] permutation;
        final int communication;
        
        Solution(Set[] solution, int[] permutation, int communication) {
            this.solution = solution;
            this.permutation = permutation;
            this.communication = communication;
        }
    }

    /**
     * Create a new SearchSplit for a given set, number of subsets and neigbour function.
     * 
     * @param set
     *            the set to split
     * @param subsets
     *            the number of subsets to create.
     */
    public SearchSplit(Set set, int subsets) {

        super(set, subsets);

        if (set.size() < subsets) {
            throw new IllegalArgumentException("Cannot split set with " + set.size() + " work into " + subsets + " parts!");
        }
    }

    private int getCommunicationSum(Set[] sets) {

        int communication = 0;

        for (int i = 0; i < sets.length; i++) {
            communication += sets[i].getCommunication();
        }
        
        return communication;
    }

    /*
    private int getCommunicationMax(Set[] sets) {

        int max = 0;

        for (int i = 0; i < sets.length; i++) {
          
            int tmp = sets[i].getCommunication();
            
            if (tmp > max) { 
                max = tmp;
            }
        }
         
        return max;
    }

/*    
    private Set[] findBestSplitFourWays(Set set, int[] workPerSlice) {

        // We now have 4 options here: >, <, ^, v
        Set[][] solutions = new Set[4][];

        solutions[0] = splitHorizontal(set, workPerSlice, false);
        solutions[1] = splitHorizontal(set, workPerSlice, true);
        solutions[2] = splitVertical(set, workPerSlice, false);
        solutions[3] = splitVertical(set, workPerSlice, true);

        // Now select the best of the four
        Set[] best = solutions[0];
        int bestCommunication = getCommunicationSum(solutions[0]);
        int bestMaxCommunication = getCommunicationMax(solutions[0]);
        
        if (logger.isDebugEnabled()) {
            logger.debug("   solution[0] " + bestCommunication);
        }

        System.out.println("Set best to " + 0);
        System.out.println("   solution[0] " + bestCommunication + " " + bestMaxCommunication);
        
        for (int i = 1; i < 4; i++) {

            int tmpSum = getCommunicationSum(solutions[i]);
            int tmpMax = getCommunicationMax(solutions[i]);
            
            if (logger.isDebugEnabled()) {
                logger.debug("   solution[" + i + "] " + tmpSum);
            }

            System.out.println("   solution[" + i + "] " + tmpSum + " " + tmpMax);

            if (tmpSum < bestCommunication) {
                best = solutions[i];
                bestCommunication = tmpSum;
                bestMaxCommunication = tmpMax;
                
                System.out.println("Set best to " + i);
                
            } else if (tmpSum == bestCommunication && tmpMax < bestMaxCommunication) {
                best = solutions[i];
                bestCommunication = tmpSum;
                bestMaxCommunication = tmpMax;
                
                System.out.println("Set best to " + i);

            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("   best solution -- " + bestCommunication);
        }
        
        System.out.println("   best solution -- " + bestCommunication + "  " + bestMaxCommunication);

        return best;
    }
*/
    
    @SuppressWarnings("rawtypes")
    private void findBestSplit(Set set, int[] workPerSlice, Collection<Solution> solutions) {

        // We should test all permutations of workPerSlice here.
        ArrayList permutations = new ArrayList();
        getIndexPermutations(workPerSlice.length, permutations);
        
        System.out.println("Permutations generated: " + permutations.size());
        
        HashMap<Integer, ArrayList<int []>> cache = new HashMap<Integer, ArrayList<int []>>();
        
        for (int i = 0; i < permutations.size(); i++) {

            int[] perm = (int[]) permutations.get(i);
            int[] work = new int[workPerSlice.length];

            for (int j = 0; j < workPerSlice.length; j++) {
                work[j] = workPerSlice[perm[j]];
            }

            int hashcode = Arrays.hashCode(work);
            
            ArrayList<int []> array = cache.get(hashcode);
         
            boolean seen = false;
            
            if (array == null) { 
                array = new ArrayList<int[]>();
                array.add(work);
                cache.put(hashcode, array);
           
            } else { 
                
                for (int [] a : array) { 
                    if (Arrays.equals(work, a)) { 
                        seen = true;
                        break;
                    }
                }
        
                if (!seen) { 
                    array.add(work);
                }
            }
            
            if (!seen) { 

                if (logger.isDebugEnabled()) {
                    logger.debug(" TESTING: " + Arrays.toString(perm) + " " + Arrays.toString(work));
                }

                System.out.println("TESTING: " + Arrays.toString(perm) + " " + Arrays.toString(work));
                                               
                Set [] tmp = splitHorizontal(set, work, false);
                int communication = getCommunicationSum(tmp);
                solutions.add(new Solution(tmp, perm, communication));

                tmp = splitHorizontal(set, work, true);
                communication = getCommunicationSum(tmp);
                solutions.add(new Solution(tmp, perm, communication));

                tmp = splitVertical(set, work, false);
                communication = getCommunicationSum(tmp);
                solutions.add(new Solution(tmp, perm, communication));

                tmp = splitVertical(set, work, true);
                communication = getCommunicationSum(tmp);
                solutions.add(new Solution(tmp, perm, communication));
            }
        }
    }

    private int[] swap(int[] input, int i, int j) {
        int[] copy = input.clone();
        copy[i] = input[j];
        copy[j] = input[i];
        return copy;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void getPermutation(int[] input, int start, Collection output) {

        if (start == input.length) {
            output.add(input.clone());
            return;
        }

        getPermutation(input, start + 1, output);

        for (int i = start + 1; i < input.length; i++) {
            if (input[start] != input[i]) {
                input = swap(input, start, i);
                getPermutation(input, start + 1, output);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private void getIndexPermutations(int len, Collection output) {

        int[] index = new int[len];

        for (int i = 0; i < index.length; i++) {
            index[i] = i;
        }

        getPermutation(index, 0, output);
    }

    /**
     * Split the set into <code>subSlices.length</code> subsets, after which each subset <code>i</code> is split into
     * <code>subSlices[i]</code> subsets. These subsets are that stored in the collection.
     * 
     * @param subSlices
     *            array describing how the set should be split.
     * @param result
     *            a Collection in which the resulting subsets are stored.
     */
    protected void split(int[] subSlices, Collection<Set> result) {

        if (logger.isDebugEnabled()) {
            logger.debug("Splitting set of size " + set.size() + " into " + Arrays.toString(subSlices));
        }

        int[] workPerSlice = splitWork(set.size(), subSlices, parts);

        if (logger.isDebugEnabled()) {
            logger.debug(" Work per slice: " + Arrays.toString(workPerSlice));
        }

        ArrayList<Solution> solutions = new ArrayList<Solution>();
        
        findBestSplit(set, workPerSlice, solutions);

        Solution best = null;
        int bestCommunication = Integer.MAX_VALUE;
        int maxCommunication = Integer.MAX_VALUE;
        
        for (Solution s : solutions) { 
            
            System.out.println("\n\nTESTING SOLUTION " + Arrays.toString(s.permutation) + " " + s.communication);
       
            ArrayList<Solution> solutions2 = new ArrayList<Solution>();
            
            for (int i = 0; i < s.solution.length; i++) {

                ArrayList<Solution> solutions3 = new ArrayList<Solution>();
                
                if (logger.isDebugEnabled()) {
                    logger.debug("Splitting SUB " + i + " ---------------------");
                }

                int[] workPerPart = splitWork(s.solution[i].size(), subSlices[s.permutation[i]]);

                System.out.println("Splitting SUB " + i + " " + Arrays.toString(workPerPart));
                
                findBestSplit(s.solution[i], workPerPart, solutions3);

                int bestComm = Integer.MAX_VALUE;
                int bestMax = Integer.MAX_VALUE;
                Solution tmp = null;
                
                System.out.println("Splitting SUB gave " + solutions3.size() + " results");
                
                for (Solution s3 : solutions3) { 
                    
                    int sum = 0;
                    int max = 0;
                    
                    for (Set set : s3.solution) { 
                        
                        int comm = set.getCommunication();
                        
                        if (comm > max) { 
                            max = comm;
                        }
                        
                        sum += comm;
                    }

                    if (sum < bestComm) { 
                        bestComm = sum;
                        bestMax = max;
                        tmp = s3;
                    } else if (sum == bestComm) { 
                        if (max < bestMax) { 
                            bestComm = sum;
                            bestMax = max;
                            tmp = s3;
                        }
                    }
                    
//                    if (s3.communication < bestComm) { 
//                        bestComm = s3.communication;
//                        tmp = s3;
//                    }
                }

                System.out.println(" BEST SUB " + bestComm + " " + bestMax + " " + tmp.communication);
                
                solutions2.add(tmp);
                
//                for (int j = 0; j < tmp.solution.length; j++) {
//                    result.add(tmp.solution[j]);
//                }
            }
      
            int min = Integer.MAX_VALUE; 
            int max = 0;
            int sum = 0;
            int count = 0;
            
            for (Solution sub : solutions2) {
                
                for (Set set : sub.solution) { 
                    
                    int comm = set.getCommunication(); 
                    
                    if (comm > max) { 
                        max = comm;
                    }
                    
                    if (comm < min) { 
                        min = comm;
                    }
                    
                    sum += comm;
                    count++;
                }
            }

            System.out.println("RESULT OF " + Arrays.toString(s.permutation) + " " + " " + 
                    s.communication + " " + max + " " + sum + 
                    " " + min + " " + count + " " + bestCommunication);

            if (sum  < bestCommunication) { 
                bestCommunication = sum;
                maxCommunication = max;
                best = s;
            } else if (sum == bestCommunication) { 
                if (max < maxCommunication) { 
                    bestCommunication = sum;
                    maxCommunication = max;
                    best = s;
                }
            }
        }
         
        System.out.println("\n\n****");
        System.out.println("BEST SOLUTION: " + Arrays.toString(best.permutation) + " " + best.communication + " -> " + 
                bestCommunication);

        // RECOMPUTE BEST RESULT!
        
        ArrayList<Solution> solutions2 = new ArrayList<Solution>();
        
        for (int i = 0; i < best.solution.length; i++) {
            
            ArrayList<Solution> solutions3 = new ArrayList<Solution>();
            
            if (logger.isDebugEnabled()) {
                logger.debug("Splitting SUB " + i + " ---------------------");
            }

            int[] workPerPart = splitWork(best.solution[i].size(), subSlices[best.permutation[i]]);

            findBestSplit(best.solution[i], workPerPart, solutions3);

            int bestComm = Integer.MAX_VALUE;
            Solution tmp = null;
            
            for (Solution s3 : solutions3) { 
                if (s3.communication < bestComm) { 
                    bestComm = s3.communication;
                    tmp = s3;
                }
            }
            
            solutions2.add(tmp);
        } 
        
        for (Solution tmp : solutions2) {     
            for (int j = 0; j < tmp.solution.length; j++) {
                result.add(tmp.solution[j]);
            }
        }
    }
}
