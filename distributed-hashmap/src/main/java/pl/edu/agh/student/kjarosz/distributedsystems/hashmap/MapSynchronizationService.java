package pl.edu.agh.student.kjarosz.distributedsystems.hashmap;

import org.jgroups.*;
import org.jgroups.protocols.*;
import org.jgroups.protocols.pbcast.FLUSH;
import org.jgroups.protocols.pbcast.GMS;
import org.jgroups.protocols.pbcast.NAKACK2;
import org.jgroups.protocols.pbcast.STABLE;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

class MapSynchronizationService implements AutoCloseable {
    private final JChannel channel;

    private Consumer<String> removeListener = v -> {

    };

    private BiConsumer<String, Integer> putListener = (u, v) -> {

    };

    private Consumer<OutputStream> stateSerializer = v -> {

    };

    private Consumer<InputStream> stateDeserializer = v -> {

    };

    private Consumer<InputStream> merger = v -> {

    };

    MapSynchronizationService(String clusterName, String address) throws Exception {
        channel = new JChannel(false);
        channel.getProtocolStack()
                .addProtocol(new UDP()
                        .setValue("mcast_group_addr", InetAddress.getByName(address)))
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

        channel.setReceiver(new ReceiverAdapter() {
            @Override
            public void receive(Message msg) {
                receiveMessage(msg);
            }

            @Override
            public void getState(OutputStream output) {
                stateSerializer.accept(output);
            }

            @Override
            public void setState(InputStream input) {
                stateDeserializer.accept(input);
            }

            @Override
            public void viewAccepted(View view) {
                if (view instanceof MergeView) {
                    MergeView mergeView = (MergeView) view;
                    new Thread(new MapMerger(channel, mergeView)).start();
                }
            }
        });
        channel.connect(clusterName);
        channel.getState(null, 1000);
    }

    private void receiveMessage(Message msg) {
        Object obj = msg.getObject();
        if (obj instanceof SynchronizedAction.Remove) {
            SynchronizedAction.Remove remove = (SynchronizedAction.Remove) obj;
            removeListener.accept(remove.getKey());
        } else if (obj instanceof SynchronizedAction.Put) {
            SynchronizedAction.Put put = (SynchronizedAction.Put) obj;
            putListener.accept(put.getKey(), put.getValue());
        } else if (obj instanceof SynchronizedAction.Merge) {
            SynchronizedAction.Merge merge = (SynchronizedAction.Merge) obj;
            merger.accept(new ByteArrayInputStream(merge.getData()));
        }
    }

    void setRemoveListener(Consumer<String> removeListener) {
        this.removeListener = Objects.requireNonNull(removeListener);
    }

    void setPutListener(BiConsumer<String, Integer> putListener) {
        this.putListener = Objects.requireNonNull(putListener);
    }

    void setStateSerializer(Consumer<OutputStream> stateSerializer) {
        this.stateSerializer = Objects.requireNonNull(stateSerializer);
    }

    void setStateDeserializer(Consumer<InputStream> stateDeserializer) {
        this.stateDeserializer = Objects.requireNonNull(stateDeserializer);
    }

    void setMerger(Consumer<InputStream> merger) {
        this.merger = Objects.requireNonNull(merger);
    }

    void synchronizePut(String key, Integer value) {
        synchronizeAction(new SynchronizedAction.Put(key, value));
    }

    void synchronizeRemove(String key) {
        synchronizeAction(new SynchronizedAction.Remove(key));
    }

    private void synchronizeAction(SynchronizedAction action) {
        try {
            channel.send(new Message(null, action));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        channel.close();
    }
}
