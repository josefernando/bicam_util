package br.com.bicam.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Stack;

/**
 *  The <tt>Stopwatch</tt> data type is for measuring
 *  the time that elapses between the start and end of a
 *  programming task (wall-clock time).
 *
 *  See {@link StopwatchCPU} for a version that measures CPU time.
 *
 *  @author Robert Sedgewick
 *  @author Kevin Wayne
 */


public class Watch { 

    private long start;
    private long interval;

    private Stack<Long> blockElapsedTime;

    /**
     * Initialize a stopwatch object.
     */
    public Watch() {
        start = System.currentTimeMillis();
        blockElapsedTime = new Stack<Long>();
    } 

    /**
     * Returns the elapsed time (in seconds) since this object was created.
     */
    public double elapsedTime() {
        long now = System.currentTimeMillis();
        double elapsedTime = (now - start) / 1000.0;
        start = now;
        interval = now;
        return elapsedTime;
    }
    
    public double elapsedTimeInterval() {
        long now = System.currentTimeMillis();
        double elapsedTimeInterval = (now - interval) / 1000.0;
        interval = now;
        return elapsedTimeInterval;
    }    
   
    public void startBlockElapsedTime() {
    	blockElapsedTime.push(System.currentTimeMillis());
    }
    
    public double endBlockElapsedTime() {
        long now = System.currentTimeMillis();
        double elapsedTime = (now - blockElapsedTime.pop()) / 1000.0;
        return elapsedTime;
    }    
    
    public String getDateTime(Long currentTimeMillis){
	    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");    
	    Date resultdate = new Date(currentTimeMillis);
	    return sdf.format(resultdate);
    }
} 
