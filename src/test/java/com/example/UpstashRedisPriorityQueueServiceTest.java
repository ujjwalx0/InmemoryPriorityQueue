package com.example;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class UpstashRedisPriorityQueueServiceTest {

    private UpstashRedisPriorityQueueService queueService; // Queue service for interacting with Upstash Redis
    private String queueUrl = "testQueue"; // Queue URL to be used in tests

    /* 
     * Setup method that initializes the queue service before each test.
     * The queue is cleared to ensure tests start with an empty queue.
     */
    @Before
    public void setUp() {
        // Initialize Upstash queue service with credentials and host information
        queueService = new UpstashRedisPriorityQueueService("humane-barnacle-43927.upstash.io", 6379, 
                                                             "AauXAAIjcDFlZWVlMzgxNmU0Yjc0OTQ5YTBjODNmNzNhNTYyNDA3YXAxMA");
        queueService.clearQueue(queueUrl); // Clear any existing messages from the queue before each test
    }

    /* 
     * Test for pushing messages with different priorities into the queue.
     * The test checks that messages are pulled in the correct priority order.
     */
    @Test
    public void testPushAndPullWithPriority() {
        // Push messages with varying priority levels into the queue
        queueService.push(queueUrl, new Message("Low priority message", 1)); // Low priority
        queueService.push(queueUrl, new Message("High priority message", 10)); // High priority
        queueService.push(queueUrl, new Message("Medium priority message", 5)); // Medium priority

        // Pull the highest priority message (should be "High priority message")
        Message msg = queueService.pull(queueUrl);
        assertNotNull(msg); // Ensure the message is not null
        assertEquals("High priority message", msg.getBody()); // Check the content of the message

        // Pull the medium priority message (should be "Medium priority message")
        msg = queueService.pull(queueUrl);
        assertNotNull(msg);
        assertEquals("Medium priority message", msg.getBody());

        // Pull the low priority message (should be "Low priority message")
        msg = queueService.pull(queueUrl);
        assertNotNull(msg);
        assertEquals("Low priority message", msg.getBody());
    }

    /* 
     * Test to check the queue size functionality.
     * This test ensures that the queue size is updated correctly after pushing and pulling messages.
     */
    @Test
    public void testQueueSize() {
        // Push two messages into the queue
        queueService.push(queueUrl, new Message("Message 1", 5));
        queueService.push(queueUrl, new Message("Message 2", 3));

        // Check if the queue size is 2 after pushing two messages
        assertEquals(2, queueService.getQueueSize(queueUrl)); // Expected queue size: 2

        // Pull one message from the queue
        queueService.pull(queueUrl); // Remove the first message

        // Check if the queue size is 1 after pulling one message
        assertEquals(1, queueService.getQueueSize(queueUrl)); // Expected queue size: 1
    }

    /* 
     * Tear down method to clear the queue and close the Jedis connection after each test.
     * This ensures that each test starts with a clean state and no resources are left open.
     */
    @After
    public void tearDown() {
        queueService.clearQueue(queueUrl); // Clear the queue after each test
        queueService.close(); // Close the Jedis connection to release resources
    }
}
