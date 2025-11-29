package com.intiwasi.platform.iam.interfaces.rest.transform;


import com.intiwasi.platform.iam.domain.model.aggregates.User;
import com.intiwasi.platform.iam.interfaces.rest.resources.AuthenticatedUserResource;

public class AuthenticatedUserResourceFromEntityAssembler {
    public static AuthenticatedUserResource toResourceFromEntity(User user, String token) {
        return new AuthenticatedUserResource(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                token);
    }
}
