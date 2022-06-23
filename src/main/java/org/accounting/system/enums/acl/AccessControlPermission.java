package org.accounting.system.enums.acl;

/**
 * Defines the permissions for use with the permissions component of an Access Control
 * {@link org.accounting.system.entities.acl.AccessControl access control}
 */
public enum AccessControlPermission {

    /**
     * Permission to read a particular entity.
     */
    READ,
    /**
     * Permission to update a particular entity.
     */
    UPDATE,
    /**
     * Permission to delete a particular entity.
     */
    DELETE,
    /**
     * Permission to grant acl to particular entity.
     */
    ACL,
    /**
     * Permission to grant access to particular Project.
     */
    ACCESS_PROJECT,
    /**
     * Permission to grant access to particular Provider.
     */
    ACCESS_PROVIDER;
}
