//  Copyright 2021 Goldman Sachs
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//

package org.finos.legend.depot.domain.project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.finos.legend.depot.domain.BaseDomain;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectVersion extends BaseDomain
{
    private String versionId;

    public ProjectVersion()
    {
    }

    public ProjectVersion(String groupId, String artifactId, String versionId)
    {
        super(groupId, artifactId);
        this.versionId = versionId;
    }

    public String getVersionId()
    {
        return versionId;
    }

    @Override
    public boolean equals(Object obj)
    {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @JsonIgnore
    public String getGav()
    {
        return String.format("%s:%s:%s", this.getGroupId(), getArtifactId(), this.getVersionId());
    }
}
