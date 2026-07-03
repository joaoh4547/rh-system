package com.rhsystem.application.port;

import com.rhsystem.domain.model.Functionality;

public interface AccessManager {

    boolean hasAccess(Functionality functionality);

    boolean hasAccessAny(Functionality... functionalities);

    boolean hasAccessAll(Functionality... functionalities);
    
}
