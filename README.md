# GigMatch Pro: High-Performance Gig Economy Backend

## 🚀 Overview
GigMatch Pro is a sophisticated simulation of a modern freelancing platform. While users experience a seamless matching process, the backend manages up to **500,000 users** and real-time reputation updates using custom-built, high-efficiency data structures to ensure the system scales gracefully under real-world pressure.

---

## 🛠 Under the Hood: The Engineering

To achieve maximum performance without relying on high-level built-in libraries, the system implements core computer science primitives to handle massive data loads:

### 1. High-Speed User Management (Custom HashMaps)
The platform manages customers and freelancers using a custom **Hashmap** implementation.
* **Hash Function**: We use **Horner's Method** (with a prime base of 31) to convert string IDs into table indices, ensuring a uniform distribution of users across the table.
* **Collision Handling**: To prevent "traffic jams" in the data, the system uses **Separate Chaining**. Each bucket in the table is a Linked List; if two IDs hash to the same index, they are stored in a chain.
* **Dynamic Scaling**: The system monitors its "Load Factor." Once the table is more than 75% full, it automatically **rehashes**—doubling the table size and re-mapping all users to maintain $O(1)$ average time complexity for lookups.



### 2. The Intelligent Matchmaker (Max-Heaps)
When a customer requests a job, the app must find the "Best" freelancer instantly.
* **Priority Ranking**: Each service (e.g., Web Dev, Plumbing) maintains its own **Max-Heap** of available freelancers.
* **Composite Scoring**: Freelancers are ranked by a score (0–10,000) calculated from **Skill Match (55%)**, **Rating (25%)**, and **Reliability (20%)**.
* **Efficiency**: Using a Heap allows the system to find the top candidate in $O(1)$ time and re-organize the pool in $O(\log n)$ time whenever a freelancer's skills or ratings are updated.



### 3. Dynamic Skill & Burnout Evolution
Freelancers are dynamic entities whose profiles evolve based on real-time performance:
* **Skill Growth**: Successfully completing high-rated jobs (+4 stars) increases specific primary and secondary skills based on the service's requirement profile.
* **Skill Degradation**: Unprofessional behavior, such as freelancer-initiated cancellations, results in an immediate **-3 point penalty** across all five skill dimensions.
* **Burnout System**: If a freelancer completes **5 or more jobs** in a month, they are marked as "Burned Out." This triggers a heavy penalty to their ranking score until they rest (completing 2 or fewer jobs the next month).

### 4. Safety & Blacklisting
* **Personal Blacklists**: Customers can block specific freelancers. The matching algorithm checks the customer's personal blacklist Hashmap before suggesting a match.
* **System Bans**: A dedicated `systemBlackList` Hashmap tracks freelancers who cancel 5+ jobs in a single simulated month, permanently removing them from the marketplace to ensure quality.

---

## 📂 Project Architecture
* **`User.java`**: The base class for all platform participants.
* **`Freelancer.java` / `Customer.java`**: Specialized logic for workplace behavior, skill updates, and employment history.
* **`Hashmap.java`**: The core storage engine using Horner’s method and separate chaining.
* **`Heap.java`**: The ranking engine used to find the best-matched workers efficiently.
* **`Service.java`**: Definitions for the 10 service types and their unique skill requirements.

---

## 🚦 How to Run
The simulation is driven by command-line instructions. Compile and run from the `Project2/src` directory:

```bash
javac *.java
java Main <input_file> <output_file>
