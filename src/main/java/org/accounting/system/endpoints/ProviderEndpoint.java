package org.accounting.system.endpoints;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.quarkus.security.Authenticated;
import org.accounting.system.constraints.NotFoundEntity;
import org.accounting.system.dtos.InformativeResponse;
import org.accounting.system.dtos.pagination.PageResource;
import org.accounting.system.dtos.provider.ProviderRequestDto;
import org.accounting.system.dtos.provider.ProviderResponseDto;
import org.accounting.system.dtos.provider.UpdateProviderRequestDto;
import org.accounting.system.enums.Collection;
import org.accounting.system.enums.Operation;
import org.accounting.system.interceptors.annotations.AccessPermission;
import org.accounting.system.repositories.provider.ProviderRepository;
import org.accounting.system.services.ProviderService;
import org.accounting.system.services.acl.AccessControlService;
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
import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;

import java.text.ParseException;

import java.text.ParseException;

import static org.eclipse.microprofile.openapi.annotations.enums.ParameterIn.QUERY;

@Path("/providers")
@Authenticated
@SecurityScheme(securitySchemeName = "Authentication",
        description = "JWT token",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER)

public class ProviderEndpoint {

    @ConfigProperty(name = "quarkus.resteasy.path")
    String basePath;

    @ConfigProperty(name = "server.url")
    String serverUrl;

    @Inject
    ProviderService providerService;

    @Inject
    ProviderRepository providerRepository;

    @Inject
    AccessControlService accessControlService;


