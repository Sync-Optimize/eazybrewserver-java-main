package com.eazybrew.vend.paystack.dto.response;

public class Meta {
    private String next;
    private String previous;
    private int perPage;

    // Getters and Setters
    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public String getPrevious() {
        return previous;
    }

    public void setPrevious(String previous) {
        this.previous = previous;
    }

    public int getPerPage() {
        return perPage;
    }

    public void setPerPage(int perPage) {
        this.perPage = perPage;
    }
}

