package renaudIA.datastructure;

import java.util.HashMap;

/**
 * Mutable Minimum Value Queue
 * based on a classic binary heap
 * implemented as described in
 * <a href="https://www.researchgate.net/publication/222329184_An_efficient_agglomerative_clustering_algorithm_using_a_heap">
 *     this paper</a>
 * @param <T> type of the object you want to sort
 * I use two hashmap which references a double[] index with access in O(1)
 * rather than a LinkedList which has access time in O(n)
 * adding an element is less efficient O(n) beacause we should copy the array
 * but sorting is quicker
 * If you add multiple times an object with same hashCode, the second reference will erase the first
 * Can store a maximum of (2^31 - 1) objects = max size of an int
 * Not recommended for a lot of data (will work but not efficient when adding new objects)
 * Can be used with different kind of values (Integer, Long, Float, Double)
 *           but the precision of the value stored is a double.
 * Be aware that sorting will not change if you change the return value of the method after adding the object
 *
 */
public class MutableMinimumValueQueue<T> {
    private final HashMap<Integer, T> currentObject;
    private final HashMap<T, Integer> currentPosition;
    private double[] binaryHeap;
    private ValueGetter<T, Number> valueGetter;
    private int currentLastIndex;

    /**
     * interface for setting the method using for sorting the structure
     * @param <T> the class of the object to sort
     * @param <R> the return value of the method
     */
    @FunctionalInterface
    public interface ValueGetter<T, R extends Number> {
        R getValue(T obj);
    }

    // public methods to manipulate the queue as needed

    public MutableMinimumValueQueue() {
        currentObject = new HashMap<>();
        currentPosition = new HashMap<>();

        currentLastIndex = 0;
        binaryHeap = new double[currentLastIndex + 1];
    }

    /**
     * specifying the method of the object T to use
     * for ex : str -> str.length() if you use String as generic type
     */
    public void setValueGetter(ValueGetter<T, Number> valueGetter) {
        this.valueGetter = valueGetter;
    }

    /**
     * adding an object to the data structure
     * @param object the object of T class to use
     */
    public void add(T object) {
        if (valueGetter == null) throw new RuntimeException("You should first initialize a value getter method");
        if (currentLastIndex + 1 == Integer.MAX_VALUE) throw new RuntimeException("Two many objects in the heap");

        // first we need to remove the object in the heap to avoid lost references
        this.remove(object);

        // we increment first because we don't want to reference the 0 position (otherwise sorting will fail)
        currentLastIndex++;

        // we make a copy of the heap to add a new item, not very efficient but ok
        double[] newHeap = new double[currentLastIndex + 1];
        System.arraycopy(binaryHeap, 0, newHeap, 0, currentLastIndex);
        binaryHeap = newHeap;

        currentPosition.put(object, currentLastIndex);
        currentObject.put(currentLastIndex, object);

        // we put the new value at the end and we sort it
        binaryHeap[currentLastIndex] = valueGetter.getValue(object).floatValue();
        shiftup(currentLastIndex);
    }

    /**
     * remove an object to the data structure by passing the object
     * @param object the object of T class to use
     * @return true if the object was remove, false if not (typically if it was not finded)
     */
    public boolean remove(T object) {
        Integer indexToRemove = currentPosition.get(object);

        if (indexToRemove == null) return false;

        // for removing, we just swap the last value and the value removed
        double removedValue = binaryHeap[indexToRemove];
        double lastValue = binaryHeap[currentLastIndex];

        swapValues(currentLastIndex, indexToRemove);

        currentPosition.remove(object);
        currentObject.remove(currentLastIndex);

        // we do not modify the heap but just decrement the lastIndex for efficiency
        currentLastIndex--;

        binaryHeap[indexToRemove] = lastValue;
        if (removedValue < lastValue) {
            shiftdown(indexToRemove);
        }
        else if (removedValue > lastValue) {
            shiftup(indexToRemove);
        }

        return true;
    }


    /**
     * get the object with the minimum value of the specified method
     * remove the object from the data structure
     * @return the object, null if the data structure is empty
     * @throws RuntimeException if the getter method was not specified
     */
    public T getMinObject() {
        if (valueGetter == null) throw new RuntimeException("You should first initialize a value getter method");

        T firstObject = currentObject.get(1);
        this.remove(firstObject);

        return firstObject;
    }

