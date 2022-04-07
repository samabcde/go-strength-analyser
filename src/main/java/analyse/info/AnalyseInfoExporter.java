package analyse.info;

import analyse.core.ApplicationConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;

@Component
public class AnalyseInfoExporter {

    private final ApplicationConfig applicationConfig;

    public AnalyseInfoExporter(ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
    }

    public void export(AnalyseInfo analyseInfo) {
        String outputFilePath = applicationConfig.getAnalyseInfoFolder() + analyseInfo.getSgfName() + ".json";
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        try (FileWriter fileWriter = new FileWriter(outputFilePath)) {
            objectMapper.writeValue(fileWriter, analyseInfo);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
