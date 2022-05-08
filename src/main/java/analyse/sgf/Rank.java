package analyse.sgf;

import java.util.Arrays;

public enum Rank {
    _35K_,
    _34K_,
    _33K_,
    _32K_,
    _31K_,
    _30K_,
    _29K_,
    _28K_,
    _27K_,
    _26K_,
    _25K_,
    _24K_,
    _23K_,
    _22K_,
    _21K_,
    _20K_,
    _19K_,
    _18K_,
    _17K_,
    _16K_,
    _15K_,
    _14K_,
    _13K_,
    _12K_,
    _11K_,
    _10K_,
    _9K_,
    _8K_,
    _7K_,
    _6K_,
    _5K_,
    _4K_,
    _3K_,
    _2K_,
    _1K_,
    _1D_,
    _2D_,
    _3D_,
    _4D_,
    _5D_,
    _6D_,
    _7D_,
    _8D_,
    _9D_,
    _1P_,
    _2P_,
    _3P_,
    _4P_,
    _5P_,
    _6P_,
    _7P_,
    _8P_,
    _9P_,
    _NR_;

    public String code() {
        return this.name().replaceAll("_", "");
    }

    public static Rank valueByCode(String code) {
        return Arrays.stream(values()).filter(r -> r.code().equalsIgnoreCase(code))
                .findFirst().orElse(Rank._NR_);
    }
}
