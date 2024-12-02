package com.example;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.*;

public class InMemoryPriorityQueueService implements QueueService {
    private final Map<String, Queue<Message>> queues; // Map to store multiple queues by their URLs
    private long visibilityTimeout; // Timeout for visibility of messages

    /* 
     * Constructor initializes the queues map and loads configuration properties.
     * Sets the visibility timeout from the config file or defaults to 30 seconds.
     */
    public InMemoryPriorityQueueService() {
        this.queues = new ConcurrentHashMap<>(); // Thread-safe map to store queues
        String propFileName = "config.properties"; // Configuration file name
        Properties confInfo = new Properties();

        try (InputStream inStream = getClass().getClassLoader().getResourceAsStream(propFileName)) {
            if (inStream != null) {
                confInfo.load(inStream); // Load properties from file
                this.visibilityTimeout = Long.parseLong(confInfo.getProperty("visibilityTimeout", "30"));
            } else {
                System.err.println("Config file not found, using default visibility timeout.");
                this.visibilityTimeout = 30;  // Default timeout if file not found
            }
        } catch (IOException e) {
            e.printStackTrace();
            this.visibilityTimeout = 30;  // Default timeout in case of error
        }
    }

    public long getVisibilityTimeout() {
        return visibilityTimeout; // Return visibility timeout
    }

    /* 
     * Pushes a message with default priority (0) if no priority is specified.
     */
    @Override
    public void push(String queueUrl, String messageBody) {
        push(queueUrl, messageBody, 0); // Default priority 0
    }

    /* 
     * Pushes a message with specified priority into the queue.
     * If the queue doesn't exist, it's created with the appropriate comparator.
     */
    public void push(String queueUrl, String messageBody, int priority) {
        Queue<Message> queue = queues.computeIfAbsent(queueUrl, k -> createPriorityQueue());
        queue.add(new Message(messageBody, priority)); // Add message to the queue
    }

    /* 
     * Pulls the next visible message from the queue, considering visibility timeout.
     * Sets new visibility and increments attempts before removing the message.
     */
    @Override
    public Message pull(String queueUrl) {
        Queue<Message> queue = queues.get(queueUrl);
        if (queue == null || queue.isEmpty()) {
            return null; // Return null if the queue is empty
        }

        long nowTime = System.currentTimeMillis();
        
        // Find a message that is visible based on the current time
        Optional<Message> msgOpt = queue.stream().filter(m -> m.isVisibleAt(nowTime)).findFirst();
        if (msgOpt.isEmpty()) {
            return null; // Return null if no message is visible
        } else {
            Message msg = msgOpt.get();

            // Set visibility to the current time + timeout
            msg.setVisibleFrom(nowTime + TimeUnit.SECONDS.toMillis(visibilityTimeout));

            // Update receipt ID and increment message attempts
            msg.setReceiptId(UUID.randomUUID().toString());
            msg.incrementAttempts();

            // Remove the message from the queue after it is pulled
            queue.remove(msg);

            // Return a new Message object without modifying original body or priority
            return new Message(msg.getBody(), msg.getPriority());
        }
    }

    /* 
     * Deletes a message from the queue using its receiptId.
     * It only deletes if the message is not visible based on the current time.
     */
    @Override
    public void delete(String queueUrl, String receiptId) {
        Queue<Message> queue = queues.get(queueUrl);
        if (queue != null) {
            long nowTime = System.currentTimeMillis();

            for (Iterator<Message> it = queue.iterator(); it.hasNext(); ) {
                Message msg = it.next();
                if (!msg.isVisibleAt(nowTime) && msg.getReceiptId().equals(receiptId)) {
                    it.remove(); // Remove the message with matching receiptId
                    break;
                }
            }
        }
    }

    /* 
     * Returns the size of the queue.
     */
    public int getQueueSize(String queueUrl) {
        Queue<Message> queue = queues.get(queueUrl);
        return queue == null ? 0 : queue.size(); // Return 0 if the queue does not exist
    }

    /* 
     * Helper method to create a priority queue with custom comparator
     * Higher priority messages are retrieved first, and if priorities match, FCFS is applied.
     */
    private Queue<Message> createPriorityQueue() {
        return new PriorityBlockingQueue<>(11, (msg1, msg2) -> {
            int priorityComparison = Integer.compare(msg2.getPriority(), msg1.getPriority()); // Higher priority first
            if (priorityComparison == 0) {
                return Long.compare(msg1.getTimestamp(), msg2.getTimestamp()); // First-Come, First-Served (FCFS) if priority is equal
            }
            return priorityComparison;
        });
    }
}
