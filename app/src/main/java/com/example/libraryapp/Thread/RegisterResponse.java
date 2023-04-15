package com.example.libraryapp.Thread;

import java.util.Date;
import java.util.List;

public class RegisterResponse {
    private int jsonrpc;
    private String id;
    private List<Error> error;

    public List<Error> getError() {
        return error;
    }
    public static class Error {
        private int code;
        private List<Data> data;
        public int getCode() {
            return code;
        }
        public List<Data> getError() {
            return data;
        }
    }
    public static class Data {
        private String message;
        public String getMessage() {
            return message;
        }
    }
}
