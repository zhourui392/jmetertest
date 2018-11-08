package com.zz.data;

public class LoginResponseData {
    /**
     * agoraKey : 914252d1293c457f8f9f6b2b5b2453df
     * token : eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJwYWQiLCJuYW1lIjoicGFkIiwic2hvd05hbWUiOiJwYWQiLCJpZGVudGl0eSI6IkFGNDEyRjM5OUUwQ0Y3QjIiLCJzY29wZXMiOlsiUk9MRV9QQUQiXSwiaXNzIjoiaHR0cDovL3N2bGFkYS5jb20iLCJpYXQiOjE1MzU2MDA1MDcsImV4cCI6MTUzNTYzNjUwN30.YJD0kKzftp3R3nisEsNylxADm45GNrSwEdAh8PfXZmdJf8n32qTVrxrd_0X1_kiQxJ_ox5Qd0pSNlAK3Wb96Sg
     */
    private String agoraKey;
    private String token;

    public String getAgoraKey() {
        return agoraKey;
    }

    public void setAgoraKey(String agoraKey) {
        this.agoraKey = agoraKey;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "LoginResponseData{" +
                "agoraKey='" + agoraKey + '\'' +
                ", token='" + token + '\'' +
                '}';
    }
}
