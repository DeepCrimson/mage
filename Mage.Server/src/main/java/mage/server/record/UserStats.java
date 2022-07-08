package mage.server.record;

import com.google.protobuf.InvalidProtocolBufferException;
import mage.game.result.ResultProtos.UserStatsProto;

public class UserStats {

    protected String userName;

    protected byte[] proto;

    protected long endTimeMs;

    public UserStats() {
    }

    public UserStats(UserStatsProto proto, long endTimeMs) {
        this.userName = proto.getName();
        this.proto = proto.toByteArray();
        this.endTimeMs = endTimeMs;
    }

    public UserStatsProto getProto() {
        try {
            return UserStatsProto.parseFrom(this.proto);
        } catch (InvalidProtocolBufferException ex) {
        }
        return null;
    }

    public long getEndTimeMs() {
        return this.endTimeMs;
    }
}
