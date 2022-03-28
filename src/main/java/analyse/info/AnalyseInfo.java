package analyse.info;

import analyse.core.AnalyseMetadata;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@AllArgsConstructor
public class AnalyseInfo {
    @Getter
    private AnalyseMetadata metadata;
    @Getter
    private List<MoveInfo> moveInfoList;

    public String getSgfName() {
        return metadata.getSgfName();
    }
}
