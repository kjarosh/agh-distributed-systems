package pl.edu.agh.student.kjarosz.distributedsystems.hashmap;

import org.apache.commons.io.IOUtils;
import org.jgroups.util.Streamable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;

interface SynchronizedAction extends Streamable {
    class Remove implements SynchronizedAction {
        private String key;

        Remove() {

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

        Put() {

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

    class RequestMerge implements SynchronizedAction {
        RequestMerge() {

        }

        @Override
        public void writeTo(DataOutput out) {

        }

        @Override
        public void readFrom(DataInput in) {

        }
    }

    class Merge implements SynchronizedAction {
        private byte[] data;

        Merge() {

        }

        Merge(InputStream inputStream) throws IOException {
            data = IOUtils.toByteArray(inputStream);
        }

        byte[] getData() {
            return data;
        }

        @Override
        public void writeTo(DataOutput out) throws Exception {
            out.writeInt(data.length);
            out.write(data);
        }

        @Override
        public void readFrom(DataInput in) throws Exception {
            data = new byte[in.readInt()];
            in.readFully(data);
        }
    }
}
