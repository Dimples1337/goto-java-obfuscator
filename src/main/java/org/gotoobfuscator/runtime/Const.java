package org.gotoobfuscator.runtime;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.util.HashMap;

@SuppressWarnings("unused")
public final class Const {
    public static final Object[] ARRAY;

    static {
        Object[] cache;

        final InputStream stream = Const.class.getResourceAsStream("/Const");

        if (stream == null) {
            System.out.println("Const pool not found");

            cache = new Object[0];
        } else {
            try (final DataInputStream dis = new DataInputStream(stream)) {
                final int size = dis.readInt();

                cache = new Object[size];

                for (int i = 0; i < size; i++) {
                    final int type = dis.read();

                    switch (type) {
                        case 0:
                            cache[i] = dis.readUTF();
                            break;
                        case 1:
                            cache[i] = dis.readDouble();
                            break;
                        case 2:
                            cache[i] = dis.readFloat();
                            break;
                        case 3:
                            cache[i] = dis.readLong();
                            break;
                        case 4:
                            cache[i] = dis.readInt();
                            break;
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();

                cache = new Object[0];
            }
        }

        ARRAY = cache;
    }
}
