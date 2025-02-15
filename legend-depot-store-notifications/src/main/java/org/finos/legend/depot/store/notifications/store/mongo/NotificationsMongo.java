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

package org.finos.legend.depot.store.notifications.store.mongo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.finos.legend.depot.domain.api.status.MetadataEventStatus;
import org.finos.legend.depot.store.mongo.BaseMongo;
import org.finos.legend.depot.store.notifications.api.Notifications;
import org.finos.legend.depot.store.notifications.domain.MetadataNotification;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;


public class NotificationsMongo extends BaseMongo<MetadataNotification> implements Notifications
{
    public static final String EVENTS = "notifications";

    private static final String EVENT_ID = "eventId";
    private static final String LAST_UPDATED = "lastUpdated";
    private static final String PARENT_EVENT = "parentEventId";
    private static final String RESPONSE_STATUS = "status";

    @Inject
    public NotificationsMongo(@Named("mongoDatabase") MongoDatabase databaseProvider)
    {
        super(databaseProvider, MetadataNotification.class, new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_EMPTY));
    }

    @Override
    protected MongoCollection getCollection()
    {
        return getMongoCollection(EVENTS);
    }


    @Override
    protected boolean createIndexIfAbsent(String indexName, String... fieldNames)
    {
        createIndexIfAbsent("parentId",PARENT_EVENT);
        createIndexIfAbsent("status",RESPONSE_STATUS);
        createIndexIfAbsent("groupId-artifactId-versionId", GROUP_ID, ARTIFACT_ID, VERSION_ID);
        return createIndexIfAbsent("eventId", EVENT_ID);
    }

    @Override
    protected Bson getKeyFilter(MetadataNotification data)
    {
        return NotificationKeyFilter.getFilter(data);
    }

    @Override
    public List<MetadataNotification> getAll()
    {
        return super.getAllStoredEntities();
    }

    @Override
    protected void validateNewData(MetadataNotification data)
    {
        //no specific validation
    }

    public Optional<MetadataNotification> get(String eventId)
    {
        return findOne(Filters.eq(EVENT_ID, eventId));
    }


    @Override
    public List<MetadataNotification> find(String groupId, String artifactId, String version, String parentEventId, Boolean success, LocalDateTime fromDate, LocalDateTime toDate)
    {
        MongoCollection<Document> events = getCollection();
        LocalDateTime from = LocalDateTime.now().minusMinutes(30);
        LocalDateTime to = LocalDateTime.now();
        if (fromDate != null)
        {
            from = fromDate;
        }
        if (toDate != null)
        {
            to = toDate;
        }

        Bson filter = Filters.and(Filters.gte(LAST_UPDATED, toTime(from)), Filters.lte(LAST_UPDATED, toTime(to)));
        filter = groupId != null ? and(filter, eq(GROUP_ID, groupId)) : filter;
        filter = artifactId != null ? and(filter, eq(ARTIFACT_ID, artifactId)) : filter;
        filter = version != null ? and(filter, eq(VERSION_ID, version)) : filter;
        filter = parentEventId != null ? and(filter, eq(PARENT_EVENT, parentEventId)) : filter;
        filter = success != null ? and(filter, eq(RESPONSE_STATUS, (success ? MetadataEventStatus.SUCCESS.name() : MetadataEventStatus.FAILED.name()))) : filter;

        List<MetadataNotification> result = new ArrayList<>();
        events.find(filter).sort(Sorts.descending(LAST_UPDATED)).forEach((Consumer<Document>)doc -> result.add(convert(doc, MetadataNotification.class)));
        return result;
    }

    private long toTime(LocalDateTime date)
    {
        return Date.from(date.atZone(ZoneId.systemDefault())
                .toInstant()).getTime();
    }

    public void insert(MetadataNotification event)
    {
        createOrUpdate(event);
    }

    public void complete(MetadataNotification event)
    {
        createOrUpdate(event.setLastUpdated(new Date()));
    }

}
