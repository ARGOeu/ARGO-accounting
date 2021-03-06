package org.accounting.system.mappers;

import org.accounting.system.beans.RequestInformation;
import org.accounting.system.dtos.authorization.request.RoleRequestDto;
import org.accounting.system.dtos.authorization.response.RoleResponseDto;
import org.accounting.system.dtos.authorization.update.UpdateCollectionAccessPermissionDto;
import org.accounting.system.dtos.authorization.update.UpdateRoleRequestDto;
import org.accounting.system.entities.authorization.CollectionPermission;
import org.accounting.system.entities.authorization.Role;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import javax.enterprise.inject.spi.CDI;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * This interface is responsible for turning a Role Entity into a request/response and vice versa.
 */
@Mapper(uses= UtilMapper.class, imports = {StringUtils.class})
public interface RoleMapper {

    RoleMapper INSTANCE = Mappers.getMapper( RoleMapper.class );

    List<RoleResponseDto> rolesToResponse(List<Role> roles);

    Set<CollectionPermission> updateCollectionPermissionToCollectionPermission(Set<UpdateCollectionAccessPermissionDto> permissions);

    Role requestToRole(RoleRequestDto request);

    RoleResponseDto roleToResponse(Role response);

    @Mapping(target = "name", expression = "java(StringUtils.isNotEmpty(request.name) ? request.name : role.getName())")
    @Mapping(target = "description", expression = "java(StringUtils.isNotEmpty(request.description) ? request.description : role.getDescription())")
    @Mapping(source = "collectionsAccessPermissions", target = "collectionsAccessPermissions", qualifiedByName = "access_permissions")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateRoleFromDto(UpdateRoleRequestDto request, @MappingTarget Role role);

    @Named("access_permissions")
    default Set<CollectionPermission> accessPermissions(Set<UpdateCollectionAccessPermissionDto> permissions) {

        if(Objects.nonNull(permissions) && permissions
                    .stream()
                    .allMatch(collectionPermission -> Objects.nonNull(collectionPermission.collection) && Objects.nonNull(collectionPermission.accessPermissions))
                    &&
                    permissions
                            .stream()
                            .map(cp->cp.accessPermissions)
                            .flatMap(Collection::stream)
                            .allMatch(permission -> Objects.nonNull(permission.operation) && Objects.nonNull(permission.accessType))){

                return updateCollectionPermissionToCollectionPermission(permissions);
            }
        return null;
    }

    @AfterMapping
    default void setCreatorId(RoleRequestDto source, @MappingTarget Role role) {
        RequestInformation requestInformation = CDI.current().select(RequestInformation.class).get();
        role.setCreatorId(requestInformation.getSubjectOfToken());
    }
}