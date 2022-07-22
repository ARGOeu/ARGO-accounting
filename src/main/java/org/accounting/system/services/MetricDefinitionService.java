package org.accounting.system.services;

import com.mongodb.MongoWriteException;
import io.quarkus.mongodb.panache.PanacheQuery;
import org.accounting.system.dtos.metric.MetricResponseDto;
import org.accounting.system.dtos.metricdefinition.MetricDefinitionRequestDto;
import org.accounting.system.dtos.metricdefinition.MetricDefinitionResponseDto;
import org.accounting.system.dtos.metricdefinition.UpdateMetricDefinitionRequestDto;
import org.accounting.system.dtos.pagination.PageResource;
import org.accounting.system.endpoints.MetricDefinitionEndpoint;
import org.accounting.system.entities.Metric;
import org.accounting.system.entities.MetricDefinition;
import org.accounting.system.exceptions.ConflictException;
import org.accounting.system.mappers.MetricDefinitionMapper;
import org.accounting.system.mappers.MetricMapper;
import org.accounting.system.repositories.metric.MetricRepository;
import org.accounting.system.repositories.metricdefinition.MetricDefinitionRepository;
import org.accounting.system.util.QueryParser;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.json.simple.parser.ParseException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This service exposes business logic, which uses the {@link MetricDefinitionRepository}.
 * It is used to keep logic to a minimum in {@link MetricDefinitionEndpoint} and
 * {@link MetricDefinitionRepository}
 */
@ApplicationScoped
public class MetricDefinitionService {

    @Inject
    MetricDefinitionRepository metricDefinitionRepository;

    @Inject
    MetricService metricService;

    @Inject
    MetricRepository metricRepository;

    @Inject
    QueryParser queryParser;

    public MetricDefinitionService(MetricDefinitionRepository metricDefinitionRepository, MetricService metricService, MetricRepository metricRepository) {
        this.metricDefinitionRepository = metricDefinitionRepository;
        this.metricService = metricService;
        this.metricRepository = metricRepository;
    }

    /**
     * Maps the {@link MetricDefinitionRequestDto} to {@link MetricDefinition}.
     * Then the {@link MetricDefinition} is stored in the mongo database.
     *
     * @param request The POST request body.
     * @return The stored metric definition has been turned into a response body.
     */
    public MetricDefinitionResponseDto save(MetricDefinitionRequestDto request) {

        var metricDefinition = MetricDefinitionMapper.INSTANCE.requestToMetricDefinition(request);

        metricDefinitionRepository.save(metricDefinition);

        return MetricDefinitionMapper.INSTANCE.metricDefinitionToResponse(metricDefinition);
    }

    /**
     * Fetches a Metric Definition by given id.
     *
     * @param id The Metric Definition id.
     * @return The corresponding Metric Definition.
     */
    public MetricDefinitionResponseDto fetchMetricDefinition(String id){

        var metricDefinition = metricDefinitionRepository.fetchEntityById(new ObjectId(id));

        return MetricDefinitionMapper.INSTANCE.metricDefinitionToResponse(metricDefinition);
    }

    public List<MetricDefinitionResponseDto> fetchAllMetricDefinitions(){

        var list = metricDefinitionRepository.getAllEntities();

        return MetricDefinitionMapper.INSTANCE.metricDefinitionsToResponse(list);
    }



    /**
     * Returns the N Metric Definitions from the given page.
     *
     * @param page Indicates the page number.
     * @param size The number of Metric Definitions to be retrieved.
     * @param uriInfo The current uri.
     * @return An object represents the paginated results.
     */
    public PageResource<MetricDefinitionResponseDto> findAllMetricDefinitionsPageable(int page, int size, UriInfo uriInfo){

        PanacheQuery<MetricDefinition> panacheQuery = metricDefinitionRepository.findAllPageable(page, size);

        return new PageResource<>(panacheQuery, MetricDefinitionMapper.INSTANCE.metricDefinitionsToResponse(panacheQuery.list()), uriInfo);
    }

    /**
     * This method calls the {@link MetricDefinitionRepository metricDefinitionRepository} to update a Metric Definition.
     *
     * @param id The Metric Definition to be updated.
     * @param request The Metric Definition attributes to be updated.
     * @return The updated Metric Definition.
     * @throws ConflictException If the Metric Definition with the given combination of unit_type and metric_name exists
     */
    public MetricDefinitionResponseDto update(String id, UpdateMetricDefinitionRequestDto request){

        MetricDefinition metricDefinition = null;

        try{
            metricDefinition = metricDefinitionRepository.updateEntity(new ObjectId(id), request);
        } catch (MongoWriteException e){
            throw new ConflictException("The combination of unit_type and metric_name should be unique. A Metric Definition with that combination has already been created.");
        }

        return MetricDefinitionMapper.INSTANCE.metricDefinitionToResponse(metricDefinition);
    }

    /**
     * Delete a Metric Definition by given id.
     * @param metricDefinitionId The Metric Definition to be deleted.
     * @return If the operation is successful or not.
     */
    public boolean delete(String metricDefinitionId){

        return metricDefinitionRepository.deleteEntityById(new ObjectId(metricDefinitionId));
    }

    /**
     *Τwo Metric Definitions are considered similar when having the same unit type and name.
     *
     * @param unitType Unit Type of the Metric.
     * @param name The name of the Metric.
     * @throws ConflictException If Metric Definition already exists.
     */
    public void exist(String unitType, String name){

        metricDefinitionRepository.exist(unitType, name);
    }

    /**
     * Checks if there is any Metric assigned to the Metric Definition.
     *
     * @param id The Metric Definition id.
     * @throws ConflictException If the Metric Definition has children.
     */
    public void hasChildren(String id){

        if(metricService.countMetricsByMetricDefinitionId(id) > 0){
            throw new ConflictException("The Metric Definition cannot be deleted. There is a Metric assigned to it.");
        }
    }

    /**
     * Returns the N Metrics, which have been assigned to the Metric Definition, from the given page.
     *
     * @param metricDefinitionId The Metric Definition id.
     * @param page Indicates the page number.
     * @param size The number of Metrics to be retrieved.
     * @param uriInfo The current uri.
     * @return An object represents the paginated results.
     */
    public PageResource<MetricResponseDto>  findMetricsByMetricDefinitionIdPageable(String metricDefinitionId, int page, int size, UriInfo uriInfo){

        metricDefinitionRepository.fetchEntityById(new ObjectId(metricDefinitionId));

        PanacheQuery<Metric> panacheQuery = metricRepository.findMetricsByMetricDefinitionIdPageable(metricDefinitionId, page, size);

        return new PageResource<>(panacheQuery, MetricMapper.INSTANCE.metricsToResponse(panacheQuery.list()), uriInfo);
    }
    public PageResource<MetricDefinitionResponseDto> searchMetricDefinition( String json, boolean isAlwaysPermission, int page, int size,UriInfo uriInfo) throws ParseException, NoSuchFieldException {

        List<String> entityIds=new ArrayList<>();
        if(!isAlwaysPermission){
            entityIds= fetchAllMetricDefinitions().stream().map(MetricDefinitionResponseDto::getId).collect(Collectors.toList());
        }
        Bson query=queryParser.parseFile(json, isAlwaysPermission, entityIds, MetricDefinition.class);
        PanacheQuery<MetricDefinition> projectionQuery = metricDefinitionRepository.search(query,page,size);
        return new PageResource<>(projectionQuery, MetricDefinitionMapper.INSTANCE.metricDefinitionsToResponse(projectionQuery.list()), uriInfo);

    }


}