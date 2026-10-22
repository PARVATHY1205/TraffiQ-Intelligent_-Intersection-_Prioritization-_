import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * TraffiQ: Intelligent Intersection Prioritization & Emergency Queue Management System
 * Author: PARVATHY RAJEEV (Roll No: 34)
 */

// ============================================================================
// 1. VEHICLE DOMAIN MODEL
// ============================================================================
class Vehicle implements Comparable<Vehicle> {
    public enum Type {
        EMERGENCY_AMBULANCE(1, "Ambulance"),
        EMERGENCY_FIRE(2, "Fire Engine"),
        EMERGENCY_POLICE(3, "Police Vehicle"),
        REGULAR_BUS(4, "Public Bus"),
        REGULAR_CAR(5, "Car"),
        REGULAR_BIKE(6, "Motorcycle");

        private final int priorityRank; // Lower numerical value = Higher priority
        private final String displayName;

        Type(int priorityRank, String displayName) {
            this.priorityRank = priorityRank;
            this.displayName = displayName;
        }

        public int getPriorityRank() { return priorityRank; }
        public String getDisplayName() { return displayName; }
        public boolean isEmergency() { return priorityRank <= 3; }
    }

    private final String licensePlate;
    private final Type type;
    private final String lane; // "NORTH", "EAST", "SOUTH", "WEST"
    private final long arrivalTimestamp;

    public Vehicle(String licensePlate, Type type, String lane) {
        if (licensePlate == null || licensePlate.trim().isEmpty()) {
            throw new IllegalArgumentException("License plate cannot be null or empty.");
        }
        if (lane == null || lane.trim().isEmpty()) {
            throw new IllegalArgumentException("Lane assignment cannot be null or empty.");
        }
        this.licensePlate = licensePlate.trim().toUpperCase();
        this.type = type;
        this.lane = lane.trim().toUpperCase();
        this.arrivalTimestamp = System.currentTimeMillis();
    }

    public String getLicensePlate() { return licensePlate; }
    public Type getType() { return type; }
    public String getLane() { return lane; }
    public long getArrivalTimestamp() { return arrivalTimestamp; }

    /**
     * Priority Comparison based on Priority Rank and FIFO ordering for ties.
     * Time Complexity: O(1)
     */
    @Override
    public int compareTo(Vehicle other) {
        if (this.type.getPriorityRank() != other.type.getPriorityRank()) {
            return Integer.compare(this.type.getPriorityRank(), other.type.getPriorityRank());
        }
        return Long.compare(this.arrivalTimestamp, other.arrivalTimestamp);
    }

    @Override
    public String toString() {
        return String.format("[%s | %s | Lane: %s | Priority: %d]", 
            licensePlate, type.getDisplayName(), lane, type.getPriorityRank());
    }
}

// ============================================================================
// 2. CUSTOM CIRCULAR QUEUE IMPLEMENTATION
// ============================================================================
class CircularQueue<T> {
    private final Object[] queue;
    private final int capacity;
    private int front;
    private int rear;
    private int size;

