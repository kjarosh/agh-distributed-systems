package pl.edu.agh.student.kjarosz.distributedsystems.hashmap;

import org.jgroups.JChannel;
import org.jgroups.MergeView;
import org.jgroups.View;

class MergeViewHandler implements Runnable {
    private final JChannel channel;
    private final MergeView view;

    MergeViewHandler(JChannel channel, MergeView view) {
        this.channel = channel;
        this.view = view;
    }

    @Override
    public void run() {
        mergeView(view.getSubgroups().get(0));
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
