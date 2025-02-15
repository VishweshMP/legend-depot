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

package org.finos.legend.depot.artifacts.repository.services;

import org.finos.legend.depot.artifacts.repository.api.ArtifactRepository;
import org.finos.legend.depot.artifacts.repository.api.ArtifactRepositoryException;
import org.finos.legend.depot.domain.project.ProjectData;
import org.finos.legend.depot.artifacts.repository.domain.VersionMismatch;
import org.finos.legend.depot.services.api.projects.ProjectsService;
import org.finos.legend.sdlc.domain.model.version.VersionId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestVersionsMismatch
{
    protected ArtifactRepository repository = mock(ArtifactRepository.class);
    protected ProjectsService projects = mock(ProjectsService.class);
    protected RepositoryServices repositoryServices = new RepositoryServices(repository,projects);

    @Before
    public void setup() throws ArtifactRepositoryException
    {
        ProjectData testProjectA = new ProjectData("PROD-A","examples.metadata", "test1");
        testProjectA.getVersions().add("2.2.0");
        testProjectA.getVersions().add("2.3.0");
        ProjectData testProjectB = new ProjectData("PROD-B","examples.metadata", "test2");
        testProjectB.getVersions().add("1.0.0");
        ProjectData testProjectC = new ProjectData("PROD-C","examples.metadata", "test3");
        testProjectC.getVersions().add("2.0.1");
        ProjectData testProjectD = new ProjectData("PROD-D","examples.metadata", "test4");
        testProjectD.getVersions().add("0.0.1");

        when(projects.getAll()).thenReturn(Arrays.asList(testProjectA,testProjectB,testProjectC,testProjectD));
        when(repository.findVersions("examples.metadata", "test1")).thenReturn(Arrays.asList(VersionId.parseVersionId("2.2.0"),VersionId.parseVersionId("2.3.0"), VersionId.parseVersionId("2.3.1")));
        when(repository.findVersions("examples.metadata", "test2")).thenReturn(Arrays.asList(VersionId.parseVersionId("1.0.1")));
        when(repository.findVersions("examples.metadata", "test3")).thenReturn(Collections.emptyList());
        when(repository.findVersions("examples.metadata", "test4")).thenReturn(Arrays.asList(VersionId.parseVersionId("0.0.1")));
    }

    @Test
    public void getVersionsMismatch()
    {

        List<VersionMismatch> counts = repositoryServices.findVersionsMismatches();
        Assert.assertNotNull(counts);
        Assert.assertEquals(3, counts.size());
        Assert.assertEquals(1, counts.stream().filter(p -> p.projectId.equals("PROD-A")).count());
        Assert.assertEquals(1, counts.stream().filter(p -> p.projectId.equals("PROD-B")).count());
        Assert.assertEquals(1, counts.stream().filter(p -> p.projectId.equals("PROD-C")).count());

        VersionMismatch prodA = counts.stream().filter(p -> p.projectId.equals("PROD-A")).findFirst().get();
        Assert.assertEquals(1, prodA.versionsNotInStore.size());
        Assert.assertEquals("2.3.1", prodA.versionsNotInStore.get(0));
        VersionMismatch prodB = counts.stream().filter(p -> p.projectId.equals("PROD-B")).findFirst().get();
        Assert.assertEquals("1.0.1", prodB.versionsNotInStore.get(0));
        Assert.assertEquals("1.0.0", prodB.versionsNotInRepository.get(0));
        VersionMismatch prodC = counts.stream().filter(p -> p.projectId.equals("PROD-C")).findFirst().get();
        Assert.assertEquals("2.0.1", prodC.versionsNotInRepository.get(0));


    }

    @Test
    public void getVersionsMismatchWithExceptions() throws ArtifactRepositoryException
    {
        when(repository.findVersions("examples.metadata", "test4")).thenThrow(new ArtifactRepositoryException("not found"));

        List<VersionMismatch> counts = repositoryServices.findVersionsMismatches();
        Assert.assertNotNull(counts);
        Assert.assertEquals(4, counts.size());
        Assert.assertEquals(1, counts.stream().filter(p -> p.projectId.equals("PROD-A")).count());
        Assert.assertEquals(1, counts.stream().filter(p -> p.projectId.equals("PROD-B")).count());
        Assert.assertEquals(1, counts.stream().filter(p -> p.projectId.equals("PROD-C")).count());
        Assert.assertEquals(1, counts.stream().filter(p -> p.projectId.equals("PROD-D")).count());

        VersionMismatch prodD = counts.stream().filter(p -> p.projectId.equals("PROD-D")).findFirst().get();
        Assert.assertFalse(prodD.errors.isEmpty());



    }
}
