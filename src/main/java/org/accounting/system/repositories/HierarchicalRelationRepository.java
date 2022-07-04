package org.accounting.system.repositories;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Field;
import com.mongodb.client.model.Filters;
import io.quarkus.mongodb.panache.PanacheMongoRepositoryBase;
import liquibase.repackaged.org.apache.commons.collections4.CollectionUtils;
import org.accounting.system.entities.HierarchicalRelation;
import org.accounting.system.entities.projections.HierarchicalRelationProjection;
import org.accounting.system.entities.projections.InstallationProjection;
import org.accounting.system.entities.projections.MetricProjection;
import org.accounting.system.entities.projections.ProjectionQuery;
import org.accounting.system.repositories.installation.InstallationRepository;
import org.accounting.system.repositories.metric.MetricRepository;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@ApplicationScoped
public class HierarchicalRelationRepository implements PanacheMongoRepositoryBase<HierarchicalRelation, String> {

    @Inject
    MongoClient mongoClient;

    @Inject
    MetricRepository metricRepository;

    @Inject
    InstallationRepository installationRepository;

    public void save(HierarchicalRelation hierarchicalRelation, ObjectId metric){

        var toBeStored = findByPath(hierarchicalRelation.id);

        if(Objects.isNull(toBeStored)){

            CollectionUtils.addIgnoreNull(hierarchicalRelation.metrics, metric);
            persist(hierarchicalRelation);
        } else {
            CollectionUtils.addIgnoreNull(toBeStored.metrics, metric);
            persistOrUpdate(toBeStored);
        }
    }

    /**
     * This method executes a query to check if the given hierarchical relationship exists
     *
     * @param path Hierarchical relationship path
     * @return if the given path exists.
     */
    public boolean exist(String path){

        Bson regex = Aggregates.match(Filters.regex("_id","\\b" + path + "\\b"+"(?![-])"));

        Document count = getMongoCollection()
                .aggregate(List
                        .of(regex, Aggregates.count())).first();

        return count != null && Long.parseLong(count.get("count").toString()) > 0L;
    }


    public HierarchicalRelation findByPath(final String id){

        var document = findById(id);

        if( document == null ) {
            return document;
        }

        return build(document, find(Document.parse(Filters.regex("_id", id + "[.\\s]").toBsonDocument().toJson())).list());
    }

    public ProjectionQuery<InstallationProjection> findInstallationsByProjectId(String project, String from, String localField, String foreignField, String as, int page, int size, Class<InstallationProjection> projection){

        Bson eq = Aggregates.match(Filters.regex("project", project));

        Bson lookup = Aggregates.lookup(from, localField, foreignField, as);


        List<InstallationProjection> projections = installationRepository
                .getMongoCollection()
                .aggregate(List
                        .of(lookup, eq,  Aggregates.skip(size * (page)), Aggregates.limit(size)), InstallationProjection.class).into(new ArrayList<>());

        var projectionQuery = new ProjectionQuery<InstallationProjection>();

        Document count = installationRepository
                .getMongoCollection()
                .aggregate(List
                        .of(eq, Aggregates.count())).first();

        projectionQuery.list = projections;
        projectionQuery.index = page;
        projectionQuery.size = size;
        projectionQuery.count = count == null ? 0L : Long.parseLong(count.get("count").toString());

        return projectionQuery;
    }

    public ProjectionQuery<InstallationProjection> findInstallationsByProject(String project, String from, String localField, String foreignField, String as, int page, int size, Class<InstallationProjection> projection){

        Bson eq = Aggregates.match(Filters.eq("project", project));

        Bson lookup = Aggregates.lookup(from, localField, foreignField, as);


        List<InstallationProjection> projections = installationRepository
                .getMongoCollection()
                .aggregate(List
                        .of(lookup, eq,  Aggregates.skip(size * (page)), Aggregates.limit(size)), InstallationProjection.class).into(new ArrayList<>());

        var projectionQuery = new ProjectionQuery<InstallationProjection>();

        Document count = installationRepository
                .getMongoCollection()
                .aggregate(List
                        .of(eq, Aggregates.count())).first();

        projectionQuery.list = projections;
        projectionQuery.index = page;
        projectionQuery.size = size;
        projectionQuery.count = count == null ? 0L : Long.parseLong(count.get("count").toString());

        return projectionQuery;
    }


