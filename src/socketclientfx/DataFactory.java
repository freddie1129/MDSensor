/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package socketclientfx;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PipedOutputStream;
import static java.lang.Math.PI;
import static java.lang.Math.sin;
import java.util.*;
import static socketclientfx.ConsVar.PointsNum;


/**
 *
 * @author freddie
 */
class TestSignal {
    double dFre;
    double dFs;
    char[] strTag;
    String tag;
    public TestSignal(double fre,double fs,char [] str )
    {
        strTag = new char[4];
        System.arraycopy(str, 0, strTag, 0, 4);
        dFre = fre;
        dFs = fs;
        tag = String.valueOf(str); //  .format("%s",Arrays.toString(str));
    }
}
class DataFactory extends Thread {

    public double gfre;
    public double fs;
    public double phase = 0;
    private double step;
    public int blockLen;            //length of each frame
    public PipedOutputStream pipeOut;

    List<TestSignal> SigFreList = new ArrayList<TestSignal>();

    public DataFactory() {
        pipeOut = new PipedOutputStream();
    }

    //public void addSig(double fre) {
    //    
    //    SigFreList.add(fre);
    //}
    public void addSig(double fre,double fs,char []tag) {
        
        TestSignal tempTestSignal = new TestSignal(fre, fs, tag);
        SigFreList.add(tempTestSignal);
        for (int i = 0 ; i < SigFreList.size(); i ++)
        {
            System.out.printf("Test: %f %f %s\n", SigFreList.get(i).dFre, SigFreList.get(i).dFs , String.valueOf(SigFreList.get(i).strTag));
        }
        System.out.printf("========================\n");
    }
    
   // public void suspend() {
   //     wait();
   // }
    
    public void play() {
        notify();
    }
    
    public void ResetFactory()
    {
        if (SigFreList.size() != 0) {
            SigFreList.clear();
        }
    }

    public DataFactory(double fre, double fs) {
        gfre = fre;
        step = 2 * PI * fre / fs;
        this.fs = fs;
    }

    public void setBlockLen(int l) {
        blockLen = l;
    }

    public void setFs(double fs) {
        this.fs = fs;
    }

    public void init(double fre, double fs) {
        pipeOut = new PipedOutputStream();
        gfre = fre;
        step = 2 * Math.PI * fre / fs;
        blockLen = PointsNum;
        System.out.printf("PointsNum = %d \n",blockLen);
    }

    public double[] getData(int len) {
        double[] s = new double[len];
        for (int i = 0; i < len; i++) {
            phase += step;
            phase = phase % (2 * PI);
            s[i] = sin(phase);
        }
        return s;
    }

