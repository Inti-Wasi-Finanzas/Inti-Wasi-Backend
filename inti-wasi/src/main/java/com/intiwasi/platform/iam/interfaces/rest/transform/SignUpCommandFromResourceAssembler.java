package com.intiwasi.platform.iam.interfaces.rest.transform;

import com.intiwasi.platform.iam.domain.model.commands.SignUpCommand;
import com.intiwasi.platform.iam.interfaces.rest.resources.SignUpResource;

import java.util.ArrayList;

public class SignUpCommandFromResourceAssembler {
    public static SignUpCommand toCommandFromResource(SignUpResource resource) {
        return new SignUpCommand(
                resource.username(),
                resource.password(),
                resource.role());
    }
}
