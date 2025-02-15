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

package org.finos.legend.depot.store.artifacts.purge.resources;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.finos.legend.depot.core.authorisation.api.AuthorisationProvider;
import org.finos.legend.depot.core.authorisation.resources.BaseAuthorisedResource;
import org.finos.legend.depot.domain.api.MetadataEventResponse;
import org.finos.legend.depot.store.artifacts.purge.api.ArtifactsPurgeService;
import org.finos.legend.depot.tracing.resources.ResourceLoggingAndTracing;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.security.Principal;


@Path("")
@Api("Artifacts Purge")
public class ArtifactsPurgeResource extends BaseAuthorisedResource
{
    public static final String ARTIFACTS_RESOURCE = "ArtifactsPurge";
    private final ArtifactsPurgeService artifactsPurgeService;

    @Inject
    public ArtifactsPurgeResource(ArtifactsPurgeService artifactsPurgeService,
                                  AuthorisationProvider authorisationProvider,
                                  @Named("requestPrincipal") Provider<Principal> principalProvider)
    {
        super(authorisationProvider, principalProvider);
        this.artifactsPurgeService = artifactsPurgeService;
    }


    @DELETE
    @Path("/artifactDelete/{groupId}/{artifactId}/versions/{versionId}")
    @ApiOperation(ResourceLoggingAndTracing.PURGE_VERSION)
    @Produces(MediaType.APPLICATION_JSON)
    public MetadataEventResponse purgeVersion(@PathParam("groupId") String groupId,
                                              @PathParam("artifactId") String artifactId,
                                              @PathParam("versionId") String versionId)
    {

        return handle(ResourceLoggingAndTracing.PURGE_VERSION, () ->
        {
            validateUser();
            artifactsPurgeService.delete(groupId, artifactId, versionId);
            return new MetadataEventResponse();
        });
    }


    @DELETE
    @Path("/artifactDelete/{groupId}/{artifactId}/old/{keepVersions}")
    @ApiOperation(ResourceLoggingAndTracing.PURGE_OLD_VERSIONS)
    @Produces(MediaType.APPLICATION_JSON)
    public MetadataEventResponse purgeOldVersions(@PathParam("groupId") String groupId,
                                              @PathParam("artifactId") String artifactId,
                                                  @PathParam("keepVersions") int versionsToKeep)
    {

        return handle(ResourceLoggingAndTracing.PURGE_OLD_VERSIONS, () ->
        {
            validateUser();
            return artifactsPurgeService.deleteOldestProjectVersions(groupId, artifactId,versionsToKeep);
        });
    }

    @Override
    protected String getResourceName()
    {
        return ARTIFACTS_RESOURCE;
    }

}