    public List<InstallationProjection>  findInstallationsOfProjects(List<String> projects, String from, String localField, String foreignField, String as){

        Bson eq = Aggregates.match(Filters.in("project", projects));

        Bson lookup = Aggregates.lookup(from, localField, foreignField, as);


     return  installationRepository
                .getMongoCollection()
                .aggregate(List
                        .of(lookup, eq), InstallationProjection.class).into(new ArrayList<>());

    }

    public List<InstallationProjection> findInstallationsOfProviders(List<String>providers, String from, String localField, String foreignField, String as){

        Bson eq = Aggregates.match(Filters.in("organisation", providers));

        Bson lookup = Aggregates.lookup(from, localField, foreignField, as);


        return installationRepository
                .getMongoCollection()
                .aggregate(List
                        .of(lookup, eq), InstallationProjection.class).into(new ArrayList<>());
    }

    public ProjectionQuery<InstallationProjection> findInstallationsByProvider(String project, String provider, String from, String localField, String foreignField, String as, int page, int size, Class<InstallationProjection> projection){

        Bson eq = Aggregates.match(Filters.and(Filters.eq("project", project), Filters.eq("organisation", provider)));

        Bson lookup = Aggregates.lookup(from, localField, foreignField, as);


        List<InstallationProjection> projections = installationRepository
                .getMongoCollection()
                .aggregate(List
                        .of(lookup, eq,  Aggregates.skip(size * (page)), Aggregates.limit(size)), InstallationProjection.class).into(new ArrayList<>());

        var projectionQuery = new ProjectionQuery<InstallationProjection>();

        Document count = installationRepository
                .getMongoCollection()
                .aggregate(List
                        .of(eq, Aggregates.count())).first();

        projectionQuery.list = projections;
        projectionQuery.index = page;
        projectionQuery.size = size;
        projectionQuery.count = count == null ? 0L : Long.parseLong(count.get("count").toString());

        return projectionQuery;
    }


    public List<InstallationProjection> findInstallationsByProvider(String project, String provider, String from, String localField, String foreignField, String as){

        Bson eq = Aggregates.match(Filters.and(Filters.eq("project", project), Filters.eq("organisation", provider)));

        Bson lookup = Aggregates.lookup(from, localField, foreignField, as);


        return installationRepository
                .getMongoCollection()
                .aggregate(List
                        .of(lookup, eq), InstallationProjection.class).into(new ArrayList<>());


    }

