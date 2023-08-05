package extension;

import github.xinan.extension.ExtensionLoader;
import org.junit.Test;
import github.xinan.serialize.Serializer;

/**
 * TODO
 *
 * @author xinan
 * @date 2022-05-24 20:42
 */
public class ExtensionTest {

    @Test
    public void  extensionTest() {
        ExtensionLoader<Serializer> extensionLoader = ExtensionLoader.getExtensionLoader(Serializer.class);
        Serializer kryo = extensionLoader.getExtension("kyro");

    }

}
