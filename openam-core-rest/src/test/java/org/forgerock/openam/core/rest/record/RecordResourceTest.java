/*
* The contents of this file are subject to the terms of the Common Development and
* Distribution License (the License). You may not use this file except in compliance with the
* License.
*
* You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
* specific language governing permission and limitations under the License.
*
* When distributing Covered Software, include this CDDL Header Notice in each file and include
* the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
* Header, with the fields enclosed by brackets [] replaced by your own identifying
* information: "Portions copyright [year] [name of copyright owner]".
*
* Copyright 2015-2016 ForgeRock AS.
*/

package org.forgerock.openam.core.rest.record;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.forgerock.json.JsonValue;
import org.forgerock.json.resource.ActionRequest;
import org.forgerock.json.resource.ActionResponse;
import org.forgerock.json.resource.ResourceException;
import org.forgerock.openam.audit.context.AMExecutorServiceFactory;
import org.forgerock.openam.audit.context.AuditRequestContextPropagatingExecutorServiceFactory;
import org.forgerock.openam.test.apidescriptor.ApiAnnotationAssert;
import org.forgerock.openam.utils.IOUtils;
import org.forgerock.openam.utils.JsonValueBuilder;
import org.forgerock.services.context.Context;
import org.forgerock.util.promise.Promise;
import org.forgerock.util.thread.listener.ShutdownManager;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RecordResourceTest extends DebugTestTemplate {

    public static final String RECORD_DIRECTORY = "/record/";

    private RecordResource recordResource;
    private DebugRecorder debugRecorder;
    @Mock
    private ActionRequest request;
    @Mock
    Context serverContext;

    @BeforeMethod
    public void setup() throws Exception {
        super.setUp();
        MockitoAnnotations.initMocks(this);
        debugRecorder = new DefaultDebugRecorder(
                new AuditRequestContextPropagatingExecutorServiceFactory(Mockito.mock(ShutdownManager.class)));
        this.recordResource = new RecordResource(debugRecorder);
    }

    @Test
    public void startRecording() throws IOException, ExecutionException, InterruptedException {
        // Given...
        JsonValue jsonRecordProperties = JsonValueBuilder.toJsonValue(
                IOUtils.getFileContentFromClassPath(RecordResourceTest.class,
                        RECORD_DIRECTORY + "startSimpleRecord.json"));
        given(request.getContent()).willReturn(jsonRecordProperties);

        // When...
        Promise<ActionResponse, ResourceException> promise = recordResource.actionStart(serverContext, request);

        RecordProperties recordPropertiesInput = RecordProperties.fromJson(jsonRecordProperties);
        RecordProperties recordPropertiesOutput = RecordProperties.fromJson(promise.get().getJsonContent().get("record"));

        assertThat(recordPropertiesInput).isEqualTo(recordPropertiesOutput);
    }


    @Test
    public void stopRecording() throws IOException, RecordException, ExecutionException, InterruptedException {

        JsonValue jsonRecordProperties = JsonValueBuilder.toJsonValue(
                IOUtils.getFileContentFromClassPath(RecordResourceTest.class,
                        RECORD_DIRECTORY + "startSimpleRecord.json"));
        debugRecorder.startRecording(jsonRecordProperties);

        // When...
        Promise<ActionResponse, ResourceException> promise = recordResource.actionStop(serverContext, request);

        RecordProperties recordPropertiesInput = RecordProperties.fromJson(jsonRecordProperties);
        RecordProperties recordPropertiesOutput = RecordProperties.fromJson(promise.get().getJsonContent().get("record"));

        assertThat(recordPropertiesInput).isEqualTo(recordPropertiesOutput);
    }

    @Test
    public void shouldFailIfAnnotationsAreNotValid() {
        ApiAnnotationAssert.assertThat(RecordResource.class).hasValidAnnotations();
    }
}