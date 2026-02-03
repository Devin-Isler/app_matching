import java.util.ArrayList;

public class HashTable {

    LinkedList[] table;
    int size;
    int tableSize;

    HashTable(){
        this.tableSize = 10007;
        this.table = new LinkedList[tableSize];
        this.size = 0;
    }

    private int hashFunction(String id){
        int hash = 0;
        int B = 31;
        int len = id.length();
        for(int i = 0; i < len; i++){
            hash = id.charAt(i) + (B * hash);
        }
        return ((hash % tableSize) + tableSize) % tableSize;
    }

    public User insert(User user){
        // Check load factor and expand
        if ((double)size / tableSize > 0.75) {
            rehash();
        }

        int value = hashFunction(user.id);
        if (table[value] == null){
            table[value] = new LinkedList();
        }

        // No duplicate IDs
        if (table[value].search(user.id) != null) {
            return null;
        }

        table[value].insert(user);
        size++;
        return user;
    }

    public User search(String id){
        int value = hashFunction(id);
        if(table[value] == null){
            return null;
        }
        return table[value].search(id);
    }

    public User delete(User user){
        // Use user.id directly - already computed, no need to extract again
        int value = hashFunction(user.id);
        if(table[value] == null){
            return null;
        }

        User deletedUser = table[value].delete(user.id);
        if(deletedUser != null){
            size--;
            if(table[value].size == 0){
                table[value] = null;
            }
        }
        return deletedUser;
    }

    private void rehash(){
        // Save old values
        LinkedList[] oldTable = table;
        int oldTableSize = tableSize;

        tableSize = oldTableSize * 2 - 1;

        table = new LinkedList[tableSize];
        size = 0;

        for(int i = 0; i < oldTableSize; i++){
            if(oldTable[i] != null){
                LinkedListNode current = oldTable[i].front;
                while(current != null){
                    insert(current.user);
                    current = current.next;
                }
            }
        }
    }

    // Add this method to your HashTable.java file
    public ArrayList<User> getAllValues() {
        ArrayList<User> allUsers = new ArrayList<>(size);
        for (int i = 0; i < tableSize; i++) {
            if (table[i] != null) {
                // This assumes your LinkedList has a 'front' node
                // and 'next' pointer, just like in your rehash() method.
                LinkedListNode current = table[i].front;
                while (current != null) {
                    allUsers.add(current.user);
                    current = current.next;
                }
            }
        }
        return allUsers;
    }
}
