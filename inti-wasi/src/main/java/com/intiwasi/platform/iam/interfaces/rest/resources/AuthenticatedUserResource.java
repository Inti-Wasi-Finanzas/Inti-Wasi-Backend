package com.intiwasi.platform.iam.interfaces.rest.resources;

import com.intiwasi.platform.iam.domain.model.valueobjects.Roles;

public record AuthenticatedUserResource(Long id, String username, Roles role, String token) {

}