    public ProjectionQuery<MetricProjection> findByExternalId(final String externalId, int page, int size) {
        //Bson regex = Aggregates.match(Filters.regex("resource_id", externalId + "[.\\s]"));
        Bson regex = Aggregates.match(Filters.regex("resource_id", "\\b" + externalId + "\\b"+"(?![-])"));

        List<MetricProjection> projections = metricRepository
                .getMongoCollection()
                .aggregate(List
                        .of(regex,  Aggregates.skip(size * (page)),Aggregates.limit(size)), MetricProjection.class).into(new ArrayList<>());

        Document count = metricRepository
                .getMongoCollection()
                .aggregate(List
                        .of(regex, Aggregates.count())).first();

        var projectionQuery = new ProjectionQuery<MetricProjection>();

        projectionQuery.list = projections;
        projectionQuery.index = page;
        projectionQuery.size = size;
        projectionQuery.count = count == null ? 0L : Long.parseLong(count.get("count").toString());

        return projectionQuery;
    }

//    public ProjectionQuery<MetricProjection> findByExternalId(final String externalId, int page, int size) {
//        //Bson regex = Aggregates.match(Filters.regex("resource_id", externalId + "[.\\s]"));
//        //Bson regex = Aggregates.match(Filters.regex("resource_id", externalId));
//
//        List<AccessControl> accessControlList = accessControlRepository.findAllByWhoAndCollection(requestInformation.getSubjectOfToken(), org.accounting.system.enums.Collection.Metric, AccessControlPermission.READ);
//
//        List<String> entities = accessControlList
//                .stream()
//                .map(AccessControl::getEntity)
//                .collect(Collectors.toList());
//
//
//        Bson match = Filters.and(Filters.regex("resource_id", externalId), Filters.or(Filters.eq("creatorId", requestInformation.getSubjectOfToken()), Filters.in("_id", entities)));
//
//        List<MetricProjection> projections = metricRepository
//                .getMongoCollection()
//                .aggregate(List
//                        .of(match,  Aggregates.skip(size * (page)) , Aggregates.limit(size)), MetricProjection.class).into(new ArrayList<>());
//
//        Document count = metricRepository
//                .getMongoCollection()
//                .aggregate(List
//                        .of(match, Aggregates.count())).first();
//
//        var projectionQuery = new ProjectionQuery<MetricProjection>();
//
//        projectionQuery.list = projections;
//        projectionQuery.index = page;
//        projectionQuery.size = size;
//        projectionQuery.count = count == null ? 0L : Long.parseLong(count.get("count").toString());
//
//        return projectionQuery;
//    }

//    public List<HierarchicalRelationProjection> findByExternalId(final String externalId) {
//
//        Document externalIdToInstallation = Document.parse("{\n" +
//                "        $cond: [\n" +
//                "          {\n" +
//                "            $eq: [\n" +
//                "              \"$relationType\",\n" +
//                "              \"INSTALLATION\"\n" +
//                "            ]\n" +
//                "          },\n" +
//                "          {\n" +
//                "            \"$toObjectId\": \"$externalId\"\n" +
//                "          },\n" +
//                "          \"$$REMOVE\"\n" +
//                "        ]\n" +
//                "      }");
//
//        Document externalIdToProject = Document.parse("{\n" +
//                "        $cond: [\n" +
//                "          {\n" +
//                "            $eq: [\n" +
//                "              \"$relationType\",\n" +
//                "              \"PROJECT\"\n" +
//                "            ]\n" +
//                "          },\n" +
//                "          \"$externalId\",\n" +
//                "          \"$$REMOVE\"\n" +
//                "        ]\n" +
//                "      }");
//
//        Document externalIdToProvider = Document.parse("{\n" +
//                "        $cond: [\n" +
//                "          {\n" +
//                "            $eq: [\n" +
//                "              \"$relationType\",\n" +
//                "              \"PROVIDER\"\n" +
//                "            ]\n" +
//                "          },\n" +
//                "          \"$externalId\",\n" +
//                "          \"$$REMOVE\"\n" +
//                "        ]\n" +
//                "      }");
//
//        Document removeInstallationFunction = Document.parse("{\n" +
//                "        $cond: [\n" +
//                "          {\n" +
//                "            $eq: [\n" +
//                "              {\n" +
//                "                $size: \"$installation\"\n" +
//                "              },\n" +
//                "              0\n" +
//                "            ]\n" +
//                "          },\n" +
//                "          \"$$REMOVE\",\n" +
//                "          \"$installation\"\n" +
//                "        ]\n" +
//                "      }");
//
//
//        Field installation = new Field<>("installation", externalIdToInstallation);
//        Field provider = new Field<>("provider", externalIdToProvider);
//        Field project = new Field<>("project", externalIdToProject);
//
//        Field remove = new Field<>("installation", removeInstallationFunction);
//
//        Bson fields = Aggregates.addFields(installation, provider, project);
//
//        Bson lookupMetric = Aggregates.lookup("Metric", "metrics", "_id", "full_metrics");
//
//        Bson lookupInstallation = Aggregates.lookup("Installation", "installation", "_id", "installation");
//
//        Bson regex = Aggregates.match(Filters.regex("_id", externalId + "[.\\s]"));
//
//        Bson documentsByExternalId = Aggregates.match(Filters.eq("_id", externalId));
//
//        final var documents = getMongoCollection().aggregate(List.of(fields, lookupMetric, lookupInstallation, documentsByExternalId, Aggregates.addFields(remove)), HierarchicalRelationProjection.class).into(new ArrayList<>());
//
//        if( documents == null ) {
//            return documents;
//        }
//
//        return documents.
//                stream()
//                .map(document->build(document, getMongoCollection().aggregate(List.of(fields, lookupMetric, lookupInstallation, regex, Aggregates.addFields(remove)), HierarchicalRelationProjection.class).into(new ArrayList<>())))
//                .collect(Collectors.toList());
//    }

