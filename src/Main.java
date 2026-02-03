import java.io.*;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Main entry point for GigMatch Pro platform.
 */
public class Main {

    static HashTable customers = new HashTable();
    static HashTable freelancers = new HashTable();
    static HashTable systemBlackList = new HashTable();
    static Services services = new Services();
    static HashTable goingToBeUpdatedFreelancers = new HashTable();
    static HashTable goingToBeUpdatedCustomers = new HashTable();

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        if (args.length != 2) {
            System.err.println("Usage: java Main <input_file> <output_file>");
            System.exit(1);
        }

        String inputFile = args[0];
        String outputFile = args[1];

        // Create shared data structures - these should persist across all commands

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                processCommand(line, writer);
            }

        } catch (IOException e) {
            System.err.println("Error reading/writing files: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void processCommand(String command, BufferedWriter writer)
            throws IOException {

        String[] parts = command.split("\\s+");
        String operation = parts[0];

        try {
            String result = "";

            switch (operation) {
                case "register_customer":
                    // Format: register_customer customerID
                    String id1 = parts[1];
                    Customer cust1 = new Customer(id1);
                    if (customers.insert(cust1) == null) {
                        result = "Some error occurred in register customer.";
                    } else {
                        result = "registered customer " + id1;
                    }
                    break;

                case "register_freelancer":
                    // Format: register_freelancer freelancerID serviceName basePrice T C R E A
                    String id2 = parts[1];
                    String service1 = parts[2];
                    int price = Integer.parseInt(parts[3]);
                    int T = Integer.parseInt(parts[4]);
                    int C = Integer.parseInt(parts[5]);
                    int R = Integer.parseInt(parts[6]);
                    int E = Integer.parseInt(parts[7]);
                    int A = Integer.parseInt(parts[8]);
                    Freelancer free1 = new Freelancer(id2, service1, price, T, C, R, E, A, services);
                    if (freelancers.insert(free1) == null || free1.service == null
                            || !isValid(T) || !isValid(C) || !isValid(R) || !isValid(E) || !isValid(A) || price < 0) {
                        result = "Some error occurred in register freelancer.";
                    }
                    else{
                        result = "registered freelancer " + id2;
                        free1.service.addFreelancer(free1);
                    }
                    break;

                case "request_job":
                    // Format: request_job customerID serviceName topK
                    String id3 = parts[1];
                    String service2 = parts[2];
                    int k = Integer.parseInt(parts[3]);

                    Customer cust5 = (Customer) customers.search(id3);
                    Service requestedService = services.find(service2);
                    if(cust5 == null || requestedService == null){
                        result = "Some error occurred in request job.";
                    }
                    else{
                        String[] outcome = requestedService.getTopFreelancers(k, cust5, systemBlackList);
                        if(outcome == null){
                            result = "no freelancers available";
                        }
                        else{
                            Freelancer free5 = (Freelancer) freelancers.search(outcome[1]);
                            free5.employedBy(cust5);
                            result = "available freelancers for " + service2 + " (top " + k + "):\n";
                            result += outcome[0];
                            result += "auto-employed best freelancer: " + free5.id + " for customer " + id3;
                        }
                    }
                    break;

                case "employ_freelancer":
                    // Format: employ_freelancer customerID freelancerID
                    String custId = parts[1];
                    String freeId = parts[2];
                    Customer cust3 = (Customer) customers.search(custId);
                    Freelancer free3 = (Freelancer) freelancers.search(freeId);

                    if(cust3 == null || free3 == null || free3.hasJob || cust3.isBlackListed(free3)){
                        result = "Some error occurred in employ.";
                    }
                    else{
                        free3.employedBy(cust3);
                        result = custId + " employed " + freeId + " for " + free3.service.name;
                    }
                    break;

                case "complete_and_rate":
                    // Format: complete_and_rate freelancerID rating
                    String id4 = parts[1];
                    int rating = Integer.parseInt(parts[2]);
                    Freelancer free4 = (Freelancer) freelancers.search(id4);

                    if(free4 == null || !free4.hasJob || free4.employer == null || rating > 5 || rating < 0){
                        result = "Some error occurred in complete and rate.";
                    }
                    else{
                        Customer cust4 = free4.employer;
                        result = id4 + " completed job for " + cust4.id + " with rating " + rating;
                        free4.completed(rating);
                        // Add back to heap (was removed when employed)
                        if(free4.service != null){
                            free4.service.addFreelancer(free4);
                        }
                        handleCustomerTierQueue(cust4);
                        goingToBeUpdatedFreelancers.insert(free4);
                    }
                    break;

                case "cancel_by_freelancer":
                    // Format: cancel_by_freelancer freelancerID
                    String id5 = parts[1];
                    Freelancer free5 = (Freelancer) freelancers.search(id5);
                    if(free5 == null || !free5.hasJob){
                        result = "Some error occurred in cancel by freelancer.";
                    }
                    else{
                        result = "cancelled by freelancer: " + free5.id + " cancelled " + free5.employer.id;
                        free5.cancelledByFreelancer();
                        // Add back to heap (was removed when employed)
                        if(free5.service != null){
                            free5.service.addFreelancer(free5);
                        }
                        if(free5.cancelCountPerMonth >= 5){
                            free5.service.removeFreelancer(free5);
                            freelancers.delete(free5);
                            systemBlackList.insert(free5);
                            result += "\nplatform banned freelancer: " + free5.id;
                        }
                        else{
                            goingToBeUpdatedFreelancers.insert(free5);
                        }
                    }
                    break;

                case "cancel_by_customer":
                    // Format: cancel_by_customer customerID freelancerID
                    String custId0 = parts[1];
                    String freeId0 = parts[2];
                    Customer cust0 = (Customer) customers.search(custId0);
                    Freelancer free0 = (Freelancer) freelancers.search(freeId0);
                    if(cust0 == null || free0 == null || !free0.employer.id.equals(custId0)){
                        result = "Some error occurred in cancel by customer.";
                    }
                    else{
                        result = "cancelled by customer: " + free0.employer.id + " cancelled " + free0.id;
                        free0.cancelledByCustomer();
                        // Add back to heap (was removed when employed)
                        if(free0.service != null){
                            free0.service.addFreelancer(free0);
                        }
                        handleCustomerTierQueue(cust0);
                    }
                    break;

                case "blacklist":
                    // Format: blacklist customerID freelancerID
                    String custId1 = parts[1];
                    String freeId1 = parts[2];
                    Customer cust2 = (Customer) customers.search(custId1);
                    Freelancer free2 = (Freelancer) freelancers.search(freeId1);
                    if(cust2 == null || free2 == null || cust2.isBlackListed(free2)){
                        result = "Some error occurred in blacklist.";
                    }
                    else{
                        cust2.blackList(free2);
                        result = custId1 + " blacklisted " + freeId1;
                    }
                    break;

                case "unblacklist":
                    // Format: unblacklist customerID freelancerID
                    String custId3 = parts[1];
                    String freeId3 = parts[2];
                    Customer cust7 = (Customer) customers.search(custId3);
                    Freelancer free7 = (Freelancer) freelancers.search(freeId3);
                    if(cust7 == null || free7 == null || !cust7.isBlackListed(free7)){
                        result = "Some error occurred in unblacklist.";
                    }
                    else{
                        cust7.unBlackList(free7);
                        result = custId3 + " unblacklisted " + freeId3;
                    }
                    break;

                case "change_service":
                    // Format: change_service freelancerID newService newPrice
                    String id8 = parts[1];
                    String service3 = parts[2];
                    int price1 = Integer.parseInt(parts[3]);
                    Freelancer free8 = (Freelancer) freelancers.search(id8);
                    Service newService = services.find(service3);
                    if(free8 == null || newService == null){
                        result = "Some error occurred in change service.";
                    }
                    else{
                        free8.change(newService, price1);
                        goingToBeUpdatedFreelancers.insert(free8);
                        result = "service change for " + id8 + " queued from " + free8.service.name + " to " + service3;
                    }
                    break;

                case "simulate_month":
                    // Format: simulate_month
                    // Process freelancers: update and conditionally remove
                    ArrayList<User> freeList = goingToBeUpdatedFreelancers.getAllValues();
                    for(User free: freeList){
                        Freelancer freelancer = (Freelancer) free;
                        // Remove from heap before score changes
                        if(freelancer.service != null){
                            freelancer.service.removeFreelancer(freelancer);
                        }
                        // Update score
                        freelancer.monthlyUpdate();
                        // Re-insert with new score
                        if(freelancer.service != null){
                            freelancer.service.addFreelancer(freelancer);
                        }
                        if(!freelancer.isBurnedOut){
                            goingToBeUpdatedFreelancers.delete(freelancer);
                        }
                    }
                    // Process customers: all are removed after update
                    ArrayList<User> custList = goingToBeUpdatedCustomers.getAllValues();
                    for(User customer: custList){
                        customer.monthlyUpdate();
                    }
                    // Clear all customers at once instead of deleting one by one
                    goingToBeUpdatedCustomers = new HashTable();
                    result = "month complete";
                    break;

                case "query_freelancer":
                    // Format: query_freelancer freelancerID
                    String id6 = parts[1];
                    Freelancer free6 = (Freelancer) freelancers.search(id6);
                    if(free6 == null){
                        result = "Some error occurred in query freelancer.";
                    }
                    else{
                        result = free6.toString();
                    }

                    break;

                case "query_customer":
                    // Format: query_customer customerID
                    String id7 = parts[1];
                    Customer cust6 = (Customer) customers.search(id7);
                    if(cust6 == null){
                        result = "Some error occurred in query customer.";
                    }
                    else{
                        result = cust6.toString();
                    }
                    break;

                case "update_skill":
                    // Format: update_skill freelancerID T C R E A
                    String id9 = parts[1];
                    int T1 = Integer.parseInt(parts[2]);
                    int C1 = Integer.parseInt(parts[3]);
                    int R1 = Integer.parseInt(parts[4]);
                    int E1 = Integer.parseInt(parts[5]);
                    int A1 = Integer.parseInt(parts[6]);
                    Freelancer free9 = (Freelancer) freelancers.search(id9);
                    if(free9 == null || !isValid(T1) || !isValid(C1) || !isValid(R1) || !isValid(E1) || !isValid(A1)){
                        result = "Some error occurred in update skill.";
                    }
                    else{
                        // Remove from heap before score changes
                        free9.service.removeFreelancer(free9);
                        // Update score
                        free9.manuelUpdate(T1, C1, R1, E1, A1);
                        // Re-insert with new score
                        free9.service.addFreelancer(free9);
                        result = "updated skills of " + free9.id + " for " + free9.service.name;
                    }
                    break;

                default:
                    result = "Unknown command: " + operation;
            }

            writer.write(result);
            writer.newLine();

        } catch (Exception e) {
            writer.write("Error processing command: " + command);
            writer.newLine();
        }
    }

    private static void handleCustomerTierQueue(Customer customer){
        if(customer == null){
            return;
        }
        if(customer.isGoingToChange){
            goingToBeUpdatedCustomers.insert(customer);
            customer.isGoingToChange = false;
        }
        else if(!customer.hasPendingTierChange()){
            goingToBeUpdatedCustomers.delete(customer);
        }
    }

    private static boolean isValid(int value){
        return value <= 100 && value >= 0;
    }
}
