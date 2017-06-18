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
    public TestSignal(double fre,double fs,char [] str )
    {
        strTag = new char[4];
        System.arraycopy(str, 0, strTag, 0, 4);
        dFre = fre;
        dFs = fs;
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
    public byte[] Doubls2Bytes(double[][] values) {
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
        if (numSig <= 0) {
            System.out.println("ERROR:No any Signal is added into this factory!");
        }
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
                    s[k][i] = sin(phase[k]);

                }
            }
            byte[] outData = Doubls2Bytes(s);
            try {
                pipeOut.write(outData, 0, outData.length);
                System.out.printf("Output %d\n",outData.length);
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }
}
