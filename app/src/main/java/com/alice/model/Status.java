package com.alice.model;

/**
 * Auto-generated: 2019-09-19 17:30:45
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class Status {

    private String timestamp;
    private int error_code;
    private String error_message;
    private int elapsed;
    private int credit_count;
    private String notice;

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    public String getTimestamp() {
        return timestamp;
    }

    public void setError_code(int error_code) {
        this.error_code = error_code;
    }
    public int getError_code() {
        return error_code;
    }

    public void setError_message(String error_message) {
        this.error_message = error_message;
    }
    public String getError_message() {
        return error_message;
    }

    public void setElapsed(int elapsed) {
        this.elapsed = elapsed;
    }
    public int getElapsed() {
        return elapsed;
    }

    public void setCredit_count(int credit_count) {
        this.credit_count = credit_count;
    }
    public int getCredit_count() {
        return credit_count;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }
    public String getNotice() {
        return notice;
    }

}