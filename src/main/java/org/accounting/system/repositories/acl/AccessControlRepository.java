package org.accounting.system.repositories.acl;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import org.accounting.system.entities.acl.AccessControl;
import org.accounting.system.entities.acl.RoleAccessControl;
import org.accounting.system.enums.Collection;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * This repository {@link AccessControlRepository} encapsulates the logic required to access
 * {@link AccessControl} data stored in the mongo database. More specifically, it encapsulates the queries
 * that can be performed on the {@link AccessControl} collection. It is also responsible for mapping
 * the data from the storage format to the {@link AccessControl}.
 *
 */
@ApplicationScoped
public class AccessControlRepository implements PanacheMongoRepository<RoleAccessControl> {

//    /**
//     * Returns a specific Collection entity to which a client may has {permission} access.
//     *
//     * @param who The one to whom the permission may be granted
//     * @param collection The name of the Collection
//     * @param entity The entity id
//     * @param permission Access control permission
//     * @return |The Access Control that may grant access to a client in a particular entity
//     */
//    public Optional<T> findByWhoAndCollectionAndEntityAndPermission(String who, Collection collection, String entity, AccessControlPermission permission){
//
//        return find("who = ?1 and collection = ?2 and entity = ?3 and permissions in ?4", who, collection, entity, permission).stream().findAny();
//    }

//    /**
//     * Returns all Collection entities to which a client has {permission} access
//     * @param who The one to whom the permissions have been granted
//     * @param collection The name of the Collection
//     * @param permission Access control permission
//     * @return The available access controls that grant access to a client in a Collection
//     */
//    public List<T> findAllByWhoAndCollection(String who, Collection collection, AccessControlPermission permission){
//
//        return list("who = ?1 and collection = ?2 and permissions in ?3", who, collection, permission);
//    }

    /**
     * Returns a specific Collection entity to which a client may has access.
     *
     * @param who the one to whom the permission may be granted
     * @param collection The name of the Collection
     * @param entity The entity id
     * @return the Access Control that may grant access to a client in a particular entity
     */
    public Optional<RoleAccessControl> findByWhoAndCollectionAndEntity(String who, Collection collection, String entity){

        var optional =  find("who = ?1 and collection = ?2 and entity = ?3", who, collection, entity).stream().findAny();

        return optional;
   }

    /**
     * Executes a query to retrieve all Access Controls that have been created for the given entity id.
     *
     * @param id The entity for which permissions will be returned.
     * @param collection The collection that the entity belongs to.
     */
    public PanacheQuery<RoleAccessControl> findAllByEntityAndCollection(String id, Collection collection, int page, int size){

        return find("entity = ?1 and collection = ?2", id, collection).page(Page.of(page, size));
    }


    /**
     * Returns a specific Collection entity to which a client may has access.
     *
     * @param who the one to whom the permission may be granted
     * @param collection The name of the Collection
     * @return a list of Access Controls that may grant access to a client in a particular entity
     */
    public List<RoleAccessControl> findByWhoAndCollection(String who, Collection collection) {

        return find("who = ?1 and collection = ?2 ", who, collection).list();
    }

    public void accessListOfProjects(Set<String> projects, String clientId){

        projects.stream().forEach(project->{

            try {
                RoleAccessControl accessControl = new RoleAccessControl();
                accessControl.setRoles(Set.of("project_admin"));
                accessControl.setCollection(Collection.Project);
                accessControl.setEntity(project);
                accessControl.setWho(clientId);

                persist(accessControl);
            } catch (Exception e){
                //acl already exists, so try to insert other projects
            }
        });
    }

//    /**
//     * Returns all Access Controls that have been created for given collection
//     * @param collection The name of the Collection
//     * @return The available access controls that grant access to a client in a Collection
//     */
//    public List<T> findAllByCollection(Collection collection){
//
//        return list("collection = ?1" , collection);
//    }

//    /**
//     * Returns all Access Controls that have been created for given collection and created by given creatorId
//     * @param collection The name of the Collection
//     * @param creatorId  The creator id
//     * @return The available access controls that grant access to a client in a Collection
//     */
//    public List<T> findAllByCollectionAndCreatorId(Collection collection, String creatorId){
//
//        return list("collection = ?1 and creatorId = ?2" , collection, creatorId);
//    }


}