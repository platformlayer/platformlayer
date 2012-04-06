package org.platformlayer.federation;

import org.platformlayer.ids.FederationKey;
import org.platformlayer.ids.ProjectId;

public class FederationMapping {
    final FederationKey host;
    final ProjectId project;

    public FederationMapping(FederationKey host, ProjectId project) {
        super();
        this.host = host;
        this.project = project;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((host == null) ? 0 : host.hashCode());
        result = prime * result + ((project == null) ? 0 : project.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FederationMapping other = (FederationMapping) obj;
        if (host == null) {
            if (other.host != null)
                return false;
        } else if (!host.equals(other.host))
            return false;
        if (project == null) {
            if (other.project != null)
                return false;
        } else if (!project.equals(other.project))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "FederationMapping [host=" + host + ", project=" + project + "]";
    }

}