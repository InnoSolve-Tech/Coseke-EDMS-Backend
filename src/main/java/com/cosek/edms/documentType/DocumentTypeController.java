package com.cosek.edms.documentType;


import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/document-types")
@CrossOrigin(origins = "http://localhost:3000")
public class DocumentTypeController {
}
