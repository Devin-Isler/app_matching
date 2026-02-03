public class LinkedList {
    LinkedListNode tail;
    LinkedListNode front;
    int size;

    LinkedList(){
        this.tail = null;
        this.front = null;
        this.size = 0;
    }

    public void insert(User user){
        LinkedListNode node = new LinkedListNode(user);
        if(front == null){
            front = node;
            tail = node;
        }
        else {
            tail.next = node;
            tail = node;
        }
        size++;
    }

    public User search(String id){
        LinkedListNode node = front;
        while(node != null){
            if(node.user.id.equals(id)){
                return node.user;
            }
            node = node.next;
        }
        return null;
    }

    public User delete(String id){
        if(front == null){
            return null;
        }
        
        // If deleting the first node
        if(id.equals(front.user.id)){
            User deletedUser = front.user;
            front = front.next;
            if(front == null){
                tail = null;
            }
            size--;
            return deletedUser;
        }
        
        // Search for the node to delete
        LinkedListNode current = front;
        while(current.next != null){
            if(id.equals(current.next.user.id)){
                User deletedUser = current.next.user;
                // If deleting the last node, update tail
                if(current.next == tail){
                    tail = current;
                }
                current.next = current.next.next;
                size--;
                return deletedUser;
            }
            current = current.next;
        }
        
        return null; // User not found
    }

}
