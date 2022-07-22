package org.accounting.system.endpoints;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.quarkus.security.Authenticated;
import org.accounting.system.constraints.AccessInstallation;
import org.accounting.system.constraints.AccessProvider;
import org.accounting.system.constraints.NotFoundEntity;
import org.accounting.system.dtos.InformativeResponse;
import org.accounting.system.dtos.acl.role.RoleAccessControlRequestDto;
import org.accounting.system.dtos.acl.role.RoleAccessControlResponseDto;
import org.accounting.system.dtos.acl.role.RoleAccessControlUpdateDto;
import org.accounting.system.dtos.installation.InstallationRequestDto;
import org.accounting.system.dtos.installation.InstallationResponseDto;
import org.accounting.system.dtos.installation.UpdateInstallationRequestDto;
import org.accounting.system.dtos.metric.MetricRequestDto;
import org.accounting.system.dtos.metric.MetricResponseDto;
import org.accounting.system.dtos.metric.UpdateMetricRequestDto;
import org.accounting.system.dtos.pagination.PageResource;
import org.accounting.system.dtos.project.ProjectResponseDto;
import org.accounting.system.entities.HierarchicalRelation;
import org.accounting.system.entities.projections.MetricProjection;
import org.accounting.system.enums.Collection;
import org.accounting.system.enums.Operation;
import org.accounting.system.repositories.client.ClientRepository;
import org.accounting.system.repositories.installation.InstallationRepository;
import org.accounting.system.repositories.metric.MetricRepository;
import org.accounting.system.services.HierarchicalRelationService;
import org.accounting.system.services.MetricService;
import org.accounting.system.services.acl.AccessControlService;
import org.accounting.system.services.installation.InstallationService;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeIn;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.specimpl.ResteasyUriInfo;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.text.ParseException;

import static org.accounting.system.enums.Operation.*;
import static org.eclipse.microprofile.openapi.annotations.enums.ParameterIn.QUERY;

@Path("/installations")
@Authenticated
@SecurityScheme(securitySchemeName = "Authentication",
        description = "JWT token",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER)

public class InstallationEndpoint {

    @ConfigProperty(name = "quarkus.resteasy.path")
    String basePath;

    @ConfigProperty(name = "server.url")
    String serverUrl;

    @Inject
    InstallationService installationService;

    @Inject
    HierarchicalRelationService hierarchicalRelationService;

    @Inject
    AccessControlService accessControlService;

    @Inject
    InstallationRepository installationRepository;

    @Inject
    MetricService metricService;