    /**
     * @return true if the queue is empty, false otherwise
     */
    public boolean isEmpty() {
        return currentPosition.isEmpty();
    }

    /**
     * @return the number of elements in the queue
     */
    public int size() {
        return currentPosition.size();
    }

    /**
     * method used only for debug purpose
     * verify that the first value is the smaller one
     * loop on the heap to verify that a value stored at '2 * index' is always higher than value stored at 'index'
     * @return true if the heap is well sorted, false otherwise
     */
    public boolean checkHeap() {
        double firstValue = binaryHeap[1];
        for (int i = 0; i <= currentLastIndex; i++) {
            if (binaryHeap[i] < firstValue) {
                return false;
            }
            if ((2 * i) > currentLastIndex) continue;
            if (binaryHeap[2 * i] < binaryHeap[i]) {
                return false;
            }
        }
        return true;
    }

    // private methods to sort the data structure

    /**
     * method for sort the whole heap
     * not used because we sort for every add() calls
     * but can be useful if you want for example change
     */
    private void sortHeap() {
        int indexModifie = (currentLastIndex / 2) + 1;
        while (indexModifie > 1) {
            indexModifie--;
            shiftdown(indexModifie);
        }
    }

    /**
     * move an element down in the heap
     * @param indexElement index of the element to move
     */
    private void shiftdown(int indexElement) {
        int i = indexElement;
        int j = 2 * i;
        double valeur = binaryHeap[i];
        T modifiedObject = currentObject.get(i);

        int e = currentLastIndex;
        // we loop on the down
        while (j <= e) {
            if (j < e) {
                if (binaryHeap[j] > binaryHeap[j+1]) j++;
            }
            if (valeur <= binaryHeap[j]) break;

            swapValues(j, i);

            i = j;
            j = 2 * i;
        }

        binaryHeap[i] = valeur;
        currentPosition.put(modifiedObject, i);
        currentObject.put(i, modifiedObject);
    }

    /**
     * move an element up in the heap
     * @param indexElement index of the element to move
     */
    private void shiftup(int indexElement) {
        int i = indexElement;
        int j = i / 2;
        double valeur = binaryHeap[indexElement];
        T modifiedObject = currentObject.get(i);

        while(j >= 1) {
            if (binaryHeap[j] <= valeur) break;

            swapValues(j, i);

            i = j;
            j = i / 2;
        }

        binaryHeap[i] = valeur;
        currentPosition.put(modifiedObject, i);
        currentObject.put(i, modifiedObject);
    }

    /**
     * swapping two values and saving the new place of the first one
     * we don't need to store the second one because it can move
     * @param j index to move
     * @param i index where we move
     */
    private void swapValues(int j, int i) {
        binaryHeap[i] = binaryHeap[j];
        T storedObject = currentObject.get(j);
        currentPosition.put(storedObject, i);
        currentObject.put(i, storedObject);
    }


    /**
     * naive testing implementation
     */
    public static void main(String[] args) {
        MutableMinimumValueQueue<String> queue = new MutableMinimumValueQueue<>();
        queue.setValueGetter(String::length);

        String mysteryFruit = "pineapple";
        String mangoString = "mango";
        String cocoString = "coco";
        String bananaString = "banana";

        // Add elements
        queue.add(mysteryFruit);
        queue.add(cocoString);
        queue.add(mangoString);
        queue.add(bananaString);

        // "coco" is the first
        String shortestString = queue.getMinObject();
        System.out.println("Should be coco : " + shortestString);

        // Remove specific element
        System.out.println("Should be false : " + queue.remove(cocoString));
        System.out.println("Should be true : " + queue.remove(bananaString));
        System.out.println("Should be 2 : " + queue.size());

        // "mango is the first"
        shortestString = queue.getMinObject();
        System.out.println("Should be mango : " + shortestString);

        // dynamically change the value of a string and refresh the heap
        String figFruit = "fig";
        queue.add(figFruit);

        // fig is the first
        shortestString = queue.getMinObject();
        System.out.println("Should be fig : " + shortestString);

        // should be ananas
        shortestString = queue.getMinObject();
        System.out.println("Should be pineapple : " + shortestString);

        shortestString = queue.getMinObject();
        System.out.println("Should be null : " + shortestString);
    }
}
