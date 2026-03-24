package com.icodesoftware.community.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins="*")
@RequestMapping("/community")
public class CommunityController {

    private static Logger logger = LoggerFactory.getLogger(CommunityController.class);

    @GetMapping("/index")
    public ResponseEntity<String> index() {
        logger.debug("creating index data");
        return ResponseEntity.ok("index");
    }
}
