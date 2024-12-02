# Message Queues

This project is an implementation of message queues with basic features.

## Background

Message queues are a ubiquitous mechanism for achieving horizontal scalability. However, many production message services (e.g., Amazon's SQS) do not come with an offline implementation suitable for local development and testing. The purpose of this project is to resolve this deficiency by designing a simple message-queue API that supports several implementations:

- **In-memory queue**: Suitable for same-JVM producers and consumers. The in-memory queue is thread-safe.
- **File-based queue**: Suitable for same-host producers and consumers, but potentially different JVMs. The file-based queue is thread-safe and inter-process safe when run in a *nix environment.
- **SQS-based queue**: Adapter for using a production queue service like Amazon's SQS.
- **Upstash Redis-based queue**: A Redis-based implementation using Upstash for scalable queueing.
- **In-memory priority queue**: Implements a priority queue service with the option to pull messages by priority, using an in-memory solution.

The intended usage is that application components are written to use queues via the common interface (`QueueService`), and injected with an instance suitable for the environment in which that component is running (development, testing, integration-testing, staging, production, etc.).

## Main Features of Message Queue
- **Multiplicity**  
  A queue supports many producers and many consumers.
  
- **Delivery**  
  A queue strives to deliver each message exactly once to exactly one consumer, but guarantees at-least-once delivery (it can re-deliver a message to a consumer or deliver a message to multiple consumers in some cases).
  
- **Order**  
  A queue strives to deliver messages in FIFO order but makes no guarantee about delivery order.
  
- **Reliability**  
  When a consumer receives a message, it is not removed from the queue. Instead, it is temporarily suppressed (becomes "invisible"). If the consumer that received the message does not subsequently delete it within a timeout period (the "visibility timeout"), the message automatically becomes visible at the head of the queue again, ready to be delivered to another consumer.

## Code Structure

The code is organized under the `com.example` package.

1. **QueueService.java**  
   The interface to cater for the essential queue actions:
   - **push**: Pushes a single message onto a specified queue.
   - **pull**: Receives a single message from a specified queue.
   - **delete**: Deletes a received message.
  
2. **InMemoryQueueService.java**  
   An in-memory version of `QueueService`. The in-memory queue is thread-safe.

3. **FileQueueService.java**  
   Implements a file-based version of the interface, which uses the file system to coordinate between producers and consumers in different JVMs (i.e., thread-safe in a single VM, but also inter-process safe when used concurrently in multiple VMs).

4. **SqsQueueService.java**  
   An adapter for using Amazon SQS (Simple Queue Service) to handle queueing in production environments.

5. **UpstashRedisPriorityQueueService.java**  
   An adapter for Upstash Redis, implementing a priority queue system using Redis for scalable message storage and retrieval. This implementation provides a highly scalable solution leveraging Redis' sorted sets for message priority handling.

6. **InMemoryPriorityQueueService.java**  
   An in-memory implementation of a priority queue, where messages are added with a priority level and pulled in priority order.

7. **Config File**  
   `src/main/resources/config.properties` - Configuration file for various queue settings such as `visibilityTimeout`.

8. **Unit Tests**  
   Unit tests covering different queue implementations and their behavior, including the visibility timeout for the queues.

## Building and Running

You can use Maven to run tests from the command line with:
  ```bash
  mvn package



This **README.md** now includes all the relevant sections, such as new queue implementations like Upstash Redis and InMemory Priority Queue, with the same structure as the previous README.
