package org.accounting.system.services;

import io.quarkus.mongodb.panache.PanacheQuery;
import org.accounting.system.dtos.MetricDefinitionRequestDto;
import org.accounting.system.dtos.MetricDefinitionResponseDto;
import org.accounting.system.dtos.PageResource;
import org.accounting.system.dtos.UpdateMetricDefinitionRequestDto;
import org.accounting.system.entities.Metric;
import org.accounting.system.entities.MetricDefinition;
import org.accounting.system.exceptions.ConflictException;
import org.accounting.system.mappers.MetricDefinitionMapper;
import org.accounting.system.repositories.MetricDefinitionRepository;
import org.accounting.system.repositories.MetricRepository;
import org.bson.types.ObjectId;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Optional;

/**
 * This service exposes business logic, which uses the {@link MetricDefinitionRepository}.
 * It is used to keep logic to a minimum in {@link org.accounting.system.endpoints.MetricDefinitionEndpoint} and
 * {@link MetricDefinitionRepository}
 */
@ApplicationScoped
public class MetricDefinitionService {

    @Inject
    private MetricDefinitionRepository metricDefinitionRepository;

    @Inject
    private MetricService metricService;

    @Inject
    private MetricRepository metricRepository;


    public MetricDefinitionService(MetricDefinitionRepository metricDefinitionRepository, MetricService metricService, MetricRepository metricRepository) {
        this.metricDefinitionRepository = metricDefinitionRepository;
        this.metricService = metricService;
        this.metricRepository = metricRepository;
    }

    /**
     * Maps the {@link MetricDefinitionRequestDto} to {@link MetricDefinition}.
     * Then the {@link MetricDefinition} is stored in the mongo database.
     *
     * @param request The POST request body
     * @return The stored metric definition has been turned into a response body
     */
    public MetricDefinitionResponseDto save(MetricDefinitionRequestDto request) {

        var metricDefinition = MetricDefinitionMapper.INSTANCE.requestToMetricDefinition(request);

        metricDefinitionRepository.persist(metricDefinition);

        return MetricDefinitionMapper.INSTANCE.metricDefinitionToResponse(metricDefinition);
    }

    public MetricDefinitionResponseDto fetchMetricDefinition(String id){

        var metricDefinition = findById(id);

        return MetricDefinitionMapper.INSTANCE.metricDefinitionToResponse(metricDefinition.get());
    }

    public List<MetricDefinitionResponseDto> fetchAllMetricDefinitions(){

        var list = metricDefinitionRepository.findAll().list();

        return MetricDefinitionMapper.INSTANCE.metricDefinitionsToResponse(list);
    }

    /**
     * This method is responsible for updating a part or all attributes of existing Metric Definition.
     *
     * @param id The Metric Definition to be updated.
     * @param request The Metric Definition attributes to be updated
     * @return The updated Metric Definition
     * @throws NotFoundException If the Metric Definition doesn't exist
     */
    public MetricDefinitionResponseDto update(String id, UpdateMetricDefinitionRequestDto request){

        var metricDefinition = findById(id).get();

        MetricDefinitionMapper.INSTANCE.updateMetricDefinitionFromDto(request, metricDefinition);

        metricDefinitionRepository.update(metricDefinition);

        return MetricDefinitionMapper.INSTANCE.metricDefinitionToResponse(metricDefinition);
    }

    /**
     * Delete a Metric Definition by given id.
     * @param metricDefinitionId The Metric Definition to be deleted
     * @return If the operation is successful or not
     * @throws NotFoundException If the Metric Definition doesn't exist
     */
    public boolean delete(String metricDefinitionId){

        return metricDefinitionRepository.deleteById(new ObjectId(metricDefinitionId));
    }

    /**
     *Τwo Metric Definitions are considered similar when having the same unit type and name.
     *
     * @param unitType Unit Type of the Metric
     * @param name The name of the Metric
     * @throws ConflictException If Metric Definition already exists
     */
    public void exist(String unitType, String name){

        metricDefinitionRepository.exist(unitType, name)
                .ifPresent(metricDefinition -> {throw new ConflictException("There is a Metric Definition with unit type "+metricDefinition.getUnitType()+" and name "+metricDefinition.getMetricName()+". Its id is "+metricDefinition.getId().toString());});
    }

    /**
     * Fetches a Metric Definition by given id.
     *
     * @param id The Metric Definition id
     * @return Optional of Metric Definition
     */
    public Optional<MetricDefinition> findById(String id){

        return metricDefinitionRepository.findByIdOptional(new ObjectId(id));
    }

    /**
     * Checks if there is any Metric assigned to the Metric Definition.
     *
     * @param id The Metric Definition id
     * @throws ConflictException If the Metric Definition has children
     */
    public void hasChildren(String id){

        if(metricService.countMetricsByMetricDefinitionId(id) > 0){
            throw new ConflictException("The Metric Definition cannot be deleted. There is a Metric assigned to it.");
        }
    }

    /**
     * Returns the N Metrics, which have been assigned to the Metric Definition, from the given page.
     *
     * @param metricDefinitionId The Metric Definition id
     * @param page Indicates the page number
     * @param size The number of Metrics to be retrieved
     * @param uriInfo The current uri
     * @return An object represents the paginated results
     */
    public PageResource<Metric>  findMetricsByMetricDefinitionIdPageable(String metricDefinitionId, int page, int size, UriInfo uriInfo){

        PanacheQuery<Metric> panacheQuery = metricRepository.findMetricsByMetricDefinitionIdPageable(metricDefinitionId, page, size);

        return new PageResource<>(panacheQuery, uriInfo);
    }
}