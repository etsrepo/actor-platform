package im.actor.core.api.rpc;
/*
 *  Generated by the Actor API Scheme generator.  DO NOT EDIT!
 */

import im.actor.runtime.bser.*;
import im.actor.runtime.collections.*;
import static im.actor.runtime.bser.Utils.*;
import im.actor.core.network.parser.*;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;
import com.google.j2objc.annotations.ObjectiveCName;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import im.actor.core.api.*;

public class RequestUpgradeCall extends Request<ResponseVoid> {

    public static final int HEADER = 0xa75;
    public static RequestUpgradeCall fromBytes(byte[] data) throws IOException {
        return Bser.parse(new RequestUpgradeCall(), data);
    }

    private long callId;
    private ApiGroupOutPeer peer;

    public RequestUpgradeCall(long callId, @NotNull ApiGroupOutPeer peer) {
        this.callId = callId;
        this.peer = peer;
    }

    public RequestUpgradeCall() {

    }

    public long getCallId() {
        return this.callId;
    }

    @NotNull
    public ApiGroupOutPeer getPeer() {
        return this.peer;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        this.callId = values.getLong(1);
        this.peer = values.getObj(2, new ApiGroupOutPeer());
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeLong(1, this.callId);
        if (this.peer == null) {
            throw new IOException();
        }
        writer.writeObject(2, this.peer);
    }

    @Override
    public String toString() {
        String res = "rpc UpgradeCall{";
        res += "callId=" + this.callId;
        res += ", peer=" + this.peer;
        res += "}";
        return res;
    }

    @Override
    public int getHeaderKey() {
        return HEADER;
    }
}