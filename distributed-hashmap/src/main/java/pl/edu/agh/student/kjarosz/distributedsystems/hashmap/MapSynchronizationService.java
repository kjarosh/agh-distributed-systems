package pl.edu.agh.student.kjarosz.distributedsystems.hashmap;

import org.jgroups.*;
import org.jgroups.protocols.*;
import org.jgroups.protocols.pbcast.*;
import org.jgroups.stack.Protocol;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

class MapSynchronizationService implements AutoCloseable {
    static final int TIMEOUT = 12000;

    private final String clusterName;
    private final String address;

    private JChannel channel;

    private Consumer<String> removeListener = v -> {

    };

    private BiConsumer<String, Integer> putListener = (u, v) -> {

    };

    private Consumer<OutputStream> stateSerializer = v -> {

    };

    private Consumer<InputStream> stateDeserializer = v -> {

    };

    MapSynchronizationService(String clusterName, String address) {
        this.clusterName = clusterName;
        this.address = address;
    }

    public void init() throws Exception {
        channel = new JChannel(getProtocols(address));
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
                List<View> views;
                if (view instanceof MergeView) {
                    MergeView mergeView = (MergeView) view;
                    new Thread(new MergeViewHandler(channel, mergeView)).start();
                }
            }
        });
        channel.connect(clusterName);
        channel.getState(null, MapSynchronizationService.TIMEOUT);
    }

    private List<Protocol> getProtocols(String address) throws UnknownHostException {
        return Arrays.asList(
                new UDP()
                        .setValue("mcast_group_addr", InetAddress.getByName(address)),
                new PING(),
                new MERGE3(),
                new FD_SOCK(),
                new FD_ALL()
                        .setValue("timeout", TIMEOUT)
                        .setValue("interval", 3000),
                new VERIFY_SUSPECT(),
                new BARRIER(),
                new NAKACK2(),
                new UNICAST3(),
                new STABLE(),
                new GMS(),
                new UFC(),
                new MFC(),
                new FRAG2(),
                new SEQUENCER(),
                new FLUSH(),
                new STATE_TRANSFER());
    }

    private void receiveMessage(Message msg) {
        Object obj = msg.getObject();
        if (obj instanceof SynchronizedAction.Remove) {
            SynchronizedAction.Remove remove = (SynchronizedAction.Remove) obj;
            removeListener.accept(remove.getKey());
        } else if (obj instanceof SynchronizedAction.Put) {
            SynchronizedAction.Put put = (SynchronizedAction.Put) obj;
            putListener.accept(put.getKey(), put.getValue());
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
