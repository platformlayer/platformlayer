//package org.platformlayer.ops.lxc;
//
//import java.io.File;
//import java.util.Collections;
//import java.util.List;
//import java.util.Properties;
//
//import org.platformlayer.ops.OpsException;
//import org.platformlayer.ops.OpsTarget;
//import org.platformlayer.ops.images.ConfigMap;
//
//import com.google.common.collect.Lists;
//
//public class DynamicFilesystemBackedPool extends FilesystemBackedPool {
//    final File resourceDir;
//
//    public DynamicFilesystemBackedPool(OpsTarget target, File resourceDir, File assignedDir) {
//        super(target, assignedDir);
//        this.resourceDir = resourceDir;
//    }
//
//    public Properties readProperties(String key) throws OpsException {
//        File path = new File(resourceDir, key);
//        Properties properties = ConfigMap.read(target, path);
//        return properties;
//    }
//
//    @Override
//    protected Iterable<String> pickRandomResource() throws OpsException {
//        List<String> resources = Lists.newArrayList(list(resourceDir));
//        Collections.shuffle(resources);
//        return resources;
//    }
//
// }
