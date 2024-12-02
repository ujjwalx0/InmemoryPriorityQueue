package com.example;

import redis.clients.jedis.Jedis;

public class UpstashRedisPriorityQueueService implements QueueService {
    private final Jedis jedis; // Redis client instance

    /* 
     * Constructor to initialize the Jedis client with Upstash connection details.
     * Establishes a secure (SSL) connection and authenticates with provided credentials.
     */
    public UpstashRedisPriorityQueueService(String redisHost, int redisPort, String redisAuth) {
        this.jedis = new Jedis(redisHost, redisPort, true); // SSL connection enabled
        this.jedis.auth(redisAuth); // Authenticate using Upstash credentials
    }

    /* 
     * This method is for pushing a message into the queue with just the message body.
     * We create a new Message object with the body and then call the main push method.
     */
    @Override
    public void push(String queueUrl, String messageBody) {
        Message message = new Message(messageBody);
        push(queueUrl, message); // Call to the actual push method with a Message object
    }

    /* 
     * Main push method where we handle the message prioritization and serialization.
     * If the priority or timestamp is missing, we set defaults and compute the score.
     * Then, the message is added to Redis' sorted set with the calculated score.
     */
    public void push(String queueUrl, Message message) {
        if (message.getPriority() == null) {
            throw new IllegalArgumentException("Priority must be set for Upstash queue."); // Priority validation
        }

        if (message.getTimestamp() == null) {
            message.setTimestamp(System.currentTimeMillis()); // Set timestamp if missing
        }

        double score = computeScore(message.getPriority(), message.getTimestamp()); // Calculate score based on priority and timestamp
        jedis.zadd(queueUrl, score, serializeMessage(message)); // Add message to Redis sorted set with score
    }

    /* 
     * Pull method retrieves the highest priority message by fetching the one with the lowest score.
     * It removes the message from the queue after retrieval to maintain the queue state.
     * If the queue is empty, it returns null.
     */
    @Override
    public Message pull(String queueUrl) {
        String serializedMessage = jedis.zrange(queueUrl, 0, 0).stream().findFirst().orElse(null); // Retrieve the highest-priority message

        if (serializedMessage != null) {
            jedis.zrem(queueUrl, serializedMessage); // Remove the message from the queue after fetching
            return deserializeMessage(serializedMessage); // Deserialize the message before returning
        }

        return null; // If no message found, return null indicating an empty queue
    }

    /* 
     * Method to delete a message from the queue.
     * Currently, deletion is handled automatically by the pull operation.
     * If necessary, additional logic can be added to handle deletion by receiptId.
     */
    @Override
    public void delete(String queueUrl, String receiptId) {
        // No-op for now as messages are automatically removed during the pull operation
    }

    /* 
     * Compute the Redis score based on priority and timestamp.
     * Higher priority results in a lower score. Timestamps are used to break ties within the same priority.
     */
    private double computeScore(int priority, long timestamp) {
        return (double) (Integer.MAX_VALUE - priority) + (timestamp / 1_000_000.0); // Priority-based score with timestamp for FCFS
    }

    /* 
     * Serialize the message into a single string in the format: "priority:timestamp:body"
     * This format is simple and effective for storage in Redis.
     */
    private String serializeMessage(Message message) {
        return message.getPriority() + ":" + message.getTimestamp() + ":" + message.getBody(); // Serialize message into a string
    }

    /* 
     * Deserialize a message from the serialized string format "priority:timestamp:body".
     * This is necessary to convert the stored string back into a usable Message object.
     */
    private Message deserializeMessage(String serialized) {
        String[] parts = serialized.split(":", 3); // Split the serialized string into components
        int priority = Integer.parseInt(parts[0]); // Extract priority
        long timestamp = Long.parseLong(parts[1]); // Extract timestamp
        String body = parts[2]; // Extract body

        Message message = new Message(body);
        message.setPriority(priority); // Set the priority from the extracted value
        message.setTimestamp(timestamp); // Set the timestamp from the extracted value
        return message; // Return the fully reconstructed Message object
    }

    /* 
     * Clear all messages from the queue. This method deletes the Redis sorted set entirely.
     */
    public void clearQueue(String queueUrl) {
        jedis.del(queueUrl); // Delete the entire queue from Redis
    }

    /* 
     * Get the current size of the queue. This returns the number of messages in the Redis sorted set.
     */
    public int getQueueSize(String queueUrl) {
        return Math.toIntExact(jedis.zcard(queueUrl)); // Get the count of messages in the sorted set (queue)
    }

    /* 
     * Close the Jedis connection. Always ensure to close the connection to avoid resource leaks.
     */
    public void close() {
        jedis.close(); // Close the Redis client connection safely
    }
}
