package pl.edu.agh.student.kjarosz.distributedsystems.hashmap;

import org.jgroups.util.Streamable;

import java.io.DataInput;
import java.io.DataOutput;

public interface SynchronizedAction extends Streamable {
    class Remove implements SynchronizedAction {
        private String key;

        public Remove() {

        }

        Remove(String key) {
            this.key = key;
        }

        String getKey() {
            return key;
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

        public Put() {

        }

        Put(String key, Integer value) {
            this.key = key;
            this.value = value;
        }

        String getKey() {
            return key;
        }

        Integer getValue() {
            return value;
        }

        @Override
        public void writeTo(DataOutput out) throws Exception {
            out.writeUTF(key);
            out.writeBoolean(value == null);
            if (value != null) {
                out.writeInt(value);
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
