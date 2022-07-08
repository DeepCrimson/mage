package mage.server.record;

import com.google.protobuf.InvalidProtocolBufferException;
import mage.game.result.ResultProtos.TableProto;

public class TableRecord {

    protected byte[] proto;

    protected long endTimeMs;

    public TableRecord() {
    }

    public TableRecord(TableProto proto, long endTimeMs) {
        this.proto = proto.toByteArray();
        this.endTimeMs = endTimeMs;
    }

    public TableProto getProto() {
        try {
            return TableProto.parseFrom(this.proto);
        } catch (InvalidProtocolBufferException ex) {
        }
        return null;
    }
}
