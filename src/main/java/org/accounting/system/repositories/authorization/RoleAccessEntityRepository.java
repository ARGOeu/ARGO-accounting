package org.accounting.system.repositories.authorization;

import org.accounting.system.entities.acl.PermissionAccessControl;
import org.accounting.system.entities.authorization.Role;
import org.accounting.system.repositories.modulators.AccessControlModulator;
import org.accounting.system.repositories.modulators.AccessEntityModulator;
import org.bson.types.ObjectId;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class RoleAccessEntityRepository extends AccessEntityModulator<Role, ObjectId, PermissionAccessControl> {

    @Inject
    RoleAccessControlRepository roleAccessControlRepository;

    @Override
    public AccessControlModulator<Role, ObjectId, PermissionAccessControl> accessControlModulator() {
        return roleAccessControlRepository;
    }
}
