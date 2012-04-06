package org.platformlayer.ops.maven;

public class MavenReference {
    public String groupId;
    public String artifactId;
    public String versionId;
    public String classifier;

    @Override
    public String toString() {
        return "MavenReference [groupId=" + groupId + ", artifactId=" + artifactId + ", versionId=" + versionId + ", classifier=" + classifier + "]";
    }

}
