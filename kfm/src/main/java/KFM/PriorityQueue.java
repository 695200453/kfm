package kfm.src.main.java.KFM;

import java.util.*;

public class PriorityQueue {
    private List<PQEntry> heap;

    public PriorityQueue() {
        this.heap = new ArrayList<>();
    }

    public void insert(Vertex vertex, double gain, Partition from, Partition to) {
        PQEntry entry = new PQEntry(vertex, gain, from, to);
        heap.add(entry);
        bubbleUp(heap.size() - 1);
    }

    public PQEntry popMax() {
        if (heap.isEmpty()) {
            return null;
        }

        PQEntry max = heap.get(0);
        PQEntry last = heap.remove(heap.size() - 1);

        if (!heap.isEmpty()) {
            heap.set(0, last);
            bubbleDown(0);
        }

        return max;
    }

    public boolean isEmpty() {
        return heap.isEmpty();
    }

    public void clear() {
        heap.clear();
    }

    public int size() {
        return heap.size();
    }

    private void bubbleUp(int index) {
        while (index > 0) {
            int parentIndex = (index - 1) / 2;
            if (heap.get(parentIndex).gain >= heap.get(index).gain) {
                break;
            }
            swap(parentIndex, index);
            index = parentIndex;
        }
    }

    private void bubbleDown(int index) {
        int length = heap.size();

        while (true) {
            int leftChild = 2 * index + 1;
            int rightChild = 2 * index + 2;
            int largest = index;

            if (leftChild < length && heap.get(leftChild).gain > heap.get(largest).gain) {
                largest = leftChild;
            }

            if (rightChild < length && heap.get(rightChild).gain > heap.get(largest).gain) {
                largest = rightChild;
            }

            if (largest == index) {
                break;
            }

            swap(index, largest);
            index = largest;
        }
    }

    private void swap(int i, int j) {
        PQEntry temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }
}
