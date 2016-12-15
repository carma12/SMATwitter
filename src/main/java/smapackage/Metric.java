package smapackage;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author edelval
 */
public class Metric implements Comparable {
    
    
    private double m;
    private double d;
    
    public double getM() {
        return m;
    }

    public void setM(double m_) {
        this.m = m_;
    }

    public double getD() {
        return d;
    }

    public void setD(double d_) {
        this.d = d_;
    }
    
    Metric(double m_, double d_){
    
        m = m_;
        d = d_;
        
        
    }
    
    public int compareTo(Object t){
        Metric o = (Metric) t;  
        if(this.m > o.getM()){
            return 1;
        }else{
            if(this.m < o.getM())
                return -1;
            else{
                if(this.d > o.getD()){
                    return -1;
                }else{
                    if(this.d < o.getD())
                        return 1;
                    else
                        return 0;
                }
            }
        }
    }

   
    
}
