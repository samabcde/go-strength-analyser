package com.samabcde.analyse.info;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

@Component
public class AnalyseInfoImporter {
    public AnalyseInfo importInfo(Resource resource) {
        ObjectMapper mapper = new ObjectMapper();
        try (Reader reader = new InputStreamReader(resource.getInputStream())) {
            return mapper.readValue(reader, AnalyseInfo.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
