package analyse.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
public class AnalyseMetadata {
    @Getter
    private String sgfName;
    @Getter
    private String sgf;
    @Getter
    private String model;
    @Getter
    private long runTimeSec;
}
