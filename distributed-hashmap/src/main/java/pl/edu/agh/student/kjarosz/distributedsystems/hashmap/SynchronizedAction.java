package pl.edu.agh.student.kjarosz.distributedsystems.hashmap;

import org.jgroups.util.Streamable;

import java.io.DataInput;
import java.io.DataOutput;

interface SynchronizedAction extends Streamable {
    class Remove implements SynchronizedAction {
        private String key;

        Remove() {

        }

        Remove(String key) {
            this.key = key;
        }

        @Override
        public void writeTo(DataOutput out) throws Exception {
            out.writeUTF(key);
        }

        @Override
        public void readFrom(DataInput in) throws Exception {
            key = in.readUTF();
        }
    }

    class Put implements SynchronizedAction {
        private String key;
        private Integer value;

        Put() {

        }

        Put(String key, Integer value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public void writeTo(DataOutput out) throws Exception {
            out.writeUTF(key);
            out.writeBoolean(value == null);
            if (value != null) {
                out.write(value);
            }
        }

        @Override
        public void readFrom(DataInput in) throws Exception {
            key = in.readUTF();
            if (in.readBoolean()) {
                value = null;
            } else {
                value = in.readInt();
            }
        }
    }
}
