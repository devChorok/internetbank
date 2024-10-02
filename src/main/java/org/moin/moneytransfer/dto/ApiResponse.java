package org.moin.moneytransfer.dto;

public class ApiResponse<T> {
    private String status;
    private String message;
    private T data;

    // 생성자, Getter, Setter

    public ApiResponse(String status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    // Getter와 Setter
}
