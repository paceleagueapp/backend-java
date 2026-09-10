package com.paceleague.admin.application.port.out;

public interface AdminSessionStorePort {
    String issue(Long adminSno);

    void revoke(String sessionToken);
}