    @Tag(name = "Provider")
    @org.eclipse.microprofile.openapi.annotations.Operation(
            operationId = "providers-from-eosc-portal",
            summary = "Get a list of all Providers in the Accounting System.",
            description = "Essentially, this operation returns all Providers available on the EOSC-Portal as well as all Providers registered through the Accounting System API. " +
                    "By default, the first page of 10 Providers will be returned. You can tune the default values by using " +
                    "the query parameters page and size.")
    @APIResponse(
            responseCode = "200",
            description = "Success operation.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = PageableProviderResponseDto.class)))
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
            description = "Not Found.",
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
    @Produces(value = MediaType.APPLICATION_JSON)
    @AccessPermission(collection = Collection.Provider, operation = Operation.READ)
    public Response get(@Parameter(name = "page", in = QUERY,
            description = "Indicates the page number. Page number must be >= 1.") @DefaultValue("1") @QueryParam("page") int page,
                        @Parameter(name = "size", in = QUERY,
                                description = "The page size.") @DefaultValue("10") @QueryParam("size") int size,
                        @Context UriInfo uriInfo) {

        if (page < 1) {
            throw new BadRequestException("Page number must be >= 1.");
        }

        var serverInfo = new ResteasyUriInfo(serverUrl.concat(basePath).concat(uriInfo.getPath()), basePath);

        var providers = providerService.findAllProvidersPageable(page - 1, size, serverInfo);

        return Response.ok().entity(providers).build();
    }

    @Tag(name = "Provider")
    @org.eclipse.microprofile.openapi.annotations.Operation(
            operationId = "register-a-new-provider",
            summary = "Registers a new Provider.",
            description = "In addition to Providers from [EOSC-Portal](#/Provider/providers-from-eosc-portal), a client can use this functionality to create a new Provider. " +
                    "Obviously this Provider will only be registered in the Accounting System API.")
    @APIResponse(
            responseCode = "201",
            description = "Provider has been created successfully.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = ProviderResponseDto.class)))
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
    @Produces(value = MediaType.APPLICATION_JSON)
    @Consumes(value = MediaType.APPLICATION_JSON)
    @AccessPermission(collection = Collection.Provider, operation = Operation.CREATE)
    public Response save(@Valid @NotNull(message = "The request body is empty.") ProviderRequestDto providerRequestDto, @Context UriInfo uriInfo) {

        var serverInfo = new ResteasyUriInfo(serverUrl.concat(basePath).concat(uriInfo.getPath()), basePath);

        providerService.existById(providerRequestDto.id);
        providerService.existByName(providerRequestDto.name);

        var response = providerService.save(providerRequestDto);

        return Response.created(serverInfo.getAbsolutePathBuilder().path(response.id).build()).entity(response).build();
    }

    @Tag(name = "Provider")
    @org.eclipse.microprofile.openapi.annotations.Operation(
            summary = "Deletes an existing Provider.",
            description = "This operation deletes an existing Provider registered through the [Accounting System API](#/Provider/register-a-new-provider). " +
                    "Deleting Providers which derive from the [EOSC-Portal](#/Provider/providers-from-eosc-portal) is not allowed.")
    @APIResponse(
            responseCode = "200",
            description = "Provider has been deleted successfully.",
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
            description = "Provider has not been found.",
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
    @AccessPermission(collection = Collection.Provider, operation = Operation.DELETE)
    public Response delete(@Parameter(
            description = "The Provider to be deleted.",
            required = true,
            example = "sites",
            schema = @Schema(type = SchemaType.STRING))
                           @PathParam("id") @Valid @NotFoundEntity(repository = ProviderRepository.class, id = String.class, message = "There is no Provider with the following id:") String id) {

        var success = providerService.delete(id);

        var successResponse = new InformativeResponse();

        if (success) {
            successResponse.code = 200;
            successResponse.message = "Provider has been deleted successfully.";
        } else {
            successResponse.code = 500;
            successResponse.message = "Provider cannot be deleted due to a server issue. Please try again.";
        }
        return Response.ok().entity(successResponse).build();
    }

    @Tag(name = "Provider")
    @org.eclipse.microprofile.openapi.annotations.Operation(
            summary = "Updates an existing Provider.",
            description = "This operation updates an existing Provider registered through the [Accounting System API](#/Provider/register-a-new-provider). " +
                    "Updating Providers which derive from the [EOSC-Portal](#/Provider/providers-from-eosc-portal) is not allowed. Finally, " +
                    "you can update a part or all attributes of Provider. The empty or null values are ignored.")
    @APIResponse(
            responseCode = "200",
            description = "Provider was updated successfully.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = ProviderResponseDto.class)))
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
            description = "Provider has not been found.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "409",
            description = "The Provider already exists.",
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
    @AccessPermission(collection = Collection.Provider, operation = Operation.UPDATE)
    public Response update(
            @Parameter(
                    description = "The Provider to be updated.",
                    required = true,
                    example = "sites",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("id") @Valid @NotFoundEntity(repository = ProviderRepository.class, id = String.class, message = "There is no Provider with the following id:") String id, @Valid @NotNull(message = "The request body is empty.") UpdateProviderRequestDto updateProviderRequestDto) {

        var response = providerService.update(id, updateProviderRequestDto);

        return Response.ok().entity(response).build();
    }

    @Tag(name = "Provider")
    @org.eclipse.microprofile.openapi.annotations.Operation(
            summary = "Returns an existing Provider.",
            description = "This operation accepts the id of a Provider and fetches from the database the corresponding record.")
    @APIResponse(
            responseCode = "200",
            description = "The corresponding Provider.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = ProviderResponseDto.class)))
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
            description = "Provider has not been found.",
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
    @AccessPermission(collection = Collection.Provider, operation = Operation.READ)
    public Response get(
            @Parameter(
                    description = "The Provider to be retrieved.",
                    required = true,
                    example = "sites",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("id") @Valid @NotFoundEntity(repository = ProviderRepository.class, id = String.class, message = "There is no Provider with the following id:") String id) {

        var response = providerService.fetchProvider(id);

        return Response.ok().entity(response).build();
    }

    @Tag(name = "Search")
    @org.eclipse.microprofile.openapi.annotations.Operation(
            summary = "Search",
            description = "Search")
    @APIResponse(
            responseCode = "200",
            description = "The corresponding Providers.",
            content = @Content(schema = @Schema(
                    type = SchemaType.ARRAY,
                    implementation = ProviderResponseDto.class)))
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

    @POST
    @Path("/search")
    @Produces(value = MediaType.APPLICATION_JSON)
    @Consumes(value = MediaType.APPLICATION_JSON)

    public Response search(
            @Valid @NotNull(message = "The request body is empty.") @RequestBody(content = @Content(
                    schema = @Schema(implementation = String.class),
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = {
                            @ExampleObject(
                                    name = "An example request of a search on providers",
                                    value = "",
                                    summary = "A complex search on Providers ")})
            ) String json, @Parameter(name = "page", in = QUERY,
            description = "Indicates the page number. Page number must be >= 1.") @DefaultValue("1") @QueryParam("page") int page,
            @Parameter(name = "size", in = QUERY,
                    description = "The page size.") @DefaultValue("10") @QueryParam("size") int size, @Context UriInfo uriInfo) throws ParseException, NoSuchFieldException, org.json.simple.parser.ParseException, JsonProcessingException {
        if (page < 1) {
            throw new BadRequestException("Page number must be >= 1.");
        }
        var response = providerService.searchProvider(json, page - 1, size, uriInfo);

        return Response.ok().entity(response).build();
    }


//    @Tag(name = "Provider")
//    @org.eclipse.microprofile.openapi.annotations.Operation(
//            summary = "Returns all Access Control entries that have been created for Provider collection.",
//            description = "Returns all Access Control entries that have been created for Provider collection.")
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
//    @AccessPermission(collection = Collection.Provider, operation = ACL)
//    public Response getAllAccessControl(){
//
//        var response = accessControlService.fetchAllPermissions(providerRepository);
//
//        return Response.ok().entity(response).build();
//    }



public static class PageableProviderResponseDto extends PageResource<ProviderResponseDto> {

    private List<ProviderResponseDto> content;

    @Override
    public List<ProviderResponseDto> getContent() {
        return content;
    }

    @Override
    public void setContent(List<ProviderResponseDto> content) {
        this.content = content;
    }

}


}