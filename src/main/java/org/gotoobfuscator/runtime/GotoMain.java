package org.gotoobfuscator.runtime;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class GotoMain extends ClassLoader {
    private static byte[] key;

    public GotoMain(ClassLoader parent) {
        super(parent);
    }

    public static void main(String[] args) throws Throwable {
        key = Base64.getDecoder().decode("%{KEY_BASE64}%".getBytes(StandardCharsets.UTF_8));

        final GotoMain gotoMain = new GotoMain(GotoMain.class.getClassLoader());

        Thread.currentThread().setContextClassLoader(gotoMain);

        gotoMain.loadClass(new String(Base64.getDecoder().decode("%{MAIN_CLASS_BASE64}%".getBytes(StandardCharsets.UTF_8)),StandardCharsets.UTF_8)).getMethod("main",String[].class).invoke(null, new Object[]{args});
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        final String replaced = name.replace(".", "/");
        final String encrypted = name(replaced.toCharArray(),key.length);
        final InputStream stream = GotoMain.class.getResourceAsStream("/" + encrypted);

        if (stream != null) {
            try {
                final ByteArrayOutputStream bos = new ByteArrayOutputStream();
                final byte[] buffer = new byte[512];
                int length;

                while ((length = stream.read(buffer)) != -1) {
                    bos.write(buffer,0,length);
                }

                try {
                    final byte[] bytes = decrypt(bos.toByteArray());

                    return defineClass(name, bytes,0,bytes.length);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    stream.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }

        throw new ClassNotFoundException(name);
    }

    private static byte[] decrypt(byte[] data) throws Exception {
        final SecureRandom sr = new SecureRandom();
        final DESKeySpec dks = new DESKeySpec(key);
        final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        final SecretKey secureKey = keyFactory.generateSecret(dks);
        final Cipher cipher = Cipher.getInstance("DES");

        cipher.init(Cipher.DECRYPT_MODE, secureKey, sr);

        return cipher.doFinal(data);
    }

    public static String name(char[] c,int key) {
        final char[] c2 = new char[c.length];

        for (int i = 0; i < c.length; i++) {
            c2[i] = (char) ~(c[i] ^ key ^ (i + 1));
        }

        final String end;

        switch (c.length % 9) {
            case 0: {
                end = ".exe";
                break;
            }
            case 1: {
                end = ".jar";
                break;
            }
            case 2: {
                end = ".png";
                break;
            }
            case 3: {
                end = ".bmp";
                break;
            }
            case 4: {
                end = ".bat";
                break;
            }
            case 5: {
                end = ".dll";
                break;
            }
            case 6: {
                end = ".jpg";
                break;
            }
            case 7: {
                end = ".mp3";
                break;
            }
            case 8: {
                end = ".mp4";
                break;
            }
            default: {
                end = "";
                break;
            }
        }

        return (new String(c2) + end).intern();
    }
}