    public byte[] Doubls2Bytes(double[] values) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        for (int i = 0; i < values.length; ++i) {
            try {
                dos.writeDouble(values[i]);
            } catch (IOException e) {

            }
        }
        return baos.toByteArray();
    }

    //convert double samples of multi-channel to bytes array
    public byte[] Doubls2Bytes(double[][] values,String tag) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        int nRow = values.length;
        int nColumn = values[0].length;
        for (int i = 0; i < nColumn; i++) {
            for (int k = 0; k < nRow; k++) {
                try {

                    dos.writeDouble(values[k][i]);
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
        }
        return baos.toByteArray();
    }

    public void run() {

        int numSig = SigFreList.size();
        double[][] s = new double[numSig][blockLen];
        double[] phase = new double[numSig];
        
        
        double [] temp = {25,26,25,27,28,28,28,27,28,28,30,31,31,32,33,33,34,31,29,28,27,26,25,24,23,22,21,20,19,16,15,15,15,14,15,16,17,18,19,20,21,22,23,24,25};
                          
        double [] hum ={46,47,48,49,50,51,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,65,67,68,65,64,63,62,50,49,48,47,46,45,44,43,43,41,40,30};
        double [] person = {60,60,70,75,70,80,60,89,88,88,82,32,61,68,70,75,70,80,60,89,88,88,82,32,};
        int    [] air = {1,2,3,4,5,6,7,8,1,1,1,2,2,3,4,5,9,5,5,7,6};
        int []  smoke = {1,1,1,1,1,1,1,1,1,1,0};
        
        
        if (numSig <= 0) {
            System.out.println("ERROR:No any Signal is added into this factory!");
        }
        
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        int indexT = 0;
        int indexH = 0;
        int indexP = 0;
        int indexA = 0;
        int indexS = 0;
        int indexT1 = 0;
        int indexH1 = 0;
        int indexP1 = 0;
        int indexA1 = 0;
        int indexS1 = 0;
        while (true) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                System.out.println(e);
            }
            for (int k = 0; k < numSig; k++) {
                double fd = SigFreList.get(k).dFre;
                double pstep = 2 * PI * fd / SigFreList.get(k).dFs;
                for (int i = 0; i < blockLen; i++) {
                    phase[k] += pstep;
                    phase[k] = phase[k] % (2 * PI);
                    //s[k][i] = sin(phase[k]);
                    String strtag = SigFreList.get(k).tag;
                    
                    
                    if (strtag.equals("A001"))
                    {
                         //dos.writeDouble(values[k][i]);
                        try{
                            dos.writeDouble(temp[indexT]);
                            if (indexT == temp.length -1)
                            {
                                indexT = 0;
                            }
                            else
                            {
                                indexT++;
                            }
                          // indexT = (indexT == (temp.length - 1)) ? 0 :  (indexT++);                            
                        }
                        catch(IOException e) {
                             System.out.println(e);
                         }
                    }
                    else if (strtag.equals("A006"))
                    {

                        try{                            
                            dos.writeDouble(temp[indexT1]);
                            if (indexT1 == temp.length -1)
                            {
                                indexT1 = 0;
                            }
                            else
                            {
                                indexT1++;
                            }
                           // indexT1 = indexT1 == (temp.length - 1) ? 0 :  indexT1++;                            
                        }
                        catch(IOException e) {
                             System.out.println(e);
                         }
                    }
                    else if (strtag.equals("A002"))
                    {
                        try{                            
                            dos.writeDouble(hum[indexH]);
                            
                            if (indexH == hum.length -1)
                            {
                                indexH = 0;
                            }
                            else
                            {
                                indexH++;
                            }
                         //   indexH = indexH == (hum.length - 1) ? 0 :  indexH++;                            
                        }
                        catch(IOException e) {
                             System.out.println(e);
                         } 
                    }
                    else if (strtag.equals("A007"))
                    {
                        try{                            
                            dos.writeDouble(hum[indexH1]);
                                                       if (indexH1 == hum.length -1)
                            {
                                indexH1 = 0;
                            }
                            else
                            {
                                indexH1++;
                            }                        
                        }
                        catch(IOException e) {
                             System.out.println(e);
                         } 
                    }
                    else if (strtag.equals("A003"))
                    {
                                                try{                            
                            dos.writeDouble(person[indexP]);
                                                        if (indexP == person.length -1)
                            {
                                indexP = 0;
                            }
                            else
                            {
                                indexP++;
                            }
                            //indexP = indexP == (person.length - 1) ? 0 :  indexP++;                            
                        }
                        catch(IOException e) {
                             System.out.println(e);
                         } 
                    }
                    else if (strtag.equals("A008"))
                    {
                                                                       try{                            
                            dos.writeDouble(person[indexP1]);
                            
                             if (indexP1 == person.length -1)
                            {
                                indexP1 = 0;
                            }
                            else
                            {
                                indexP1++;
                            }
                          //  indexP1 = indexP1 == (person.length - 1) ? 0 :  indexP1++;                            
                        }
                        catch(IOException e) {
                             System.out.println(e);
                         } 
                    }
                    
                    else if (strtag.equals("A004"))
                    {
                            try{                            
                            dos.writeInt(air[indexA]);
                            dos.writeInt(0);
                             if (indexA == air.length -1)
                            {
                                indexA = 0;
                            }
                            else
                            {
                                indexA++;
                            }
                            //indexA = indexA == (person.length - 1) ? 0 :  indexA++;                            
                        }
                        catch(IOException e) {
                             System.out.println(e);
                         } 
                    }
                    else if (strtag.equals("A009"))
                    {
                                                                                               try{                            
                            dos.writeInt(air[indexA1]);
                            dos.writeInt(0);
                            if (indexA1 == air.length -1)
                            {
                                indexA1 = 0;
                            }
                            else
                            {
                                indexA1++;
                            }
                           // indexA1 = indexA1 == (person.length - 1) ? 0 :  indexA1++;                            
                        }
                        catch(IOException e) {
                             System.out.println(e);
                         } 
                    }
                    else if (strtag.equals("A005"))
                    {
                                                                                                                      try{                            
                            dos.writeInt(smoke[indexS]);
                            dos.writeInt(0);
                           // indexS = indexS == (smoke.length - 1) ? 0 :  indexS++;    
                           if (indexS == smoke.length - 1)
                           {
                               indexS = 0;
                           }
                           else
                           {
                               indexS++;
                           }
                        }
                        catch(IOException e) {
                             System.out.println(e);
                         } 
                    }
                    else if (strtag.equals("A010"))
                    {
                                                                                                                      try{                            
                            dos.writeInt(smoke[indexS1]);
                            dos.writeInt(0);
                           // indexS1= indexS1 == (smoke.length - 1) ? 0 :  indexS1++; 
                           if (indexS1 == smoke.length - 1)
                           {
                               indexS1 = 0;
                           }
                           else
                           {
                               indexS1++;
                           }                           
                        }
                        catch(IOException e) {
                             System.out.println(e);
                         } 
                    }
                    else
                    {
                        try{
                             dos.writeInt(0);
                            dos.writeInt(0);
                        }
                        catch (Exception ee)
                        {
                            System.out.println(ee);
                        }
                    }
                        
                }
            }
            
            
            byte[] outData =  baos.toByteArray(); // Doubls2Bytes(s,strtag);
            baos.reset();

            
            try {
                pipeOut.write(outData, 0, outData.length);
                System.out.printf("Output %d\n",outData.length);
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }
}
