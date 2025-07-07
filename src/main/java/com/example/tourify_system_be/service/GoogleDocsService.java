package com.example.tourify_system_be.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.docs.v1.Docs;
import com.google.api.services.docs.v1.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Collections;

@Service
public class GoogleDocsService {
    private final Docs docsService;
    private final String documentId;

    public GoogleDocsService(
            @Value("${google.docs.documentId}") String documentId
    ) throws Exception {
        this.documentId = documentId;

        // 1) Load credentials.json từ resources
        InputStream in = getClass().getResourceAsStream("/credentials.json");
        GoogleCredential credential = GoogleCredential.fromStream(in)
                .createScoped(Collections.singletonList("https://www.googleapis.com/auth/documents"));

        // 2) Khởi tạo Docs client
        this.docsService = new Docs.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                credential)
                .setApplicationName("Tourify Chat Logger")
                .build();
    }

    /** Thêm đoạn text vào cuối document */
    public void appendText(String text) throws Exception {
        Request insertReq = new Request()
                .setInsertText(new InsertTextRequest()
                        .setText(text + "\n")
                        .setEndOfSegmentLocation(new EndOfSegmentLocation())
                );

        BatchUpdateDocumentRequest body = new BatchUpdateDocumentRequest()
                .setRequests(Collections.singletonList(insertReq));

        docsService.documents()
                .batchUpdate(documentId, body)
                .execute();
    }
}
