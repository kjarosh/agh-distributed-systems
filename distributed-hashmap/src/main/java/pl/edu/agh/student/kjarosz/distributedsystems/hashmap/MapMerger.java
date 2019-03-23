package pl.edu.agh.student.kjarosz.distributedsystems.hashmap;

import org.jgroups.JChannel;
import org.jgroups.View;

import java.util.List;

class MapMerger implements Runnable {
    private final JChannel channel;
    private final List<View> views;

    MapMerger(JChannel channel, List<View> views) {
        this.channel = channel;
        this.views = views;
    }

    @Override
    public void run() {
        views.forEach(this::mergeView);

    }

    private void mergeView(View view) {
        if (view.getMembers().contains(channel.getAddress())) {
            return; // it's my view
        }

        try {
            channel.getState(view.getCoord(), MapSynchronizationService.TIMEOUT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
