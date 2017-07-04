/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package socketclientfx;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author freddie
 */
//public class Frequency{
//    private final SimpleIntegerProperty num = new SimpleIntegerProperty();
//    private final SimpleDoubleProperty fre = new SimpleDoubleProperty();
//    private final SimpleDoubleProperty fs = new SimpleDoubleProperty();
//    
//    public Frequency(int n, double fo, double fs)
//    {
//        setNum(n);
//        setFre(fo);
//        setFs(fs);
//        
//    }
//    public void setNum(int n)
//    {
//        num.set(n);
//    }
//    public void setFre(double d)
//    {
//        fre.set(d);
//    }
//    public void setFs(double d)
//    {
//        fs.set(d);
//    }
//    public int getNum()
//    {
//        return num.get();
//    }
//    public double getFre()
//    {
//        return fre.get();
//    }
//    public double getFs()
//    {
//        return fs.get();
//    }   
//}


public class Frequency{
    private final SimpleIntegerProperty num = new SimpleIntegerProperty();
    private final SimpleDoubleProperty fre = new SimpleDoubleProperty();
    private final SimpleDoubleProperty fs = new SimpleDoubleProperty();
    private final SimpleStringProperty tag = new SimpleStringProperty();
    
    public Frequency() {
     //   this(1, 0, 0);
    }
    public Frequency(int n, double fo, double fs,String tag)
    {
        setNum(n);
        setFre(fo);
        setFs(fs);
        setTag(tag);
        
    }
    
    public void setNum(int n)
    {
        num.set(n);
    }
    public void setFre(double d)
    {
        fre.set(d);
    }
    public void setFs(double d)
    {
        fs.set(d);
    }
    public void setTag(String t)
    {
        tag.set(t);
    }
    public int getNum()
    {
        return num.get();
    }
    public double getFre()
    {
        return fre.get();
    }
    public double getFs()
    {
        return fs.get();
    }   
    public String getTag()
    {
        return tag.get();
    }   
}
