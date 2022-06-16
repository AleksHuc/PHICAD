package utils;

import java.util.TreeMap;

/**
 * This class implements an object that presents a landmark window.
 */
public class HitWindow {

    private int windowLength;
    public TreeMap<Long,Double> window;

    /**
     * The constructor creates new HitWindow object from a given parameter.
     * @param windowLength int value that presents the window length.
     */
    public HitWindow (int windowLength) {
        this.windowLength = windowLength;
        this.window = new TreeMap<>();
    }

    /**
     * The method adds and new value for a given key in the landmark window.
     * @param key long value that presents the key or timestamp of when an event occurred.
     * @param value double value that presents the value of an event that occurred.
     */
    public void add(long key, double value){
        this.window.put(key, value);
        this.window.headMap(key - windowLength).clear();
    }

    /**
     * The method returns the number of events that have occurred since the given time parameter.
     * @param key long value that presents the current time.
     * @param size int value that presents the time offset.
     * @return int value thar presents the number of events that have occurred since the give time and offset.
     */
    public int newestSinceSize(long key, int size) {
        return this.window.tailMap(key - size).size();
    }

}
