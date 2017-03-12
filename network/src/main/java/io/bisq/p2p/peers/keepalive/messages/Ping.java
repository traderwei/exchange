package io.bisq.p2p.peers.keepalive.messages;

import io.bisq.app.Version;
import io.bisq.common.util.ProtoBufferUtils;
import io.bitsquare.common.wire.proto.Messages;

public final class Ping extends KeepAliveMessage {
    // That object is sent over the wire, so we need to take care of version compatibility.
    private static final long serialVersionUID = Version.P2P_NETWORK_VERSION;

    public final int nonce;
    public final int lastRoundTripTime;

    public Ping(int nonce, int lastRoundTripTime) {
        this.nonce = nonce;
        this.lastRoundTripTime = lastRoundTripTime;
    }

    public Messages.Envelope toProtoBuf() {
        Messages.Envelope.Builder baseEnvelope = ProtoBufferUtils.getBaseEnvelope();
        return baseEnvelope.setPing(baseEnvelope.getPingBuilder()
                .setNonce(nonce)
                .setLastRoundTripTime(lastRoundTripTime)).build();
    }

    @Override
    public String toString() {
        return "Ping{" +
                ", nonce=" + nonce +
                "} " + super.toString();
    }
}