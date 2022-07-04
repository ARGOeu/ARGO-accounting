package org.accounting.system.repositories.installation;

import org.accounting.system.dtos.installation.InstallationRequestDto;
import org.accounting.system.entities.acl.RoleAccessControl;
import org.accounting.system.entities.installation.Installation;
import org.accounting.system.entities.projections.HierarchicalRelationProjection;
import org.accounting.system.entities.projections.MetricProjection;
import org.accounting.system.entities.projections.ProjectionQuery;
import org.accounting.system.enums.Collection;
import org.accounting.system.enums.Operation;
import org.accounting.system.repositories.HierarchicalRelationRepository;
import org.accounting.system.repositories.modulators.AbstractModulator;
import org.bson.types.ObjectId;

import javax.inject.Inject;
import java.util.List;


public class InstallationModulator extends AbstractModulator<Installation, ObjectId, RoleAccessControl> {


    @Inject
    InstallationAccessEntityRepository installationAccessEntityRepository;

    @Inject
    InstallationAccessAlwaysRepository installationAccessAlwaysRepository;
    @Inject
    HierarchicalRelationRepository hierarchicalRelationRepository;
    public List<HierarchicalRelationProjection> hierarchicalStructure(final String externalId) {

        return hierarchicalRelationRepository.hierarchicalStructure(externalId);
    }

    public Installation save(InstallationRequestDto request) {

        return installationAccessAlwaysRepository.save(request);

//
//        switch (getRequestInformation().getAccessType()){
//            case ALWAYS:
//                return installationAccessAlwaysRepository.save(request);
//            case ENTITY:
//                return installationAccessEntityRepository.save(request);
//            default:
//                throw new ForbiddenException("The authenticated client is not permitted to perform the requested operation.");
//        }
    }

    public boolean accessibility(String project, String provider, String installation, Collection collection, Operation operation){

        return installationAccessEntityRepository.accessibility(project, provider, installation, collection, operation);
    }

    public ProjectionQuery<MetricProjection> fetchAllMetrics(String id, int page, int size){

        return installationAccessAlwaysRepository.fetchAllMetrics(id, page, size);


//        switch (getRequestInformation().getAccessType()){
//            case ALWAYS:
//                return installationAccessAlwaysRepository.fetchAllMetrics(id, page, size);
//            case ENTITY:
//                return installationAccessEntityRepository.fetchAllMetrics(id, page, size);
//            default:
//                throw new ForbiddenException("The authenticated client is not permitted to perform the requested operation.");
//        }
    }


    @Override
    public InstallationAccessAlwaysRepository always() {
        return installationAccessAlwaysRepository;
    }

    @Override
    public InstallationAccessEntityRepository entity() {
        return installationAccessEntityRepository;
    }
}
