package pl.edu.agh.student.kjarosz.distributedsystems.hashmap;

import org.jgroups.JChannel;
import org.jgroups.MergeView;
import org.jgroups.Message;
import org.jgroups.View;

import java.util.List;

class MapMerger implements Runnable {
    private final JChannel channel;
    private final MergeView view;

    MapMerger(JChannel channel, MergeView view) {
        this.channel = channel;
        this.view = view;
    }

    @Override
    public void run() {
        List<View> subgroups = view.getSubgroups();
        subgroups.forEach(this::mergeView);

    }

    private void mergeView(View view) {
        if (view.getMembers().contains(channel.getAddress())) {
            return; // it's my view
        }

        try {
            channel.send(new Message(view.getCoord(), new SynchronizedAction.RequestMerge()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
