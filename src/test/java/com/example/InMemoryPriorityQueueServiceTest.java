package com.example;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class InMemoryPriorityQueueServiceTest {

    private InMemoryPriorityQueueService priorityQueueService;
    private String queueUrl = "https://sqs.ap-1.amazonaws.com/007/MyQueue"; // Test queue URL

    /* 
     * Sets up the test environment before each test.
     * Initializes the priorityQueueService instance.
     */
    @Before
    public void setup() {
        priorityQueueService = new InMemoryPriorityQueueService(); // Initialize queue service
    }

    /* 
     * Test to push messages with different priorities and verify the queue size.
     */
    @Test
    public void testPushMessages() {
        // Push messages with different priorities
        priorityQueueService.push(queueUrl, "Low priority message", 1);
        priorityQueueService.push(queueUrl, "High priority message", 10);
        priorityQueueService.push(queueUrl, "Medium priority message", 5);

        // Verify that three messages have been added to the queue
        assertEquals(3, priorityQueueService.getQueueSize(queueUrl)); 
    }

    /* 
     * Test to ensure that the highest priority message is pulled first.
     */
    @Test
    public void testPullHighestPriorityMessage() {
        // Push messages with different priorities
        priorityQueueService.push(queueUrl, "Low priority message", 1);
        priorityQueueService.push(queueUrl, "High priority message", 10);
        priorityQueueService.push(queueUrl, "Medium priority message", 5);

        // Pull the highest priority message (should be "High priority message")
        Message msg = priorityQueueService.pull(queueUrl);
        assertNotNull(msg); // Ensure message is not null
        assertEquals("High priority message", msg.getBody()); // Check if the correct message is pulled
    }

    /* 
     * Test to pull messages in the correct order based on priority.
     */
    @Test
    public void testPullNextPriorityMessage() {
        // Push messages with different priorities
        priorityQueueService.push(queueUrl, "Medium priority message", 5);
        priorityQueueService.push(queueUrl, "Low priority message", 1);

        // Pull the medium priority message first
        Message msg = priorityQueueService.pull(queueUrl);
        assertNotNull(msg);
        assertEquals("Medium priority message", msg.getBody()); // Should pull medium priority message

        // Pull the low priority message next
        msg = priorityQueueService.pull(queueUrl);
        assertNotNull(msg);
        assertEquals("Low priority message", msg.getBody()); // Should pull low priority message
    }

    /* 
     * Test to ensure FCFS (First-Come, First-Served) within the same priority level.
     */
    @Test
    public void testFCFSWithinSamePriority() {
        // Push messages with the same priority
        priorityQueueService.push(queueUrl, "Message 1", 5);
        priorityQueueService.push(queueUrl, "Message 2", 5);

        // Pull the first message
        Message msg = priorityQueueService.pull(queueUrl);
        assertNotNull(msg);
        assertEquals("Message 1", msg.getBody()); // Should pull the first message

        // Pull the second message
        msg = priorityQueueService.pull(queueUrl);
        assertNotNull(msg);
        assertEquals("Message 2", msg.getBody()); // Should pull the second message
    }

    /* 
     * Test to ensure that pulling from an empty queue returns null.
     */
    @Test
    public void testPullFromEmptyQueue() {
        // Attempt to pull from an empty queue
        Message msg = priorityQueueService.pull(queueUrl);
        assertNull(msg); // Should return null as the queue is empty
    }
}
