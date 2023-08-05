package github.xinan.compress.gzip;

import github.xinan.compress.Compress;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * gzip压缩类
 *
 * @author xinan
 * @date 2022-05-24 21:40
 */
public class GzipCompress implements Compress {
    private static final int BUFFER_SIZE = 1024 * 4;
    /**
     * 压缩字节数组
     *
     * @param bytes
     * @return
     */
    @Override
    public byte[] compress(byte[] bytes) {
        if (bytes == null) {
            throw new NullPointerException("待压缩的字节数组为空");
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(out)) {
            gzip.write(bytes);
            gzip.flush();
            gzip.finish();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("gzip压缩失败");
        }
    }

    /**
     * 解压缩字节数组
     *
     * @param bytes
     * @return
     */
    @Override
    public byte[] decompress(byte[] bytes) {
        if (bytes == null) {
            throw new NullPointerException("待压缩的字节数组为空");
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GZIPInputStream gunzip = new GZIPInputStream(new ByteArrayInputStream(bytes))) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int n;
            while ((n = gunzip.read(buffer)) > -1) {
                out.write(buffer, 0, n);
            }
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("gzip解压失败");
        }
    }
}
