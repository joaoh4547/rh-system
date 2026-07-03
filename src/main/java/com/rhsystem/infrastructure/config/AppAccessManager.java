package com.rhsystem.infrastructure.config;

import com.rhsystem.application.port.AccessManager;
import com.rhsystem.domain.model.Functionality;
import com.rhsystem.domain.model.usuario.User;
import com.rhsystem.domain.repository.UserRepository;
import com.vaadin.flow.spring.security.AuthenticationContext;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@AllArgsConstructor
public class AppAccessManager implements AccessManager {

    private final AuthenticationContext authenticationContext;
    private UserRepository userRepository;

    @Override
    public boolean hasAccess(Functionality functionality) {
        return loadFunctionalities().contains(functionality);
    }

    @Override
    public boolean hasAccessAny(Functionality... functionalities) {
        return loadFunctionalities().containsAll(List.of(functionalities));
    }

    @Override
    public boolean hasAccessAll(Functionality... functionalities) {
        return loadFunctionalities().containsAll(List.of(functionalities));
    }

    private User loadUser() {
        var codUser = authenticationContext.getPrincipalName().orElseThrow();
        return userRepository.findByUsername(codUser).orElseThrow();
    }

    private Collection<Functionality> loadFunctionalities() {
        var user = loadUser();
//      TODO: implementar busca das funcionalidades  return user.getFunctionalities();
        return new ArrayList<>();
    }
}
