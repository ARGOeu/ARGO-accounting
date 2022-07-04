package org.accounting.system.repositories.installation;

import com.mongodb.client.model.Collation;
import com.mongodb.client.model.CollationStrength;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import org.accounting.system.entities.installation.Installation;
import org.accounting.system.entities.projections.HierarchicalRelationProjection;
import org.accounting.system.exceptions.ConflictException;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

/**
 * {@link InstallationRepository This repository} encapsulates the logic required to access
 * {@link Installation} data stored in the mongo database. More specifically, it encapsulates the queries
 * that can be performed on the {@link Installation} collection. It is also responsible for mapping
 * the data from the storage format to the {@link Installation}.
 *
 * Since {@link InstallationRepository this repository} extends {@link InstallationModulator},
 * it has access to all queries, which determine the degree of accessibility of the data.
 *
 * Also, all the operations that are defined on {@link PanacheMongoRepository} are available on this repository.
 * In this repository, we essentially define the queries that will be executed on the database without any restrictions.
 */
@ApplicationScoped
public class InstallationRepository extends InstallationModulator {

    public void exist(String infrastructure, String installation){

        var optional = find("infrastructure = ?1 and installation = ?2", infrastructure, installation)
                .withCollation(Collation.builder().locale("en")
                        .collationStrength(CollationStrength.SECONDARY).build())
                .stream()
                .findAny();

        optional.ifPresent(storedInstallation -> {throw new ConflictException("There is an Installation with infrastructure "+infrastructure+" and installation "+installation+". Its id is "+storedInstallation.getId().toString());});
    }

    public List<HierarchicalRelationProjection> hierarchicalStructure(final String externalId) {

        return hierarchicalRelationRepository.hierarchicalStructure(externalId);
//        switch (getRequestInformation().getAccessType()){
//            case ALWAYS:
//                return projectAccessAlwaysRepository.hierarchicalStructure(externalId);
//            case ENTITY:
//                return projectAccessEntityRepository.hierarchicalStructure(externalId);
//            default:
//                throw new ForbiddenException("The authenticated client is not permitted to perform the requested operation.");
//        }
    }
}
