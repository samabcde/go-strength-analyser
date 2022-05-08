package analyse.info;

import analyse.core.AnalyseMetadata;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AnalyseInfoImporterTest {
    private AnalyseInfoImporter analyseInfoImporter = new AnalyseInfoImporter();

    @Test
    public void Given_infoJson_Should_importToAnalyseInfo() {
        Resource resource = new ClassPathResource("/analyse_info/runKataGoTest.json");
        AnalyseInfo analyseInfo = analyseInfoImporter.importInfo(resource);
        AnalyseMetadata metadata = analyseInfo.getMetadata();
        List<MoveInfo> moveInfoList = analyseInfo.getMoveInfoList();
        assertThat(metadata.getSgfName()).isEqualTo("runKataGoTest");
        assertThat(moveInfoList).hasSize(133);
    }
}