    public CircularQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Queue capacity must be greater than zero.");
        }
        this.capacity = capacity;
        this.queue = new Object[capacity];
        this.front = -1;
        this.rear = -1;
        this.size = 0;
    }

    public synchronized boolean isFull() { return size == capacity; }
    public synchronized boolean isEmpty() { return size == 0; }
    public synchronized int size() { return size; }
    public synchronized int getCapacity() { return capacity; }

    /**
     * Inserts an element into the circular queue.
     * Time Complexity: O(1)
     */
    public synchronized boolean enqueue(T item) {
        if (isFull()) return false; // Queue Overflow
        if (isEmpty()) front = 0;
        
        rear = (rear + 1) % capacity;
        queue[rear] = item;
        size++;
        return true;
    }

    /**
     * Removes and returns the front element from the circular queue.
     * Time Complexity: O(1)
     */
    @SuppressWarnings("unchecked")
    public synchronized T dequeue() {
        if (isEmpty()) return null; // Queue Underflow
        
        T item = (T) queue[front];
        queue[front] = null; // Prevent memory leaks

        if (front == rear) { // Reset queue when empty
            front = -1;
            rear = -1;
        } else {
            front = (front + 1) % capacity;
        }
        size--;
        return item;
    }

    /**
     * Retrieves the front element without removing it.
     * Time Complexity: O(1)
     */
    @SuppressWarnings("unchecked")
    public synchronized T peek() {
        if (isEmpty()) return null;
        return (T) queue[front];
    }

    /**
     * Converts queue contents to a List via linear buffer iteration.
     * Time Complexity: O(N)
     */
    @SuppressWarnings("unchecked")
    public synchronized List<T> toList() {
        List<T> list = new ArrayList<>();
        if (isEmpty()) return list;

        int count = 0;
        int idx = front;
        while (count < size) {
            list.add((T) queue[idx]);
            idx = (idx + 1) % capacity;
            count++;
        }
        return list;
    }
}

// ============================================================================
// 3. CUSTOM MIN-BINARY HEAP PRIORITY QUEUE IMPLEMENTATION
// ============================================================================
class CustomPriorityQueue<T extends Comparable<T>> {
    private final ArrayList<T> heap;

    public CustomPriorityQueue() {
        this.heap = new ArrayList<>();
    }

    public synchronized int size() { return heap.size(); }
    public synchronized boolean isEmpty() { return heap.isEmpty(); }

    /**
     * Enqueues an element and restores min-heap order via Sift-Up.
     * Time Complexity: O(log N)
     */
    public synchronized void enqueue(T item) {
        if (item == null) throw new IllegalArgumentException("Cannot enqueue null item.");
        heap.add(item);
        siftUp(heap.size() - 1);
    }

    /**
     * Extracts top priority element and restores heap order via Sift-Down.
     * Time Complexity: O(log N)
     */
    public synchronized T dequeue() {
        if (isEmpty()) return null;
        T root = heap.get(0);
        T lastItem = heap.remove(heap.size() - 1);
        if (!isEmpty()) {
            heap.set(0, lastItem);
            siftDown(0);
        }
        return root;
    }

    public synchronized T peek() {
        if (isEmpty()) return null;
        return heap.get(0);
    }

    private void siftUp(int index) {
        while (index > 0) {
            int parentIndex = (index - 1) / 2;
            if (heap.get(index).compareTo(heap.get(parentIndex)) < 0) {
                swap(index, parentIndex);
                index = parentIndex;
            } else {
                break;
            }
        }
    }

    private void siftDown(int index) {
        int size = heap.size();
        while (index < size) {
            int leftChild = 2 * index + 1;
            int rightChild = 2 * index + 2;
            int smallest = index;

            if (leftChild < size && heap.get(leftChild).compareTo(heap.get(smallest)) < 0) {
                smallest = leftChild;
            }
            if (rightChild < size && heap.get(rightChild).compareTo(heap.get(smallest)) < 0) {
                smallest = rightChild;
            }

            if (smallest != index) {
                swap(index, smallest);
                index = smallest;
            } else {
                break;
            }
        }
    }

    private void swap(int i, int j) {
        T temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }

    public synchronized List<T> toList() {
        return new ArrayList<>(heap);
    }
}

// ============================================================================
// 4. INTERSECTION CONTROLLER ENGINE
// ============================================================================
class IntersectionController {
    public static final String[] LANES = {"NORTH", "EAST", "SOUTH", "WEST"};
    private static final int LANE_CAPACITY = 8;

    private final Map<String, CircularQueue<Vehicle>> laneQueues;
    private final CustomPriorityQueue<Vehicle> emergencyPriorityQueue;
    private final List<Vehicle> processedVehiclesHistory;

