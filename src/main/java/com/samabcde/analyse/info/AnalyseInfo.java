package com.samabcde.analyse.info;

import com.samabcde.analyse.core.AnalyseMetadata;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnalyseInfo {
    @Getter
    private AnalyseMetadata metadata;
    @Getter
    private List<MoveInfo> moveInfoList;

    @JsonIgnore()
    public String getSgfName() {
        return metadata.getSgfName();
    }
}
