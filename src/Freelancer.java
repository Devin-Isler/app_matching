public class Freelancer extends User {
    int jobsTaken;
    int totalJobsCancelled;
    int cancelCountPerMonth;
    int jobsTakenCountPerMonth;
    int price;
    int nextPrice;
    int balance;
    int score;
    double rating;
    Customer employer;
    Service service;
    Service nextService;
    Integer[] skills;
    Services services;
    boolean hasJob;
    boolean isBurnedOut;
    int indexInHeap;
    boolean isChange;

    Freelancer(String id, String service, int price, int T, int C, int R, int E, int A, Services services){
        this.id = id;
        this.services = services;
        this.service = services.find(service);
        this.nextService = null;
        this.balance = 0;
        this.price = price;
        this.nextPrice = 0;
        this.skills = new Integer[]{T, C, R, E, A};
        this.jobsTaken = 0;
        this.jobsTakenCountPerMonth = 0;
        this.totalJobsCancelled = 0;
        this.hasJob = false;
        this.employer = null;
        this.rating = 5.0;
        this.cancelCountPerMonth = 0;
        this.isBurnedOut = false;
        this.indexInHeap = -1;
        this.isChange = false;
        calculateScore();
    }

    public void employedBy(Customer employer){
        this.employer = employer;
        employer.totalEmployment ++;
        hasJob = true;
        // Remove from heap when employed (not available for other jobs)
        if(service != null){
            service.removeFreelancer(this);
        }
    }

    public void completed(int rating){
        jobsTaken ++;
        jobsTakenCountPerMonth++;
        balance += price;
        hasJob = false;
        employer.completed(price);
        employer = null;
        this.rating = arrangeRating(rating);
        skillUpdate(service, rating);
        this.calculateScore();
        isChange = true;
    }

    public void cancelledByFreelancer(){
        cancelCountPerMonth ++;
        totalJobsCancelled++;
        hasJob = false;
        employer = null;
        rating = arrangeRating(0);
        skillUpdate(service, -1);
        this.calculateScore();
    }

    public void cancelledByCustomer(){
        employer.applyPenalty(250);
        employer = null;
        hasJob = false;
    }

    private double arrangeRating(int rating){
        int n = jobsTaken + totalJobsCancelled;
        return (this.rating * n + rating) / (n + 1);
    }

    private void skillUpdate(Service service, int rating){
        if(rating >= 4){
            skills[service.primary] = Math.min(100, skills[service.primary] + 2);
            skills[service.secondary1] = Math.min(100, skills[service.secondary1] + 1);
            skills[service.secondary2] = Math.min(100, skills[service.secondary2] + 1);
        }
        if(rating == -1){
            for (int i = 0; i < 5; i++){
                skills[i] = Math.max(skills[i] - 3, 0);
            }
        }
    }

    public String toString() {
        return String.format("%s: %s, price: %d, rating: %.1f, completed: %d, cancelled: %d, skills: (%d,%d,%d,%d,%d), available: %s, burnout: %s",
         id, service.name, price, rating, jobsTaken, totalJobsCancelled, skills[0], skills[1], skills[2], skills[3], skills[4], hasJob ? "no" : "yes", isBurnedOut ? "yes" : "no");
    }

    private void calculateScore(){
        double ws = 0.55;
        double wr = 0.25;
        double wl = 0.20;
        double x = ws * skillScore() + wr * ratingScore() + wl * reliabilityScore();
        if (isBurnedOut){
            x = x - 0.45;
        }
        this.score = (int) Math.floor(10000 * x);
    }

    private double skillScore(){
        int dotProduct = 0;
        for (int i = 0; i < 5; i++){
            dotProduct += service.skill[i] * skills[i];
        }
        return (double) dotProduct / (100 * service.sum);
    }

    private double ratingScore(){
        return rating / 5.0;
    }

    private double reliabilityScore(){
        if(jobsTaken + totalJobsCancelled == 0){
            return 1.0;
        }
        return 1.0 - (double) totalJobsCancelled / (jobsTaken + totalJobsCancelled);
    }

    public void change(Service newService, int newPrice){
        this.nextService = newService;
        this.nextPrice = newPrice;
    }

    public void monthlyUpdate() {
        cancelCountPerMonth = 0;
        
        if(jobsTakenCountPerMonth >= 5){
            isBurnedOut = true;
        }
        else if(isBurnedOut && jobsTakenCountPerMonth <= 2){
            isBurnedOut = false;
        }

        jobsTakenCountPerMonth = 0;

        if(nextService != null){
            Service oldService = this.service;
            Service newService = this.nextService;
            int updatedPrice = this.nextPrice;

            if (oldService != null){
                oldService.removeFreelancer(this);
            }

            this.service = newService;
            this.price = updatedPrice;
            this.nextService = null;
            this.nextPrice = 0;

            if (this.service != null){
                this.service.addFreelancer(this);
            }
        }
        calculateScore();
    }

    public void manuelUpdate(int T, int C, int R, int E, int A){
        this.skills = new Integer[]{T, C, R, E, A};
        calculateScore();
    }
}