    private int activeLaneIndex = 0; // Rotates: NORTH -> EAST -> SOUTH -> WEST
    private int totalVehiclesRegistered = 0;
    private int totalEmergencyCleared = 0;
    private int totalRegularCleared = 0;

    public IntersectionController() {
        this.laneQueues = new HashMap<>();
        for (String lane : LANES) {
            this.laneQueues.put(lane, new CircularQueue<>(LANE_CAPACITY));
        }
        this.emergencyPriorityQueue = new CustomPriorityQueue<>();
        this.processedVehiclesHistory = new ArrayList<>();
    }

    /**
     * Registers a new vehicle into the system.
     * Time Complexity: O(log E) for emergency, O(1) for regular.
     */
    public synchronized String registerVehicle(String plate, Vehicle.Type type, String lane) {
        lane = lane.toUpperCase();
        if (!laneQueues.containsKey(lane)) {
            return "Error: Invalid lane destination specified.";
        }

        Vehicle vehicle = new Vehicle(plate, type, lane);

        // Linear check for duplicates
        if (searchVehicleByPlate(plate) != null) {
            return "Error: Vehicle with license plate " + plate + " is already queued.";
        }

        totalVehiclesRegistered++;
        if (type.isEmergency()) {
            emergencyPriorityQueue.enqueue(vehicle);
            return "EMERGENCY CLEARED PREEMPTION: " + type.getDisplayName() + " [" + plate + "] added to Priority Queue for Lane " + lane;
        } else {
            boolean success = laneQueues.get(lane).enqueue(vehicle);
            if (!success) {
                return "OVERFLOW WARNING: Lane " + lane + " circular queue is full! Vehicle [" + plate + "] rejected.";
            }
            return "SUCCESS: Regular Vehicle [" + plate + "] enqueued into " + lane + " Lane Circular Queue.";
        }
    }

    /**
     * Advances the traffic signal cycle. Preempts regular rotation if an emergency vehicle is present.
     * Time Complexity: O(log E) if emergency cleared, O(1) if regular vehicle cleared.
     */
    public synchronized String advanceSignal() {
        if (!emergencyPriorityQueue.isEmpty()) {
            Vehicle emergencyVehicle = emergencyPriorityQueue.dequeue();
            totalEmergencyCleared++;
            processedVehiclesHistory.add(emergencyVehicle);
            return "EMERGENCY OVERRIDE: Green signal granted immediately to " + emergencyVehicle.getType().getDisplayName() 
                + " [" + emergencyVehicle.getLicensePlate() + "] on " + emergencyVehicle.getLane() + " Lane!";
        }

        String currentActiveLane = LANES[activeLaneIndex];
        CircularQueue<Vehicle> currentQueue = laneQueues.get(currentActiveLane);

        String resultMsg;
        if (!currentQueue.isEmpty()) {
            Vehicle vehicle = currentQueue.dequeue();
            totalRegularCleared++;
            processedVehiclesHistory.add(vehicle);
            resultMsg = "SIGNAL CLEARANCE: Green Light on " + currentActiveLane + " Lane. Dispatched: " + vehicle.toString();
        } else {
            resultMsg = "SIGNAL PASSED: Green Light on " + currentActiveLane + " Lane, but queue was empty.";
        }

        activeLaneIndex = (activeLaneIndex + 1) % LANES.length;
        return resultMsg;
    }

    /**
     * Performs a Linear Search across all active queues.
     * Time Complexity: O(N)
     */
    public synchronized Vehicle searchVehicleByPlate(String plate) {
        if (plate == null) return null;
        String searchPlate = plate.trim().toUpperCase();

        for (Vehicle v : emergencyPriorityQueue.toList()) {
            if (v.getLicensePlate().equals(searchPlate)) return v;
        }

        for (CircularQueue<Vehicle> queue : laneQueues.values()) {
            for (Vehicle v : queue.toList()) {
                if (v.getLicensePlate().equals(searchPlate)) return v;
            }
        }
        return null;
    }

