package com.intiwasi.platform.iam.interfaces.rest.resources;

import com.intiwasi.platform.iam.domain.model.valueobjects.Roles;

import java.util.List;

public record SignUpResource(String username, String password, Roles role) {
}
