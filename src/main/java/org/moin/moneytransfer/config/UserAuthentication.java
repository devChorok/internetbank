package org.moin.moneytransfer.config;

import org.springframework.security.core.*;
import java.util.Collection;

public class UserAuthentication implements Authentication {

    private final String userId;
    private boolean authenticated = true;

    public UserAuthentication(String userId) {
        this.userId = userId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 권한 정보를 반환합니다. 필요에 따라 구현하세요.
        return null;
    }

    @Override
    public Object getCredentials() {
        return null; // 비밀번호 등 자격 증명 정보
    }

    @Override
    public Object getDetails() {
        return null; // 추가적인 상세 정보
    }

    @Override
    public Object getPrincipal() {
        return userId; // 사용자 ID를 반환
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        this.authenticated = isAuthenticated;
    }

    @Override
    public String getName() {
        return userId;
    }
}
