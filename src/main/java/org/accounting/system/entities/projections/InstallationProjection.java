package org.accounting.system.entities.projections;

import lombok.EqualsAndHashCode;
import org.accounting.system.entities.MetricDefinition;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.util.List;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class InstallationProjection {

    private ObjectId id;

    private String project;

    private String organisation;

    @EqualsAndHashCode.Include
    private String infrastructure;

    @EqualsAndHashCode.Include
    private String installation;

    private MetricDefinition metricDefinition;

    @BsonProperty("unit_of_access")
    private List<MetricDefinition> metricDefinitions;

    public ObjectId getId() {
        return id;
    }
    public String convertIdToStr(){
        return  id.toString();
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getOrganisation() {
        return organisation;
    }

    public void setOrganisation(String organisation) {
        this.organisation = organisation;
    }

    public String getInfrastructure() {
        return infrastructure;
    }

    public void setInfrastructure(String infrastructure) {
        this.infrastructure = infrastructure;
    }

    public String getInstallation() {
        return installation;
    }

    public void setInstallation(String installation) {
        this.installation = installation;
    }

    public List<MetricDefinition> getMetricDefinitions() {
        return metricDefinitions;
    }

    public void setMetricDefinitions(List<MetricDefinition> metricDefinitions) {
        this.metricDefinitions = metricDefinitions;
    }

    public MetricDefinition getMetricDefinition() {
        return metricDefinitions.get(0);
    }

    public void setMetricDefinition(MetricDefinition metricDefinition) {
        this.metricDefinition = metricDefinition;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }
}
