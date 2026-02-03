public class Heap {

    private static final int DEFAULT_CAPACITY = 10;

    private int currentSize;
    private Freelancer[] array;

    public Heap() {
        this(DEFAULT_CAPACITY);
    }

    public Heap(int capacity) {
        currentSize = 0;
        array = new Freelancer[capacity + 1];
    }

    private int compareFreelancers(Freelancer f1, Freelancer f2) {
        if (f1.score > f2.score) {
            return -1;
        }
        if (f1.score < f2.score) {
            return 1;
        }
        String id1 = f1.id;
        String id2 = f2.id;
        int len1 = id1.length();
        int len2 = id2.length();
        int minLen = len1;
        if (len2 < len1) {
            minLen = len2;
        }
        for (int i = 0; i < minLen; i++) {
            char char1 = id1.charAt(i);
            char char2 = id2.charAt(i);
            if ((int)char1 < (int)char2) return -1;
            if ((int)char1 > (int)char2) return 1;
        }
        if (len1 < len2) return -1;
        if (len1 > len2) return 1;
        return 0;
    }

    private void swap(int i, int j) {
        Freelancer tmp = array[i];
        array[i] = array[j];
        array[j] = tmp;

        array[i].indexInHeap = i;
        array[j].indexInHeap = j;
    }


    public void insert(Freelancer x) {
        if (currentSize == array.length - 1) {
            enlargeArray(array.length * 2 + 1);
        }

        int hole = ++currentSize;
        array[hole] = x;
        x.indexInHeap = hole;
        percolateUp(hole);
    }

    public void updatePriority(Freelancer f) {
        int index = f.indexInHeap;
        if (index <= 0 || index > currentSize) {
            return;
        }

        int finalIndex = percolateUp(index);
        percolateDown(finalIndex);
    }

    public String[] printKMax(int k, Customer customer, HashTable systemBlacklist) {
        String result = "";
        if (k > currentSize) {
            k = currentSize;
        }
        if (k <= 0) {
            return null;
        }

        Freelancer[] tempStorage = new Freelancer[currentSize];

        int count = 0;
        int i = 0;
        Freelancer best = null;
        while(count < k) {
            Freelancer max = deleteMax();
            if (max == null) break;
            tempStorage[i] = max;
            i++;
            // hasJob check no longer needed - hasJob=true freelancers are removed from heap
            // Check blacklist conditions
            if(customer.isBlackListed(max) || systemBlacklist.search(max.id) != null){
                continue;
            }
            if(count == 0){
                best = max;
            }
            count++;
            result += max.id + " - composite: " + max.score + ", price: " + max.price + ", rating: " + String.format("%.1f", max.rating) + "\n";
        }

        for (int j = 0; j < i; j++) {
            insert(tempStorage[j]);
        }
        if(best == null) return null;
        return new String[]{result, best.id};
    }

    public void remove(Freelancer freelancer) {
        int index = freelancer.indexInHeap;
        if (index <= 0 || index > currentSize) {
            return;
        }
        Freelancer removed = array[index];
        if (index == currentSize) {
            array[currentSize] = null;
            currentSize--;
            removed.indexInHeap = -1;
            return;
        }
        swap(index, currentSize);
        array[currentSize] = null;
        currentSize--;
        // Only need to percolate in one direction - check which direction is needed
        if (index > 1 && compareFreelancers(array[index], array[index / 2]) < 0) {
            percolateUp(index);
        } else {
            percolateDown(index);
        }
        removed.indexInHeap = -1;
    }

    public Freelancer findMax() {
        if (isEmpty()) {
            return null;
        }
        return array[1];
    }

    public Freelancer deleteMax() {
        if (isEmpty()) {
            return null;
        }

        Freelancer maxItem = array[1];

        if (currentSize > 1) {
            swap(1, currentSize);
        }
        currentSize--;

        if (currentSize > 0) {
            percolateDown(1);
        }

        maxItem.indexInHeap = -1;
        return maxItem;
    }

    private int percolateUp(int hole) {
        while (hole > 1 && compareFreelancers(array[hole], array[hole / 2]) < 0) {
            swap(hole, hole / 2);
            hole = hole / 2;
        }
        return hole;
    }

    private void percolateDown(int hole) {
        int child;
        for (; hole * 2 <= currentSize; hole = child) {
            child = hole * 2;

            if (child != currentSize &&
                    compareFreelancers(array[child + 1], array[child]) < 0) {
                child++;
            }

            if (compareFreelancers(array[child], array[hole]) < 0) {
                swap(hole, child);
            } else {
                break;
            }
        }
    }

    public boolean isEmpty() {
        return currentSize == 0;
    }

    public void makeEmpty() {
        for(int i = 1; i <= currentSize; i++) {
            if(array[i] != null) {
                array[i].indexInHeap = -1;
            }
            array[i] = null;
        }
        currentSize = 0;
    }

    private void enlargeArray(int newSize) {
        Freelancer[] old = array;
        array = new Freelancer[newSize];
        for (int i = 0; i < old.length; i++) {
            array[i] = old[i];
        }
    }
}