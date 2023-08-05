package github.xinan.compress;

import github.xinan.extension.SPI;

/**
 * 压缩类接口
 */
@SPI
public interface Compress {
    /**
     * 压缩字节数组
     * @param bytes
     * @return
     */
    byte[] compress(byte[] bytes);

    /**
     * 解压缩字节数组
     * @param bytes
     * @return
     */
    byte[] decompress(byte[] bytes);
}
