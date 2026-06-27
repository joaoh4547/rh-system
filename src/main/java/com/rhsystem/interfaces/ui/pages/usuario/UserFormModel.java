package com.rhsystem.interfaces.ui.pages.usuario;

import com.rhsystem.domain.model.usuario.UserStatus;
import com.rhsystem.domain.model.usuario.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Editable bean for the User form (bound via Binder). */
@Getter
@Setter
@NoArgsConstructor
public class UserFormModel {

    private String firstName;
    private String lastName;
    private String email;
    private String cpf;
    private String rg;
    private UserStatus status;

    private String street;
    private String neighborhood;
    private String streetNumber;
    private String complement;
    private String postalCode;

    /** Creates the model from an existing user (edit mode). */
    public static UserFormModel from(User u) {
        UserFormModel m = new UserFormModel();
        m.firstName = u.getFirstName();
        m.lastName = u.getLastName();
        m.email = u.getEmail();
        m.cpf = u.getCpf();
        m.rg = u.getRg();
        m.status = u.getStatus();
        if (u.getAddress() != null) {
            m.street = u.getAddress().getStreet();
            m.neighborhood = u.getAddress().getNeighborhood();
            m.streetNumber = u.getAddress().getStreetNumber();
            m.complement = u.getAddress().getComplement();
            m.postalCode = u.getAddress().getPostalCode();
        }
        return m;
    }
}