    public synchronized void printStatusSnapshot() {
        System.out.println("\n================ INTERSECTION SNAPSHOT ================");
        System.out.println("Active Signal Lane: " + LANES[activeLaneIndex]);
        System.out.println("Total Registered: " + totalVehiclesRegistered + " | Emergency Cleared: " 
            + totalEmergencyCleared + " | Regular Cleared: " + totalRegularCleared);
        System.out.println("-------------------------------------------------------");
        System.out.println("EMERGENCY PRIORITY QUEUE (Min-Heap): " + emergencyPriorityQueue.toList());
        for (String lane : LANES) {
            System.out.println(lane + " CIRCULAR QUEUE: " + laneQueues.get(lane).toList());
        }
        System.out.println("=======================================================\n");
    }
}

// ============================================================================
// 5. MAIN CLI DRIVER
// ============================================================================
public class Main {
    public static void main(String[] args) {
        IntersectionController controller = new IntersectionController();
        Scanner scanner = new Scanner(System.in);

        System.out.println("======================================================================");
        System.out.println("  TraffiQ: Intelligent Intersection Prioritization & Queue System     ");
        System.out.println("  Author: PARVATHY RAJEEV (Roll No: 34)                               ");
        System.out.println("======================================================================");

        // Demo Data
        controller.registerVehicle("KL-01-A-1001", Vehicle.Type.REGULAR_CAR, "NORTH");
        controller.registerVehicle("KL-01-B-2002", Vehicle.Type.REGULAR_BUS, "NORTH");
        controller.registerVehicle("KL-07-EMG-999", Vehicle.Type.EMERGENCY_AMBULANCE, "SOUTH");
        controller.registerVehicle("KL-01-C-3003", Vehicle.Type.REGULAR_CAR, "EAST");

        boolean running = true;
        while (running) {
            System.out.println("\n--- COMMAND MENU ---");
            System.out.println("1. Register Vehicle");
            System.out.println("2. Advance Traffic Signal Cycle");
            System.out.println("3. Search Vehicle by License Plate");
            System.out.println("4. Display Intersection Status Snapshot");
            System.out.println("5. Exit");
            System.out.print("Select Option (1-5): ");

            try {
                int choice = Integer.parseInt(scanner.nextLine());
                switch (choice) {
                    case 1:
                        System.out.print("Enter License Plate: ");
                        String plate = scanner.nextLine();
                        System.out.println("Types: 1-Ambulance, 2-Fire Engine, 3-Police, 4-Bus, 5-Car, 6-Bike");
                        System.out.print("Select Type (1-6): ");
                        int typeIdx = Integer.parseInt(scanner.nextLine());
                        Vehicle.Type type = Vehicle.Type.values()[typeIdx - 1];
                        System.out.print("Select Lane (NORTH, EAST, SOUTH, WEST): ");
                        String lane = scanner.nextLine();
                        System.out.println("\n>>> " + controller.registerVehicle(plate, type, lane));
                        break;
                    case 2:
                        System.out.println("\n>>> " + controller.advanceSignal());
                        break;
                    case 3:
                        System.out.print("Enter License Plate to Search: ");
                        String searchPlate = scanner.nextLine();
                        Vehicle v = controller.searchVehicleByPlate(searchPlate);
                        if (v != null) {
                            System.out.println("\n>>> FOUND: " + v);
                        } else {
                            System.out.println("\n>>> NOT FOUND: No active vehicle with plate " + searchPlate);
                        }
                        break;
                    case 4:
                        controller.printStatusSnapshot();
                        break;
                    case 5:
                        running = false;
                        System.out.println("Exiting TraffiQ System. Program terminated.");
                        break;
                    default:
                        System.out.println("Invalid option.");
                }
            } catch (Exception e) {
                System.out.println("Error processing command: " + e.getMessage());
            }
        }
        scanner.close();
    }
}