    public List<HierarchicalRelationProjection> hierarchicalStructure(final String externalId) {

        Document externalIdToInstallation = Document.parse("{\n" +
                "        $cond: [\n" +
                "          {\n" +
                "            $eq: [\n" +
                "              \"$relationType\",\n" +
                "              \"INSTALLATION\"\n" +
                "            ]\n" +
                "          },\n" +
                "          {\n" +
                "            \"$toObjectId\": \"$externalId\"\n" +
                "          },\n" +
                "          \"$$REMOVE\"\n" +
                "        ]\n" +
                "      }");

        Document externalIdToProject = Document.parse("{\n" +
                "        $cond: [\n" +
                "          {\n" +
                "            $eq: [\n" +
                "              \"$relationType\",\n" +
                "              \"PROJECT\"\n" +
                "            ]\n" +
                "          },\n" +
                "          \"$externalId\",\n" +
                "          \"$$REMOVE\"\n" +
                "        ]\n" +
                "      }");

        Document externalIdToProvider = Document.parse("{\n" +
                "        $cond: [\n" +
                "          {\n" +
                "            $eq: [\n" +
                "              \"$relationType\",\n" +
                "              \"PROVIDER\"\n" +
                "            ]\n" +
                "          },\n" +
                "          \"$externalId\",\n" +
                "          \"$$REMOVE\"\n" +
                "        ]\n" +
                "      }");

        Document removeInstallationFunction = Document.parse("{\n" +
                "        $cond: [\n" +
                "          {\n" +
                "            $eq: [\n" +
                "              {\n" +
                "                $size: \"$installation\"\n" +
                "              },\n" +
                "              0\n" +
                "            ]\n" +
                "          },\n" +
                "          \"$$REMOVE\",\n" +
                "          \"$installation\"\n" +
                "        ]\n" +
                "      }");


        Field installation = new Field<>("installation", externalIdToInstallation);
        Field provider = new Field<>("provider", externalIdToProvider);
        Field project = new Field<>("project", externalIdToProject);

        Field remove = new Field<>("installation", removeInstallationFunction);

        Bson fields = Aggregates.addFields(installation, provider, project);

        Bson lookupInstallation = Aggregates.lookup("Installation", "installation", "_id", "installation");
        Bson lookupProject = Aggregates.lookup("Project", "project", "_id", "project");
        Bson lookupProvider = Aggregates.lookup("Provider", "provider", "_id", "provider");


        //Bson regex = Aggregates.match(Filters.regex("_id", externalId + "[.\\s]"));

        Bson regex = Aggregates.match(Filters.regex("_id", externalId + "[.\\s]"));

        Bson documentsByExternalId = Aggregates.match(Filters.eq("_id", externalId));

        final var documents = getMongoCollection().aggregate(List.of(fields, lookupInstallation, lookupProject, documentsByExternalId, Aggregates.addFields(remove)), HierarchicalRelationProjection.class).into(new ArrayList<>());

        if( documents == null ) {
            return documents;
        }

        return documents.
                stream()
                .map(document->build(document, getMongoCollection().aggregate(List.of(fields, lookupInstallation, lookupProvider, regex, Aggregates.addFields(remove)), HierarchicalRelationProjection.class).into(new ArrayList<>())))
                .collect(Collectors.toList());
    }

    private HierarchicalRelationProjection build(final HierarchicalRelationProjection root, final Collection<HierarchicalRelationProjection> documents ) {
        final Map<String, HierarchicalRelationProjection> map = new HashMap<>();

        for(final HierarchicalRelationProjection document: documents) {
            map.put(document.id, document);
        }

        for(final HierarchicalRelationProjection document: documents) {
            map.put(document.id, document);

            final String path = document.id
                    .substring(0, document.id.lastIndexOf(HierarchicalRelation.PATH_SEPARATOR));

            if(path.equals(root.id)){
                root.hierarchicalRelations.add(document);
            } else{
                final HierarchicalRelationProjection parent = map.get(path);
                if(parent != null ) {
                    parent.hierarchicalRelations.add(document);
                }
            }
        }

        return root;
    }

    private HierarchicalRelation build(final HierarchicalRelation root, final Collection<HierarchicalRelation> documents ) {
        final Map<String, HierarchicalRelation> map = new HashMap<>();

        for(final HierarchicalRelation document: documents) {
            map.put(document.id, document);
        }

        for(final HierarchicalRelation document: documents) {
            map.put(document.id, document);

            final String path = document.id
                    .substring(0, document.id.lastIndexOf(HierarchicalRelation.PATH_SEPARATOR));

            if(path.equals(root.id)){
                root.hierarchicalRelations.add(document);
            } else{
                final HierarchicalRelation parent = map.get(path);
                if(parent != null ) {
                    parent.hierarchicalRelations.add(document);
                }
            }
        }

        return root;
    }

    public MongoCollection<Document> getMongoCollection(){
        return mongoClient.getDatabase("accounting-system").getCollection("HierarchicalRelation");
    }


}
