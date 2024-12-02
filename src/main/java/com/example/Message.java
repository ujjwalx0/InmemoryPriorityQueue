package com.example;

public class Message {
  /** How many times this message has been delivered. */
  private int attempts;

  /** Visible from time */
  private long visibleFrom;

  /** An identifier associated with the act of receiving the message. */
  private String receiptId;

  private String msgBody;
  
  // Priority of the message (used only for the priority queue)
  private Integer priority;  // Using Integer instead of int to allow null (optional)
  
  // Timestamp for FCFS (used only for the priority queue)
  private Long timestamp;  // Using Long instead of long to allow null (optional)

  // Constructor for normal use cases
  public Message(String msgBody) {
    this.msgBody = msgBody;
    this.priority = null;  // Default null, meaning no priority
    this.timestamp = null; // Default null, meaning no timestamp
  }

  // Constructor for cases where receiptId is needed
  public Message(String msgBody, String receiptId) {
    this.msgBody = msgBody;
    this.receiptId = receiptId;
    this.priority = null;  // Default null, meaning no priority
    this.timestamp = null; // Default null, meaning no timestamp
  }

  // Constructor for cases where priority and timestamp are required (e.g., InMemoryPriorityQueue)
  public Message(String msgBody, int priority) {
    this.msgBody = msgBody;
    this.priority = priority;
    this.timestamp = System.currentTimeMillis(); // Record timestamp for FCFS
  }

  // Getters and setters for receiptId
  public String getReceiptId() {
    return this.receiptId;
  }

  protected void setReceiptId(String receiptId) {
    this.receiptId = receiptId;
  }

  public void setVisibleFrom(long visibleFrom) {
      this.visibleFrom = visibleFrom;
  }

  public long getVisibleFrom() {
      return visibleFrom;
  }

  // Check visibility at a certain time
  public boolean isVisibleAt(long instant) {
    return visibleFrom < instant;
  }

  // Getter for message body
  public String getBody() {
    return msgBody;
  }

  // Getter for attempts count
  protected int getAttempts() {
    return attempts;
  }

  // Increment attempts count
  protected void incrementAttempts() {
    this.attempts++;
  }

  // Getters and setters for priority (used in priority queue implementation)
  public Integer getPriority() {
    return priority;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  // Getters and setters for timestamp (used in priority queue implementation)
  public Long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }
}
