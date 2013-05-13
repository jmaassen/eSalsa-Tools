package nl.esciencecenter.esalsa.tools;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class CreateTestTopo {

    private static int BLOCK_SIZE_X = 100;
    private static int BLOCK_SIZE_Y = 100;
    
    public static void main(String [] args) throws IOException { 

        int [][] topo = new int[10][12];
        
        for (int y=0;y<10;y++) { 
            for (int x=0;x<12;x++) { 
                topo[y][x] = 1;  
            }    
        }
        
        topo[0][3] = 0;
        topo[0][4] = 0;
        topo[0][10] = 0;
        topo[0][11] = 0;
        
        topo[1][10] = 0;
        topo[1][11] = 0;
        
        topo[2][11] = 0;

        topo[7][0] = 0;
        topo[7][9] = 0;
        topo[7][10] = 0;
        
        topo[8][0] = 0;
        topo[8][1] = 0;
        topo[8][10] = 0;
        
        
        topo[9][0] = 0;
        topo[9][1] = 0;
        topo[9][2] = 0;
        topo[9][3] = 0;
        topo[9][4] = 0;
        topo[9][10] = 0;
        topo[9][11] = 0;

        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(new FileOutputStream("test.topo")));
        
        for (int y=9;y>=0;y--) {
            
            for (int by=0;by<BLOCK_SIZE_Y;by++) {
                for (int x=0;x<12;x++) {
                    for (int bx=0;bx<BLOCK_SIZE_X;bx++) {
                        dout.writeInt(topo[y][x]);
                    }
                }
            }
        }
        
        dout.flush();
        dout.close();
    }
}
