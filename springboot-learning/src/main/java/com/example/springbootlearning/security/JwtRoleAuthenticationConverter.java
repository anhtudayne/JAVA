package com.example.springbootlearning.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * 📘 BÀI 20 — JwtRoleAuthenticationConverter
 *
 * Chuyển đổi (convert) các Claims từ Nimbus Jwt sang Spring Security GrantedAuthority:
 * 1. Đọc scopes từ claim OAuth2 tiêu chuẩn "scope" / "scp" (tiền tố SCOPE_)
 * 2. Đọc roles từ claim tùy biến "roles" (tiền tố ROLE_)
 * 3. Đặt Subject (email) làm Principal Name
 * 4. Trả về JwtAuthenticationToken chứa đầy đủ authorities cho @PreAuthorize / SecurityFilterChain
 */
@Component
public class JwtRoleAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter defaultScopeConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = new HashSet<>();

        // 1. Trích xuất authorities mặc định từ scope / scp claims (VD: SCOPE_read, SCOPE_write)
        Collection<GrantedAuthority> scopeAuthorities = defaultScopeConverter.convert(jwt);
        if (scopeAuthorities != null) {
            authorities.addAll(scopeAuthorities);
        }

        // 2. Trích xuất roles từ custom claim "roles" (VD: ["ROLE_ADMIN", "ROLE_USER"] hoặc ["ADMIN", "USER"])
        Object rolesClaim = jwt.getClaim("roles");
        if (rolesClaim instanceof List<?> rolesList) {
            for (Object roleObj : rolesList) {
                if (roleObj instanceof String roleName) {
                    String authority = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
                    authorities.add(new SimpleGrantedAuthority(authority));
                }
            }
        }

        // 3. Principal claim name là subject (email / username)
        String principalClaimName = jwt.getSubject();

        return new JwtAuthenticationToken(jwt, authorities, principalClaimName);
    }
}
