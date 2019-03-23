package pl.edu.agh.student.kjarosz.distributedsystems.hashmap;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.protocols.*;
import org.jgroups.protocols.pbcast.FLUSH;
import org.jgroups.protocols.pbcast.GMS;
import org.jgroups.protocols.pbcast.NAKACK2;
import org.jgroups.protocols.pbcast.STABLE;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

class MapSynchronizationService {
    private final JChannel channel;

    private Consumer<String> removeListener = v -> {

    };

    private BiConsumer<String, Integer> putListener = (u, v) -> {

    };

    MapSynchronizationService(String clusterName) throws Exception {
        channel = new JChannel(false);
        channel.getProtocolStack()
                .addProtocol(new UDP())
                .addProtocol(new PING())
                .addProtocol(new MERGE3())
                .addProtocol(new FD_SOCK())
                .addProtocol(new FD_ALL()
                        .setValue("timeout", 12000)
                        .setValue("interval", 3000))
                .addProtocol(new VERIFY_SUSPECT())
                .addProtocol(new BARRIER())
                .addProtocol(new NAKACK2())
                .addProtocol(new UNICAST3())
                .addProtocol(new STABLE())
                .addProtocol(new GMS())
                .addProtocol(new UFC())
                .addProtocol(new MFC())
                .addProtocol(new FRAG2())
                .addProtocol(new SEQUENCER())
                .addProtocol(new FLUSH())
                .init();
        channel.connect(clusterName);
    }

    void setRemoveListener(Consumer<String> removeListener) {
        this.removeListener = Objects.requireNonNull(removeListener);
    }

    void setPutListener(BiConsumer<String, Integer> putListener) {
        this.putListener = Objects.requireNonNull(putListener);
    }

    void synchronizeMap(SynchronizedAction action) {
        try {
            channel.send(new Message(null, action));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
