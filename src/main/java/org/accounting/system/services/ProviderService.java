package org.accounting.system.services;

import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.oidc.TokenIntrospection;
import org.accounting.system.dtos.installation.InstallationResponseDto;
import org.accounting.system.dtos.pagination.PageResource;
import org.accounting.system.dtos.provider.ProviderRequestDto;
import org.accounting.system.dtos.provider.ProviderResponseDto;
import org.accounting.system.dtos.provider.UpdateProviderRequestDto;
import org.accounting.system.entities.HierarchicalRelation;
import org.accounting.system.entities.projections.InstallationProjection;
import org.accounting.system.entities.projections.MetricProjection;
import org.accounting.system.entities.projections.ProjectionQuery;
import org.accounting.system.entities.provider.Provider;
import org.accounting.system.exceptions.ConflictException;
import org.accounting.system.mappers.InstallationMapper;
import org.accounting.system.mappers.ProviderMapper;
import org.accounting.system.repositories.HierarchicalRelationRepository;
import org.accounting.system.repositories.provider.ProviderRepository;
import org.accounting.system.services.authorization.RoleService;
import org.accounting.system.util.QueryParser;
import org.apache.commons.lang3.StringUtils;
import org.bson.conversions.Bson;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProviderService {

    @Inject
    ProviderRepository providerRepository;

    @Inject
    HierarchicalRelationRepository hierarchicalRelationRepository;

    @Inject
    HierarchicalRelationService hierarchicalRelationService;

    @Inject
    TokenIntrospection tokenIntrospection;
    @Inject
    RoleService roleService;
    @Inject
    QueryParser queryParser;
    @ConfigProperty(name = "key.to.retrieve.id.from.access.token")
    String id;
    /**
     * Returns the N Providers from the given page.
     *
     * @param page Indicates the page number.
     * @param size The number of Providers to be retrieved.
     * @param uriInfo The current uri.
     * @return An object represents the paginated results.
     */
    public PageResource<ProviderResponseDto> findAllProvidersPageable(int page, int size, UriInfo uriInfo){

        PanacheQuery<Provider> panacheQuery = providerRepository.findAllPageable(page, size);

        return new PageResource<>(panacheQuery, ProviderMapper.INSTANCE.providersToResponse(panacheQuery.list()), uriInfo);
    }

    /**
     * Maps the {@link ProviderRequestDto} to {@link Provider}.
     * Then the {@link Provider} is stored in the mongo database.
     *
     * @param request The POST request body.
     * @return The stored Provider has been turned into a response body.
     */
    public ProviderResponseDto save(ProviderRequestDto request) {

        var provider = ProviderMapper.INSTANCE.requestToProvider(request);

        providerRepository.save(provider);

        return ProviderMapper.INSTANCE.providerToResponse(provider);
    }

    /**
     * Checks if a Provider with given id exists.
     *
     * @param id The Provider id.
     * @throws ConflictException If Provider already exists.
     */
    public void existById(String id){

        providerRepository.findByIdOptional(id)
                .ifPresent(provider -> {throw new ConflictException("There is a Provider with id: "+id);});
    }

    /**
     * Checks if a Provider with given name exists.
     *
     * @param name The Provider name.
     * @throws ConflictException If Provider already exists.
     */
    public void existByName(String name){

        providerRepository.findByName(name)
                .ifPresent(provider -> {throw new ConflictException("There is a Provider with name: "+name);});
    }

    /**
     * Delete a Provider by given id.
     * @param providerId The Provider to be deleted.
     * @return If the operation is successful or not.
     * @throws ForbiddenException If the Providers derives from EOSC-Portal.
     */
    public boolean delete(String providerId){

        var provider = providerRepository.findById(providerId);

        // if Provider's creator id is null then it derives from EOSC-Portal
        if(Objects.isNull(provider.getCreatorId())){
            throw new ForbiddenException("You cannot delete a Provider which derives from EOSC-Portal.");
        }

        if(hierarchicalRelationService.providerBelongsToAnyProject(providerId)){
            throw new ForbiddenException("You cannot delete a Provider which belongs to a Project.");
        }

        return providerRepository.deleteEntityById(providerId);
    }

    /**
     * This method is responsible for checking if there is a Provider with given name or id.
     * Then, it calls the {@link ProviderRepository providerRepository} to update a Provider.
     *
     * @param id The Provider to be updated.
     * @param request The Provider attributes to be updated.
     * @return The updated Provider.
     * @throws ConflictException If Provider with the given name or id already exists.
     */
    public ProviderResponseDto update(String id, UpdateProviderRequestDto request){

        if(StringUtils.isNotEmpty(request.id)){
            existById(request.id);
        }

        if(StringUtils.isNotEmpty(request.name)){
            existByName(request.name);
        }

        var provider = providerRepository.updateEntity(id, request);

        return ProviderMapper.INSTANCE.providerToResponse(provider);
    }

    /**
     * Fetches a Provider by given id.
     *
     * @param id The Provider id.
     * @return The corresponding Provider.
     */
    public ProviderResponseDto fetchProvider(String id){

        var provider = providerRepository.fetchEntityById(id);

        return ProviderMapper.INSTANCE.providerToResponse(provider);
    }

    /**
     * This method check whether the given Provider derived from EOSC-Portal or not.
     *
     * @param id The Provider ID.
     * @throws ForbiddenException If provider derives from EOSC-Portal
     */
    public void derivesFromEoscPortal(String id){

        Provider entity = providerRepository.findById(id);

        // if Provider's creator id is null then it derives from EOSC-Portal
        if(Objects.isNull(entity.getCreatorId())){
            throw new ForbiddenException("You cannot access a Provider which derives from EOSC-Portal.");
        }
    }

    public PageResource<MetricProjection> fetchAllMetrics(String projectId, String providerId, int page, int size, UriInfo uriInfo){

        var projection = providerRepository.fetchAllMetrics(projectId + HierarchicalRelation.PATH_SEPARATOR + providerId, page, size);

        if(projection.count == 0){
            throw new NotFoundException("No metrics added.");
        }

        return new PageResource<>(projection, projection.list, uriInfo);
    }
    public PageResource<InstallationResponseDto> findInstallationsByProvider(String projectId, String providerId, int page, int size, UriInfo uriInfo){

        ProjectionQuery<InstallationProjection> projectionQuery = hierarchicalRelationRepository.findInstallationsByProvider(projectId, providerId, "MetricDefinition", "unit_of_access", "_id", "unit_of_access", page, size, InstallationProjection.class);

        return new PageResource<>(projectionQuery, InstallationMapper.INSTANCE.installationProjectionsToResponse(projectionQuery.list), uriInfo);
    }
    public  PageResource< ProviderResponseDto> searchProvider(String json, int page, int size, UriInfo uriInfo) throws  NoSuchFieldException, org.json.simple.parser.ParseException {

        Bson query=queryParser.parseFile(json);
        PanacheQuery< Provider> projectionQuery = providerRepository.search(query,page,size);
        return new PageResource<>(projectionQuery, ProviderMapper.INSTANCE.providersToResponse(projectionQuery.list()), uriInfo);
    }

}
