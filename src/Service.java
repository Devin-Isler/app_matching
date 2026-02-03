public class Service {
    int[] skill;
    String name;
    int primary = -1;
    int secondary1 = -1;
    int secondary2 = -1;
    int sum;
    Heap heap;

    Service(int T, int C, int R, int E, int A, String name) {
        this.name = name;
        this.skill = new int[]{T, C, R, E, A};
        this.primary = findMax();
        this.secondary1 = findMax();
        this.secondary2 = findMax();
        this.sum = T + C + R + E + A;
        this.heap = new Heap();
    }

    private int findMax() {
        int maxValue = -1;
        int maxIndex = -1;
        for (int i = 0; i < 5; i++) {
            if (primary == i) {
                continue;
            }
            if (secondary1 == i) {
                continue;
            }
            if (maxValue < skill[i]) {
                maxValue = skill[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    public void addFreelancer(Freelancer freelancer) {
        if (freelancer == null) {
            return;
        }
        heap.insert(freelancer);
    }

    public void removeFreelancer(Freelancer freelancer) {
        if (freelancer == null) {
            return;
        }
        heap.remove(freelancer);
    }

    public void updateFreelancer(Freelancer freelancer) {
        if (freelancer == null) {
            return;
        }
        heap.updatePriority(freelancer);
    }

    public String[] getTopFreelancers(int k, Customer customer, HashTable systemBlacklist) {
        return heap.printKMax(k, customer, systemBlacklist);
    }
}
