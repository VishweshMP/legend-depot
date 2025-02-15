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

package org.finos.legend.depot.store.artifacts.services;

import org.finos.legend.depot.domain.EntityValidator;
import org.finos.legend.depot.domain.api.MetadataEventResponse;
import org.finos.legend.depot.domain.project.ProjectData;
import org.finos.legend.depot.domain.version.VersionValidator;
import org.finos.legend.depot.services.api.projects.ManageProjectsService;
import org.finos.legend.depot.store.artifacts.api.ArtifactsRefreshService;
import org.finos.legend.depot.store.notifications.api.NotificationEventHandler;
import org.finos.legend.depot.store.notifications.domain.MetadataNotification;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.finos.legend.depot.domain.version.VersionValidator.MASTER_SNAPSHOT;

public class ArtifactRefreshEventHandler implements NotificationEventHandler
{
    private final ManageProjectsService projects;
    private final ArtifactsRefreshService artifactsRefreshService;

    @Inject
    public ArtifactRefreshEventHandler(ManageProjectsService projects, ArtifactsRefreshService artifactsRefreshService)
    {
        this.projects = projects;
        this.artifactsRefreshService = artifactsRefreshService;
    }

    @Override
    public MetadataEventResponse handleEvent(MetadataNotification event)
    {
        MetadataEventResponse response = new MetadataEventResponse();
        Optional<ProjectData> existingProject = projects.find(event.getGroupId(), event.getArtifactId());
        if (!existingProject.isPresent())
        {
            ProjectData newProject = new ProjectData(event.getProjectId(), event.getGroupId(), event.getArtifactId());
            projects.createOrUpdate(newProject);
            response.addMessage(String.format("New project %s created %s-%s", newProject.getProjectId(), newProject.getGroupId(), newProject.getArtifactId()));
        }
        return response.combine(artifactsRefreshService.refreshVersionForProject(event.getGroupId(), event.getArtifactId(), event.getVersionId(), event.isFullUpdate(),event.isTransitive(),event.getParentEventId()));
    }

    @Override
    public List<String> validateEvent(MetadataNotification event)
    {
        List<String> errors = new ArrayList<>();

        if (!EntityValidator.isValidGroupId(event.getGroupId()) || !EntityValidator.isValidArtifactId(event.getArtifactId()))
        {
            errors.add(String.format("invalid groupId %s or artifactId %s",event.getGroupId(),event.getArtifactId()));
        }
        if (!MASTER_SNAPSHOT.equals(event.getVersionId()) && !VersionValidator.isValid(event.getVersionId()))
        {
            errors.add(String.format("invalid versionId %s ",event.getVersionId()));
        }
        return errors;
    }

}
