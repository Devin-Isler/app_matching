public class Customer extends User {
    int totalSpending;
    int totalEmployment;
    int penalty;
    int discount;
    HashTable blackListed;
    String tier;
    String tierNextMonth;
    boolean isGoingToChange;

    Customer(String id){
        this.id = id;
        this.totalSpending = 0;
        this.blackListed = new HashTable();
        this.tier = "bronze";
        this.tierNextMonth = "";
        this.totalEmployment = 0;
        this.penalty = 0;
        this.discount = 0;
        this.isGoingToChange = false;
    }

    public void blackList(Freelancer freelancer){
        blackListed.insert(freelancer);
    }

    public boolean isBlackListed(Freelancer freelancer){
        // Fast path: if blacklist is empty, no need to search
        if(blackListed.size == 0){
            return false;
        }
        return blackListed.search(freelancer.id) != null;
    }

    public void unBlackList(Freelancer freelancer){
        blackListed.delete(freelancer);
    }

    public void completed(int price){
        totalSpending += price * (100 - discount) / 100;
        isGoingToChange = isTierChange();
    }

    public String toString(){
        return String.format("%s: total spent: $%d, loyalty tier: %s, blacklisted freelancer count: %d, total employment count: %d",
                id, totalSpending, tier.toUpperCase(), blackListed.size, totalEmployment);
    }

    private int getDiscount() {
        switch (tier) {
            case "bronze" -> {
                return 0;
            }
            case "silver" -> {
                return 5;
            }
            case "gold" -> {
                return 10;
            }
            case "platinum" -> {
                return 15;
            }
        }
        return 0;
    }

    // Replace your old method with this one
    private boolean isTierChange(){
        int points = Math.max(0, totalSpending - penalty);
        String newTier;

        // 1. Determine the correct tier based *only* on points
        if (points < 500){
            newTier = "bronze";
        }
        else if (points < 2000){
            newTier = "silver";
        }
        else if (points < 5000){
            newTier = "gold";
        }
        else {
            newTier = "platinum";
        }

        // 2. Check if the new tier is different from the current one
        if (!tier.equals(newTier)){
            // 3. If so, queue the change and return true
            this.tierNextMonth = newTier;
            return true;
        }

        // If there was a previously scheduled change, cancel it
        if (!tierNextMonth.isEmpty()){
            tierNextMonth = "";
        }

        // 4. Otherwise, there is no change
        return false;
    }

    public void applyPenalty(int penalty){
        this.penalty += penalty;
        this.isGoingToChange = isTierChange();
    }

    public void monthlyUpdate() {
        if (!tierNextMonth.isEmpty()){
            this.tier = tierNextMonth;
            this.discount = getDiscount();
            tierNextMonth = "";
        }
    }

    public boolean hasPendingTierChange() {
        return !tierNextMonth.isEmpty();
    }
}
