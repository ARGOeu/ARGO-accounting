package org.accounting.system.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.oidc.TokenIntrospection;
import io.vavr.CheckedFunction1;
import io.vavr.control.Option;
import org.accounting.system.dtos.installation.InstallationResponseDto;
import org.accounting.system.dtos.pagination.PageResource;
import org.accounting.system.dtos.project.ProjectResponseDto;
import org.accounting.system.entities.Project;
import org.accounting.system.entities.acl.AccessControl;
import org.accounting.system.entities.projections.HierarchicalRelationProjection;
import org.accounting.system.entities.projections.InstallationProjection;
import org.accounting.system.entities.projections.MetricProjection;
import org.accounting.system.entities.projections.ProjectionQuery;
import org.accounting.system.enums.Collection;
import org.accounting.system.enums.Operation;
import org.accounting.system.mappers.InstallationMapper;
import org.accounting.system.mappers.ProjectMapper;
import org.accounting.system.repositories.HierarchicalRelationRepository;
import org.accounting.system.repositories.acl.AccessControlRepository;
import org.accounting.system.repositories.project.ProjectRepository;
import org.accounting.system.serializer.HierarchicalRelationSerializer;
import org.accounting.system.services.authorization.RoleService;
import org.accounting.system.util.QueryParser;
import org.bson.conversions.Bson;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
@ApplicationScoped
public class ProjectService {

    @Inject
    ProjectRepository projectRepository;

    @Inject
    HierarchicalRelationRepository hierarchicalRelationRepository;

    @Inject
    AccessControlRepository accessControlRepository;

    @Inject
    TokenIntrospection tokenIntrospection;

    @Inject
    RoleService roleService;
    @Inject
    QueryParser queryParser;
    @ConfigProperty(name = "key.to.retrieve.id.from.access.token")
    String id;
    @Inject
    ObjectMapper objectMapper;
    /**
     * An http call is made to Open Aire to retrieve the corresponding Project.
     * It is then stored in the database, converted into a response body and returned.
     *
     * @param id The Project id
     * @return The Project as it is stored in the Accounting system database.
     */
    public ProjectResponseDto save(String id){

        //var project = projectRepository.save(id);

        var project = projectRepository.save(id);

        return ProjectMapper.INSTANCE.projectToDto(project);
    }

    /**
     * This method correlates the given Providers with a specific Project and creates an hierarchical structure with root
     * the given Project and children the given Providers.
     *
     * @param projectId The Project id with which the Providers are going to be correlated.
     * @param providerIds List of Providers which will be correlated with a specific Provider
     * @throws NotFoundException If a Provider doesn't exist
     */
    public void associateProjectWithProviders(String projectId, Set<String> providerIds){

        projectRepository.associateProjectWithProviders(projectId, providerIds);
    }

    public PageResource<MetricProjection> fetchAllMetrics(String id, int page, int size, UriInfo uriInfo){

        var projection = projectRepository.fetchAllMetrics(id, page, size);

        if(projection.count == 0){
            throw new NotFoundException("No metrics added.");
        }

        return new PageResource<>(projection, projection.list, uriInfo);
    }

    /**
     * This method dissociated Providers from a specific Project.
     *
     * @param projectId The Project id from which the Providers are going to be dissociated
     * @param providerIds List of Providers which will be dissociated from a specific Provider
     */
    public void dissociateProviderFromProject(String projectId, Set<String> providerIds){

        projectRepository.dissociateProviderFromProject(projectId, providerIds);
    }

    public List<HierarchicalRelationProjection> hierarchicalStructure(final String externalId) {

        return projectRepository.hierarchicalStructure(externalId);
    }

    public PageResource<InstallationResponseDto> findInstallationsByProject(String projectId, int page, int size, UriInfo uriInfo){

        ProjectionQuery<InstallationProjection> projectionQuery = hierarchicalRelationRepository.findInstallationsByProject(projectId, "MetricDefinition", "unit_of_access", "_id", "unit_of_access", page, size, InstallationProjection.class);

        return new PageResource<>(projectionQuery, InstallationMapper.INSTANCE.installationProjectionsToResponse(projectionQuery.list), uriInfo);
    }
    public  PageResource<String> searchProject(String json, int page, int size, UriInfo uriInfo) throws  NoSuchFieldException, org.json.simple.parser.ParseException {

        var ids=accessControlRepository.findByWhoAndCollection(tokenIntrospection.getJsonObject().getString(id),Collection.Project).stream().filter(projects ->
                roleService.hasRoleAccess(projects.getRoles(), Collection.Project, Operation.READ)).map(projects -> projects.getEntity()).collect(Collectors.toList());
        Bson query=queryParser.parseFile(json, false, ids, Project.class);

        PanacheQuery<Project> projectionQuery = projectRepository.search(query,page,size);
        return new PageResource<>(projectionQuery, projectToJson(projectionQuery.list().stream().map(Project::getId).collect(Collectors.toList())), uriInfo);

    }

    public PageResource<String> getAll( int page, int size, UriInfo uriInfo) throws NoSuchFieldException, org.json.simple.parser.ParseException, JsonProcessingException {

        var ids= accessControlRepository.findByWhoAndCollection(tokenIntrospection.getJsonObject().getString(id),Collection.Project).stream().filter(projects ->
                roleService.hasRoleAccess(projects.getRoles(), Collection.Project, Operation.READ)).map(AccessControl::getEntity).collect(Collectors.toList());

        PanacheQuery<Project> projectionQuery = projectRepository.fetchAll(ids, page, size);

        return new PageResource<>(projectionQuery, projectToJson(projectionQuery.list().stream().map(Project::getId).collect(Collectors.toList())), uriInfo);
    }

    private List<String> projectToJson(List<String> ids) {

        SimpleModule module = new SimpleModule();

        module.addSerializer(new HierarchicalRelationSerializer(HierarchicalRelationProjection.class));

        objectMapper.registerModule(module);

        List<HierarchicalRelationProjection> projects = ids.stream()
                .map(this :: hierarchicalStructure)
                .map(list -> list.get(0))
                .collect(Collectors.toList());

        return projects
                .stream()
                .map(project-> CheckedFunction1.lift(projection -> objectMapper.writeValueAsString(projection)).apply(project))
                .map(Option::toJavaOptional)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}