package analyse.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnalyseMetadata {
    @Getter
    private String sgfName;
    @Getter
    private String sgf;
    @Getter
    private long runTimeSec;
}
