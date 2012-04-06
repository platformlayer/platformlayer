//package org.platformlayer.ops.openstack;
//
//import java.io.File;
//
//import org.platformlayer.core.model.Tag;
//import org.platformlayer.core.model.Tags;
//import org.platformlayer.ops.CloudImage;
//import org.platformlayer.ops.OpsException;
//import org.platformlayer.ops.OpsTarget;
//
//public interface ImageClient {
//    void updateImageTags(String imageId, Tags tags) throws OpsException;
//
//    CloudImage findImage(Tag tag) throws OpsException;
//
//    String uploadImage(OpsTarget imageHost, File imageFile, long rawImageFileSize) throws OpsException;
// }