    @Tag(name = "Installation")
    @org.eclipse.microprofile.openapi.annotations.Operation(
            summary = "Generates a new Installation.",
            description = "This operation is responsible for generating and storing in the Accounting System database " +
                    "a new Installation.")
    @APIResponse(
            responseCode = "201",
            description = "Installation has been created successfully.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InstallationResponseDto.class)))
    @APIResponse(
            responseCode = "400",
            description = "Bad Request.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "401",
            description = "Client has not been authenticated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "403",
            description = "The authenticated client is not permitted to perform the requested operation.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "409",
            description = "There is a Metric Definition with that unit_type and metric_name.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "415",
            description = "Cannot consume content type.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Errors.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @SecurityRequirement(name = "Authentication")

    @POST
    @Produces(value = MediaType.APPLICATION_JSON)
    @Consumes(value = MediaType.APPLICATION_JSON)
    @AccessProvider(collection = Collection.Installation, operation = CREATE)
    public Response save(InstallationRequestDto installationRequestDto, @Context UriInfo uriInfo) {

        var serverInfo = new ResteasyUriInfo(serverUrl.concat(basePath).concat(uriInfo.getPath()), basePath);

        var belongs = hierarchicalRelationService.providerBelongsToProject(installationRequestDto.project, installationRequestDto.organisation);

        if (!belongs) {
            String message = String.format("There is no relationship between Project {%s} and Provider {%s}", installationRequestDto.project, installationRequestDto.organisation);
            throw new BadRequestException(message);
        }

        var response = installationService.save(installationRequestDto);

        return Response.created(serverInfo.getAbsolutePathBuilder().path(response.id).build()).entity(response).build();
    }

    @Tag(name = "Installation")
    @org.eclipse.microprofile.openapi.annotations.Operation(
            summary = "Deletes an existing Installation.",
            description = "This operation deletes an existing Installation registered through Accounting System API.")
    @APIResponse(
            responseCode = "200",
            description = "Installation has been deleted successfully.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "401",
            description = "Client has not been authenticated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "403",
            description = "The authenticated client is not permitted to perform the requested operation.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "404",
            description = "Installation has not been found.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Errors.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @SecurityRequirement(name = "Authentication")

    @DELETE()
    @Path("/{id}")
    @Produces(value = MediaType.APPLICATION_JSON)
    public Response delete(@Parameter(
            description = "The Installation to be deleted.",
            required = true,
            example = "507f1f77bcf86cd799439011",
            schema = @Schema(type = SchemaType.STRING))
                           @PathParam("id")
                               @Valid
                               @AccessInstallation(collection = Collection.Installation, operation = Operation.DELETE) String id) {

        var success = installationService.delete(id);

        var successResponse = new InformativeResponse();

        if (success) {
            successResponse.code = 200;
            successResponse.message = "Installation has been deleted successfully.";
        } else {
            successResponse.code = 500;
            successResponse.message = "Installation cannot be deleted due to a server issue. Please try again.";
        }
        return Response.ok().entity(successResponse).build();
    }

    @Tag(name = "Installation")
    @org.eclipse.microprofile.openapi.annotations.Operation(
            summary = "Returns an existing Installation.",
            description = "This operation accepts the id of an Installation and fetches from the database the corresponding record.")
    @APIResponse(
            responseCode = "200",
            description = "The corresponding Installation.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InstallationResponseDto.class)))
    @APIResponse(
            responseCode = "401",
            description = "Client has not been authenticated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "403",
            description = "The authenticated client is not permitted to perform the requested operation.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "404",
            description = "Installation has not been found.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Errors.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @SecurityRequirement(name = "Authentication")

    @GET
    @Path("/{id}")
    @Produces(value = MediaType.APPLICATION_JSON)
    public Response get(
            @Parameter(
                    description = "The Installation to be retrieved.",
                    required = true,
                    example = "507f1f77bcf86cd799439011",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("id") @Valid
            @AccessInstallation(collection = Collection.Installation, operation = READ) String id) {

        var response = installationService.fetchInstallation(id);

        return Response.ok().entity(response).build();
    }

    @Tag(name = "Installation")
    @org.eclipse.microprofile.openapi.annotations.Operation(
            summary = "Updates an existing Installation.",
            description = "This operation updates an existing Installation registered through the Accounting System API. Finally, " +
                    "you can update a part or all attributes of Installation. The empty or null values are ignored.")
    @APIResponse(
            responseCode = "200",
            description = "Installation was updated successfully.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InstallationResponseDto.class)))
    @APIResponse(
            responseCode = "400",
            description = "Bad Request.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "401",
            description = "Client has not been authenticated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "403",
            description = "It is not permitted to perform the requested operation.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "404",
            description = "Installation has not been found.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "409",
            description = "The Installation already exists.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "415",
            description = "Cannot consume content type.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Errors.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @SecurityRequirement(name = "Authentication")

    @PATCH
    @Path("/{id}")
    @Produces(value = MediaType.APPLICATION_JSON)
    @Consumes(value = MediaType.APPLICATION_JSON)
    public Response update(
            @Parameter(
                    description = "The Installation to be updated.",
                    required = true,
                    example = "507f1f77bcf86cd799439011",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("id") @Valid
            @AccessInstallation(collection = Collection.Installation, operation = Operation.UPDATE) String id, @Valid @NotNull(message = "The request body is empty.") UpdateInstallationRequestDto updateInstallationRequestDto) {

        var response = installationService.update(id, updateInstallationRequestDto);

        return Response.ok().entity(response).build();
    }

    @Tag(name = "Installation")
    @org.eclipse.microprofile.openapi.annotations.Operation(
            summary = "Generates a new Access Control Entry.",
            description = "This endpoint is responsible for generating a new Access Control Entry. " +
                    "Access Control Entry rules specify which clients are granted or denied access to particular Installation entities. " +
                    "It should be noted that the combination {who, collection, entity} is unique. Therefore, only one Access Control entry can be created for each client and each entity.")
    @APIResponse(
            responseCode = "200",
            description = "Access Control entry has been created successfully.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "400",
            description = "Bad Request.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "401",
            description = "Client has not been authenticated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "403",
            description = "The authenticated client is not permitted to perform the requested operation.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "404",
            description = "Installation has not been found.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "409",
            description = "There is an Access Control Entry with this {who, collection, entity}.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "415",
            description = "Cannot consume content type.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Errors.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @SecurityRequirement(name = "Authentication")

    @POST
    @Path("/{installation_id}/acl/{who}")
    @Produces(value = MediaType.APPLICATION_JSON)
    @Consumes(value = MediaType.APPLICATION_JSON)
    public Response createAccessControl(@Valid @NotNull(message = "The request body is empty.") RoleAccessControlRequestDto roleAccessControlRequestDto,
                                        @Parameter(
                                                description = "installation_id is the id of the entity to which the permissions apply.",
                                                required = true,
                                                example = "507f1f77bcf86cd799439011",
                                                schema = @Schema(type = SchemaType.STRING))
                                        @PathParam("installation_id")
                                        @Valid
                                        @AccessInstallation(collection = Collection.Installation, operation = ACL) String installationId,
                                        @Parameter(
                                                description = "who is the id of a Client that the Access Control grants access.",
                                                required = true,
                                                example = "fbdb4e4a-6e93-4b08-a1e7-0b7bd08520a6",
                                                schema = @Schema(type = SchemaType.STRING))
                                            @PathParam("who") @Valid @NotFoundEntity(repository = ClientRepository.class, id = String.class, message = "There is no registered Client with the following id:") String who) {

        var storedInstallation = installationRepository.findById(new ObjectId(installationId));

        var informativeResponse = accessControlService.grantPermission(storedInstallation.getProject() + HierarchicalRelation.PATH_SEPARATOR + storedInstallation.getOrganisation() + HierarchicalRelation.PATH_SEPARATOR + installationId, who, roleAccessControlRequestDto, Collection.Installation);

        return Response.ok().entity(informativeResponse).build();
    }

    @Tag(name = "Installation")
    @org.eclipse.microprofile.openapi.annotations.Operation(
            summary = "Modify an existing Access Control Entry.",
            description = "This endpoint is responsible for updating an existing Access Control Entry. It will modify a specific Access Control Entry " +
                    "which has granted permissions on an Installation to a specific client." +
                    "You can update a part or all attributes of the Access Control entity.")
    @APIResponse(
            responseCode = "200",
            description = "Access Control entry has been updated successfully.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "400",
            description = "Bad Request.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "401",
            description = "Client has not been authenticated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "403",
            description = "The authenticated client is not permitted to perform the requested operation.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "404",
            description = "Installation has not been found.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "409",
            description = "There is an Access Control Entry with this {who, collection, entity}.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "415",
            description = "Cannot consume content type.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Errors.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @SecurityRequirement(name = "Authentication")

    @PATCH
    @Path("/{installation_id}/acl/{who}")
    @Produces(value = MediaType.APPLICATION_JSON)
    @Consumes(value = MediaType.APPLICATION_JSON)
    public Response modifyAccessControl(@Valid @NotNull(message = "The request body is empty.") RoleAccessControlUpdateDto roleAccessControlUpdateDto,
                                        @Parameter(
                                                description = "installation_id in which permissions have been granted.",
                                                required = true,
                                                example = "507f1f77bcf86cd799439011",
                                                schema = @Schema(type = SchemaType.STRING))
                                        @PathParam("installation_id")
                                        @Valid
                                        @AccessInstallation(collection = Collection.Installation, operation = ACL) String installationId,
                                        @Parameter(
                                                description = "who is the client to whom the permissions have been granted.",
                                                required = true,
                                                example = "fbdb4e4a-6e93-4b08-a1e7-0b7bd08520a6",
                                                schema = @Schema(type = SchemaType.STRING))
                                            @PathParam("who") @Valid @NotFoundEntity(repository = ClientRepository.class, id = String.class, message = "There is no registered Client with the following id:") String who) {

        var storedInstallation = installationRepository.findById(new ObjectId(installationId));

        var response = accessControlService.modifyPermission(storedInstallation.getProject() + HierarchicalRelation.PATH_SEPARATOR + storedInstallation.getOrganisation() + HierarchicalRelation.PATH_SEPARATOR + installationId, who, roleAccessControlUpdateDto, Collection.Installation);

        return Response.ok().entity(response).build();
    }

    @Tag(name = "Installation")
    @org.eclipse.microprofile.openapi.annotations.Operation(
            summary = "Deletes an existing Access Control entry.",
            description = "You can delete the permissions that a client can access to manage a specific Installation.")
    @APIResponse(
            responseCode = "200",
            description = "Access Control entry has been deleted successfully.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "401",
            description = "Client has not been authenticated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "403",
            description = "The authenticated client is not permitted to perform the requested operation.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "404",
            description = "Installation has not been found.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Errors.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @SecurityRequirement(name = "Authentication")

    @DELETE()
    @Path("/{installation_id}/acl/{who}")
    @Produces(value = MediaType.APPLICATION_JSON)
    public Response deleteAccessControl(@Parameter(
            description = "installation_id in which permissions have been granted.",
            required = true,
            example = "507f1f77bcf86cd799439011",
            schema = @Schema(type = SchemaType.STRING))
                                        @PathParam("installation_id")
                                        @Valid
                                        @AccessInstallation(collection = Collection.Installation, operation = ACL) String installationId,
                                        @Parameter(
                                                description = "who is the client to whom the permissions have been granted.",
                                                required = true,
                                                example = "fbdb4e4a-6e93-4b08-a1e7-0b7bd08520a6",
                                                schema = @Schema(type = SchemaType.STRING))
                                        @PathParam("who") @Valid @NotFoundEntity(repository = ClientRepository.class, id = String.class, message = "There is no registered Client with the following id:") String who) {

        var storedInstallation = installationRepository.findById(new ObjectId(installationId));

        var successResponse = accessControlService.deletePermission(storedInstallation.getProject() + HierarchicalRelation.PATH_SEPARATOR + storedInstallation.getOrganisation() + HierarchicalRelation.PATH_SEPARATOR + installationId, who, Collection.Installation);

        return Response.ok().entity(successResponse).build();
    }

    @Tag(name = "Installation")
    @org.eclipse.microprofile.openapi.annotations.Operation(
            summary = "Returns an existing Access Control entry.",
            description = "This operation returns the Access Control entry created for a client upon an Installation entity.")
    @APIResponse(
            responseCode = "200",
            description = "The corresponding Access Control entry.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = RoleAccessControlResponseDto.class)))
    @APIResponse(
            responseCode = "401",
            description = "Client has not been authenticated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "403",
            description = "The authenticated client is not permitted to perform the requested operation.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "404",
            description = "Access Control entry has not been found.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Errors.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @SecurityRequirement(name = "Authentication")

    @GET
    @Path("/{installation_id}/acl/{who}")
    @Produces(value = MediaType.APPLICATION_JSON)
    public Response getAccessControl(
            @Parameter(
                    description = "installation_id in which permissions have been granted.",
                    required = true,
                    example = "507f1f77bcf86cd799439011",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("installation_id")
            @Valid
            @AccessInstallation(collection = Collection.Installation, operation = ACL) String installationId,
            @Parameter(
                    description = "who is the client to whom the permissions have been granted.",
                    required = true,
                    example = "fbdb4e4a-6e93-4b08-a1e7-0b7bd08520a6",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("who") @Valid @NotFoundEntity(repository = ClientRepository.class, id = String.class, message = "There is no registered Client with the following id:") String who) {

        var storedInstallation = installationRepository.findById(new ObjectId(installationId));

        var response = accessControlService.fetchPermission(storedInstallation.getProject() + HierarchicalRelation.PATH_SEPARATOR + storedInstallation.getOrganisation() + HierarchicalRelation.PATH_SEPARATOR + installationId, who, Collection.Installation);

        return Response.ok().entity(response).build();
    }

    @Tag(name = "Installation")
    @org.eclipse.microprofile.openapi.annotations.Operation(
            summary = "Returns all Access Control entries that have been created for a particular Installation.",
            description = "Returns all Access Control entries that have been created for a particular Installation. " +
                    "By default, the first page of 10 Metrics will be returned. You can tune the default values by using the query parameters page and size.")
    @APIResponse(
            responseCode = "200",
            description = "The corresponding Access Control entries.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = ProjectEndpoint.PageableRoleAccessControlResponseDto.class)))
    @APIResponse(
            responseCode = "401",
            description = "Client has not been authenticated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "403",
            description = "The authenticated client is not permitted to perform the requested operation.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Errors.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @SecurityRequirement(name = "Authentication")

    @GET
    @Path("/{installation_id}/acl")
    @Produces(value = MediaType.APPLICATION_JSON)
    public Response getAllInstallationAccessControl(
            @Parameter(
                    description = "installation_id in which permissions have been granted.",
                    required = true,
                    example = "507f1f77bcf86cd799439011",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("installation_id")
            @Valid
            @AccessInstallation(collection = Collection.Installation, operation = ACL) String installationId,
            @Parameter(name = "page", in = QUERY,
                    description = "Indicates the page number. Page number must be >= 1.") @DefaultValue("1") @QueryParam("page") int page,
            @Parameter(name = "size", in = QUERY,
                    description = "The page size.") @DefaultValue("10") @QueryParam("size") int size,
            @Context UriInfo uriInfo) {

        if (page < 1) {
            throw new BadRequestException("Page number must be >= 1.");
        }

        var storedInstallation = installationRepository.findById(new ObjectId(installationId));

        var response = accessControlService.fetchAllPermissions(storedInstallation.getProject() + HierarchicalRelation.PATH_SEPARATOR + storedInstallation.getOrganisation() + HierarchicalRelation.PATH_SEPARATOR + installationId, Collection.Installation, page - 1, size, uriInfo);

        return Response.ok().entity(response).build();
    }

//    @Tag(name = "Installation")
//    @org.eclipse.microprofile.openapi.annotations.Operation(
//            summary = "Returns all Access Control entries that have been created for Installation collection.",
//            description = "Returns all Access Control entries that have been created for Installation collection.")
//    @APIResponse(
//            responseCode = "200",
//            description = "The corresponding Access Control entries.",
//            content = @Content(schema = @Schema(
//                    type = SchemaType.ARRAY,
//                    implementation = PermissionAccessControlResponseDto.class)))
//    @APIResponse(
//            responseCode = "401",
//            description = "Client has not been authenticated.",
//            content = @Content(schema = @Schema(
//                    type = SchemaType.OBJECT,
//                    implementation = InformativeResponse.class)))
//    @APIResponse(
//            responseCode = "403",
//            description = "The authenticated client is not permitted to perform the requested operation.",
//            content = @Content(schema = @Schema(
//                    type = SchemaType.OBJECT,
//                    implementation = InformativeResponse.class)))
//    @APIResponse(
//            responseCode = "500",
//            description = "Internal Server Errors.",
//            content = @Content(schema = @Schema(
//                    type = SchemaType.OBJECT,
//                    implementation = InformativeResponse.class)))
//    @SecurityRequirement(name = "Authentication")
//
//    @GET
//    @Path("/acl")
//    @Produces(value = MediaType.APPLICATION_JSON)
//    @AccessPermission(collection = Collection.Installation, operation = ACL)
//    public Response getAllAccessControl(){
//
//        var response = accessControlService.fetchAllPermissions(installationRepository);
//
//        return Response.ok().entity(response).build();
//    }

    @Tag(name = "Metric")
    @org.eclipse.microprofile.openapi.annotations.Operation(
            summary = "Assigns a new Metric to a specific Installation.",
            description = "Fundamentally, this operation creates a new Metric and assigns it to a specific Installation. " +
                    "Metric is assigned to the given Installation but belongs to the hierarchical structure Project -> Provider -> Installation.")
    @APIResponse(
            responseCode = "201",
            description = "Metric has been successfully created and assigned.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = MetricResponseDto.class)))
    @APIResponse(
            responseCode = "400",
            description = "Bad Request.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "401",
            description = "Client has not been authenticated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "404",
            description = "Metric Definition has not been found.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "415",
            description = "Cannot consume content type.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Errors.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @SecurityRequirement(name = "Authentication")

    @POST
    @Path("/{installationId}/metrics")
    @Produces(value = MediaType.APPLICATION_JSON)
    @Consumes(value = MediaType.APPLICATION_JSON)
    public Response save(
            @Parameter(
                    description = "The Installation to which Metric will be assigned.",
                    required = true,
                    example = "507f1f77bcf86cd799439011",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("installationId")
            @Valid
            @AccessInstallation(collection = Collection.Metric, operation = CREATE) String installationId,
            @Valid @NotNull(message = "The request body is empty.") MetricRequestDto metricRequestDto, @Context UriInfo uriInfo) {

        var serverInfo = new ResteasyUriInfo(serverUrl.concat(basePath).concat(uriInfo.getPath()), basePath);

        var response = installationService.assignMetric(installationId, metricRequestDto);

        return Response.created(serverInfo.getAbsolutePathBuilder().path(response.id).build()).entity(response).build();
    }

    @Tag(name = "Metric")
    @org.eclipse.microprofile.openapi.annotations.Operation(
            summary = "Deletes an existing Metric.",
            description = "Deletes an existing Metric.")
    @APIResponse(
            responseCode = "200",
            description = "Metric has been deleted successfully.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "401",
            description = "Client has not been authenticated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "404",
            description = "Metric has not been found.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Errors.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @SecurityRequirement(name = "Authentication")

    @DELETE
    @Path("/{installationId}/metrics/{id}")
    @Produces(value = MediaType.APPLICATION_JSON)
    public Response delete(
            @Parameter(
                    description = "The Installation to which the Metric belongs.",
                    required = true,
                    example = "507f1f77bcf86cd799439011",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("installationId")
            @Valid
            @AccessInstallation(collection = Collection.Metric, operation = Operation.DELETE) String installationId,
            @Parameter(
                    description = "The Metric to be deleted.",
                    required = true,
                    example = "507f1f77bcf86cd799439011",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("id") @Valid @NotFoundEntity(repository = MetricRepository.class, message = "There is no Metric with the following id:") String id) {


        boolean success = metricService.delete(id);

        var successResponse = new InformativeResponse();

        if (success) {
            successResponse.code = 200;
            successResponse.message = "Metric has been deleted successfully.";
        } else {
            successResponse.code = 500;
            successResponse.message = "Metric cannot be deleted due to a server issue. Please try again.";
        }
        return Response.ok().entity(successResponse).build();
    }

    @Tag(name = "Metric")
    @org.eclipse.microprofile.openapi.annotations.Operation(
            summary = "Updates an existing Metric.",
            description = "In order to update the resource properties, the body of the request must contain an updated representation of Metric. " +
                    "You can update a part or all attributes of the Metric except for metric_id. The empty or null values are ignored.")
    @APIResponse(
            responseCode = "200",
            description = "Metric was updated successfully.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = MetricResponseDto.class)))
    @APIResponse(
            responseCode = "400",
            description = "Bad Request.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "401",
            description = "Client has not been authenticated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "404",
            description = "Metric/Metric Definition has not been found.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "415",
            description = "Cannot consume content type.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Errors.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @SecurityRequirement(name = "Authentication")

    @PATCH
    @Path("/{installationId}/metrics/{id}")
    @Produces(value = MediaType.APPLICATION_JSON)
    @Consumes(value = MediaType.APPLICATION_JSON)
    public Response update(
            @Parameter(
                    description = "The Installation to which the Metric belongs.",
                    required = true,
                    example = "507f1f77bcf86cd799439011",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("installationId")
            @Valid
            @AccessInstallation(collection = Collection.Metric, operation = Operation.UPDATE) String installationId,
            @Parameter(
                    description = "The Metric to be updated.",
                    required = true,
                    example = "61dbe3f10086512c9ff1197a",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("id") @Valid @NotFoundEntity(repository = MetricRepository.class, message = "There is no Metric with the following id:") String id, @Valid @NotNull(message = "The request body is empty.") UpdateMetricRequestDto updateMetricRequestDto) {

        MetricResponseDto response = metricService.update(id, updateMetricRequestDto);
        return Response.ok().entity(response).build();
    }

    @Tag(name = "Metric")
    @org.eclipse.microprofile.openapi.annotations.Operation(
            summary = "Returns an existing Metric.",
            description = "This operation accepts the id of a Metric and fetches from the database the corresponding record.")
    @APIResponse(
            responseCode = "200",
            description = "The corresponding Metric.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = MetricResponseDto.class)))
    @APIResponse(
            responseCode = "401",
            description = "Client has not been authenticated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "404",
            description = "Metric has not been found.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Errors.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @SecurityRequirement(name = "Authentication")

    @GET
    @Path("/{installationId}/metrics/{id}")
    @Produces(value = MediaType.APPLICATION_JSON)
    public Response getMetric(
            @Parameter(
                    description = "The Installation to which the Metric belongs.",
                    required = true,
                    example = "507f1f77bcf86cd799439011",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("installationId")
            @Valid
            @AccessInstallation(collection = Collection.Metric, operation = READ) String installationId,
            @Parameter(
                    description = "The Metric to be retrieved.",
                    required = true,
                    example = "507f1f77bcf86cd799439011",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("id") @Valid @NotFoundEntity(repository = MetricRepository.class, message = "There is no Metric with the following id:") String id) {

        MetricResponseDto response = metricService.fetchMetric(id);

        return Response.ok().entity(response).build();
    }

    @Tag(name = "Metric")
    @org.eclipse.microprofile.openapi.annotations.Operation(
            summary = "Returns all Metrics under a specific Installation.",
            description = "This operation is responsible for fetching all Metrics under a specific Installation. " +
                    "By passing the Project and Provider IDs to which the Installation belongs as well as the Installation ID, you can retrieve all Metrics that have been assigned to this specific Installation. By default, the first page of 10 Metrics will be returned. " +
                    "You can tune the default values by using the query parameters page and size.")
    @APIResponse(
            responseCode = "200",
            description = "All Metrics.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = PageableMetricProjection.class)))
    @APIResponse(
            responseCode = "401",
            description = "Client has not been authenticated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "404",
            description = "Not found.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Errors.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @SecurityRequirement(name = "Authentication")

    @GET
    @Path("/{installationId}/metrics")
    @Produces(value = MediaType.APPLICATION_JSON)
    public Response getAllMetricsUnderAnInstallation(
            @Parameter(
                    description = "Τhe Installation id with which you can request all the Metrics that have been assigned to it.",
                    required = true,
                    example = "grnet",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("installationId") @Valid @AccessInstallation(collection = Collection.Metric, operation = READ) String installationId,
            @Parameter(name = "page", in = QUERY,
                    description = "Indicates the page number. Page number must be >= 1.") @DefaultValue("1") @QueryParam("page") int page,
            @Parameter(name = "size", in = QUERY,
                    description = "The page size.") @DefaultValue("10") @QueryParam("size") int size,
            @Context UriInfo uriInfo) {

        if (page < 1) {
            throw new BadRequestException("Page number must be >= 1.");
        }

        var response = installationService.fetchAllMetrics(installationId, page - 1, size, uriInfo);

        return Response.ok().entity(response).build();
    }


    @Tag(name = "Search")
    @org.eclipse.microprofile.openapi.annotations.Operation(
            operationId = "search-project",
            summary = "Searches a project",
            description = "Search Project")

    @APIResponse(
            responseCode = "200",
            description = "The corresponding Projects.",
            content = @Content(schema = @Schema(
                    type = SchemaType.STRING,
                    implementation = ProjectResponseDto.class)))
    @APIResponse(
            responseCode = "400",
            description = "Bad Request.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "401",
            description = "Client has not been authenticated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "403",
            description = "The authenticated client is not permitted to perform the requested operation.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "415",
            description = "Cannot consume content type.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Errors.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @SecurityRequirement(name = "Authentication")

    @POST
    @Path("/search")
    @Produces(value = MediaType.APPLICATION_JSON)
    @Consumes(value = MediaType.APPLICATION_JSON)

    public Response search(
            @NotEmpty(message = "The request body is empty.") @RequestBody(content = @Content(
                    schema = @Schema(implementation = String.class),
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = {
                            @ExampleObject(
                                    name = "An example request of a search on installations",
                                    value = "{\n" +
                                            "            \"type\":\"query\",\n" +
                                            "            \"field\": \"installation\",\n" +
                                            "            \"values\": \"GRNET-KNS-3\",\n" +
                                            "            \"operand\": \"eq\"          \n" +
                                            "\n" +
                                            "}",
                                    summary = "A simple search on Installations "),
                            @ExampleObject(
                                    name = "An example request of a complex search on installations",
                                    value = "\n" +
                                            "{\n" +
                                            "  \"type\": \"filter\",\n" +
                                            "  \"operator\": \"OR\",\n" +
                                            "  \"criteria\": [\n" +
                                            " \n" +
                                            "    {\n" +
                                            "      \"type\": \"filter\",\n" +
                                            "      \"operator\": \"OR\",\n" +
                                            "      \"criteria\": [{\n" +
                                            "            \"type\":\"query\",\n" +
                                            "            \"field\": \"installation\",\n" +
                                            "            \"values\": \"GRNET-KNS-3\",\n" +
                                            "            \"operand\": \"eq\"          \n" +
                                            "\n" +
                                            "},{\n" +
                                            "            \"type\":\"query\",\n" +
                                            "            \"field\": \"organisation\",\n" +
                                            "            \"values\": \"grnet\",\n" +
                                            "            \"operand\": \"eq\"          \n" +
                                            "\n" +
                                            "}]\n" +
                                            "    }]}",
                                    summary = "A complex search on Installations ")})
            ) String json,
            @Parameter(name = "page", in = QUERY,
                    description = "Indicates the page number. Page number must be >= 1.") @DefaultValue("1") @QueryParam("page") int page,
            @Parameter(name = "size", in = QUERY,
                    description = "The page size.") @DefaultValue("10") @QueryParam("size") int size,
            @Context UriInfo uriInfo
    ) throws ParseException, NoSuchFieldException, org.json.simple.parser.ParseException, JsonProcessingException {

        if (page < 1) {
            throw new BadRequestException("Page number must be >= 1.");
        }
        if (json.equals("")) {
            throw new BadRequestException("not empty body permitted");
        }
        var results = installationService.searchInstallation(json, page - 1, size, uriInfo);


        return Response.ok().entity(results).build();


//        var  response =results.content.stream().map(ProjectResponseDto::getId).collect(Collectors.toList()).stream().map(installationService::hierarchicalStructure).collect(Collectors.toList());
//
//
//        PageResource<Project, List<HierarchicalRelationProjection>> pageResource=new PageResource<>();
//        pageResource.content=response;
//        pageResource.sizeOfPage=results.sizeOfPage;
//        pageResource.numberOfPage=results.numberOfPage;
//        pageResource.totalPages=results.totalPages;
//        pageResource.links=results.links;
//        pageResource.totalElements=results.totalElements;
//

    }


    public static class PageableMetricProjection extends PageResource<MetricProjection> {

        private List<MetricProjection> content;

        @Override
        public List<MetricProjection> getContent() {
            return content;
        }

        @Override
        public void setContent(List<MetricProjection> content) {
            this.content = content;
        }

    }
}