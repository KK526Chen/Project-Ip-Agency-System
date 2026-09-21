package com.ipagency.vo;

public record LoginVO(String token, UserProfileVO user) {
    @Override public String toString() { return "LoginVO[token=<redacted>, user=" + user + "]"; }
}
