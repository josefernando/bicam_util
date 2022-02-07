package br.com.bicam.util;
import java.util.ArrayList;
import java.util.Arrays;

public class MinHeap {

    private ArrayList<Double> list;

    public MinHeap() {

        this.list = new ArrayList<Double>();
    }

    public MinHeap(ArrayList<Double> items) {

        this.list = items;
        buildHeap();
    }

    public void insert(double item) {

        list.add(item);
        int i = list.size() - 1;
        int parent = parent(i);

        while (parent != i && list.get(i) < list.get(parent)) {

            swap(i, parent);
            i = parent;
            parent = parent(i);
        }
    }

    public void buildHeap() {

        for (int i = list.size() / 2; i >= 0; i--) {
            minHeapify(i);
        }
    }

    public double extractMin() {

        if (list.size() == 0) {

            throw new IllegalStateException("MinHeap is EMPTY");
        } else if (list.size() == 1) {

            double min = list.remove(0);
            return min;
        }

        // remove the last item ,and set it as new root
        double min = list.get(0);
        double lastItem = list.remove(list.size() - 1);
        list.set(0, lastItem);

        // bubble-down until heap property is maintained
        minHeapify(0);

        // return min key
        return min;
    }

    public void decreaseKey(int i, double key) {

        if (list.get(i) < key) {

            throw new IllegalArgumentException("Key is larger than the original key");
        }

        list.set(i, key);
        int parent = parent(i);

        // bubble-up until heap property is maintained
        while (i > 0 && list.get(parent) > list.get(i)) {

            swap(i, parent);
            i = parent;
            parent = parent(parent);
        }
    }

    private void minHeapify(int i) {

        int left = left(i);
        int right = right(i);
        int smallest = -1;

        // find the smallest key between current node and its children.
        if (left <= list.size() - 1 && list.get(left) < list.get(i)) {
            smallest = left;
        } else {
            smallest = i;
        }

        if (right <= list.size() - 1 && list.get(right) < list.get(smallest)) {
            smallest = right;
        }

        // if the smallest key is not the current key then bubble-down it.
        if (smallest != i) {

            swap(i, smallest);
            minHeapify(smallest);
        }
    }

    public double getMin() {

        return list.get(0);
    }

    public boolean isEmpty() {

        return list.size() == 0;
    }

    private int right(int i) {

        return 2 * i + 2;
    }

    private int left(int i) {

        return 2 * i + 1;
    }

    private int parent(int i) {

        if (i % 2 == 1) {
            return i / 2;
        }

        return (i - 1) / 2;
    }

    private void swap(int i, int parent) {

        double temp = list.get(parent);
        list.set(parent, list.get(i));
        list.set(i, temp);
    }
    
    public String toString() {
    	StringBuffer sb = new StringBuffer();
    	for(Double i : list) {
    		sb.append(i);
    		sb.append(" ");
    	}
    	return sb.toString();
    }
    
    public static void main(String[] args ) {
    	Double[] i = new Double[] {10.0,2.0,3.0,7.0,1.0,20.0 };
    	ArrayList<Double> ar = new ArrayList(Arrays.asList(i));
    	MinHeap mp = new MinHeap(ar);
    	mp.buildHeap();
    	System.err.println(mp.toString());
    	while(!mp.isEmpty()) {
    		System.err.println(mp.extractMin());
    	}
    }
}