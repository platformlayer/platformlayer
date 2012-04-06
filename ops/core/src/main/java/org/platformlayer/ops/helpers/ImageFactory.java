package org.platformlayer.ops.helpers;

import java.util.List;

import javax.inject.Inject;

import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.TimeSpan;
import org.platformlayer.core.model.MachineCloudBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Tag;
import org.platformlayer.ops.CloudContext;
import org.platformlayer.ops.CloudImage;
import org.platformlayer.ops.EnumUtils;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsSystem;
import org.platformlayer.ops.images.ImageStore;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.service.imagefactory.v1.DiskImage;
import org.platformlayer.service.imagefactory.v1.DiskImageRecipe;
import org.platformlayer.xml.JaxbHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public class ImageFactory {
    public static final String IMAGE_FORMAT = "image_format";

    static final Logger log = LoggerFactory.getLogger(ImageFactory.class);

    private static final Tag BOOTSTRAP_IMAGE_TAG = new Tag("system_id", "http://org.platformlayer/service/imagefactory/v1.0:bootstrap");

    // public static final String FORMAT_TAR = "tar";
    // public static final String FORMAT_DISK = "disk";
    // public static final String FORMAT_QCOW2 = "qcow2";

    public enum ImageFormat {
        Tar, DiskRaw, DiskQcow2;

        public static ImageFormat forTag(Tag tag) {
            if (!tag.getKey().equals(IMAGE_FORMAT)) {
                throw new IllegalArgumentException();
            }
            return EnumUtils.valueOfCaseInsensitive(ImageFormat.class, tag.getValue());
        }
    };

    public static Tag buildImageFormatTag(ImageFormat format) {
        return new Tag(IMAGE_FORMAT, format.name().toLowerCase());
    }

    // public static final Tag TAG_IMAGEFORMAT_TAR = buildImageFormatTag(ImageFormat.Tar);
    // public static final Tag TAG_IMAGEFORMAT_DISK_RAW = buildImageFormatTag(ImageFormat.DiskRaw);
    // public static final Tag TAG_IMAGEFORMAT_DISK_QCOW2 = buildImageFormatTag(ImageFormat.DiskQcow2);

    @Inject
    PlatformLayerHelpers platformLayer;

    @Inject
    OpsContext ops;

    public DiskImageRecipe getOrCreateRecipe(DiskImageRecipe template) throws OpsException {
        DiskImageRecipe best = null;

        // TODO: What should be the parent of a disk image? It's not really owned by anyone...

        // TODO: This needs fixing once we've got the state transitions properly working
        try {
            for (DiskImageRecipe candidate : platformLayer.listItems(DiskImageRecipe.class)) {
                if (isMatch(candidate, template)) {
                    best = candidate;
                    break;
                }
            }

            if (best == null) {
                best = platformLayer.putItem(template);
            }
        } catch (PlatformLayerClientException e) {
            throw new OpsException("Error fetching or building recipe", e);
        }
        return best;
    }

    public DiskImage getOrCreateImage(DiskImage template) throws OpsException {
        DiskImage best = null;

        try {
            for (DiskImage candidate : platformLayer.listItems(DiskImage.class)) {
                if (isMatch(candidate, template)) {
                    best = candidate;
                    break;
                }
            }

            if (best == null) {
                // We should be owned by the recipe
                PlatformLayerKey recipeKey = template.getRecipeId();
                if (recipeKey != null) {
                    template.getTags().add(Tag.buildParentTag(recipeKey));
                }

                best = platformLayer.putItem(template);
            }
        } catch (PlatformLayerClientException e) {
            throw new OpsException("Error fetching or building image", e);
        }
        return best;
    }

    public String getOrCreateImageId(MachineCloudBase targetCloud, ImageFormat format, DiskImageRecipe recipeTemplate) throws OpsException {
        DiskImageRecipe recipeItem = getOrCreateRecipe(recipeTemplate);
        PlatformLayerKey recipeKey = OpsSystem.toKey(recipeItem);

        return getOrCreateImageId(targetCloud, format, recipeKey);
    }

    @Inject
    CloudContext cloud;

    public String getOrCreateImageId(MachineCloudBase targetCloud, ImageFormat format, PlatformLayerKey recipeKey) throws OpsException {
        if (recipeKey == null) {
            CloudImage bootstrapImage = findBootstrapImage(format, cloud.getImageStore(targetCloud));
            if (bootstrapImage == null) {
                throw new OpsException("Cannot find bootstrap image for format " + format);
            }
            return bootstrapImage.getId();
        }

        DiskImage imageTemplate = new DiskImage();
        imageTemplate.setFormat(format.name());
        imageTemplate.setRecipeId(recipeKey);
        String id = "image-" + recipeKey.getItemId().getKey();
        imageTemplate.setKey(PlatformLayerKey.fromId(id));

        PlatformLayerKey cloudKey = OpsSystem.toKey(targetCloud);
        imageTemplate.setCloud(cloudKey);

        DiskImage image = getOrCreateImage(imageTemplate);

        return getImageId(image);
    }

    private CloudImage findBootstrapImage(ImageFormat format, ImageStore imageStore) throws OpsException {
        Tag formatTag = buildImageFormatTag(format);

        CloudImage image = imageStore.findImage(Lists.newArrayList(formatTag, BOOTSTRAP_IMAGE_TAG));
        if (image != null)
            return image;

        Tag osDebian = new Tag(Tag.IMAGE_OS_DISTRIBUTION, "debian");
        Tag osSqueeze = new Tag(Tag.IMAGE_OS_VERSION, "squeeze");
        Tag imageTypeBase = new Tag(Tag.IMAGE_TYPE, "base");

        List<CloudImage> images = imageStore.findImages(Lists.newArrayList(formatTag, imageTypeBase, osDebian, osSqueeze));
        for (CloudImage candidate : images) {
            return candidate;
        }

        return null;
    }

    private String getImageId(DiskImage recipe) throws OpsException {
        String imageId = recipe.getTags().findUnique(Tag.IMAGE_ID);
        if (imageId == null) {
            throw new OpsException("Image is not yet built").setRetry(TimeSpan.ONE_MINUTE);
        }
        return imageId;
    }

    <T> T cloneThroughJaxb(T a) {
        // TODO: This is probably not the most efficient way to do this!
        try {
            String xml = JaxbHelper.toXml(a, false);
            return (T) JaxbHelper.deserializeXmlObject(xml, a.getClass());
        } catch (Exception e) {
            throw new IllegalStateException("Error while cloning object", e);
        }
    }

    private boolean isMatch(DiskImageRecipe a, DiskImageRecipe b) {
        // TODO: Don't be evil

        DiskImageRecipe aCopy = cloneThroughJaxb(a);
        DiskImageRecipe bCopy = cloneThroughJaxb(b);

        aCopy.setKey(null);
        bCopy.setKey(null);

        aCopy.tags = null;
        bCopy.tags = null;

        aCopy.version = 0;
        bCopy.version = 0;

        aCopy.state = null;
        bCopy.state = null;

        aCopy.secret = null;
        bCopy.secret = null;

        try {
            // TODO: What if e.g. package order is different

            String aXml = JaxbHelper.toXml(aCopy, false);
            String bXml = JaxbHelper.toXml(bCopy, false);
            return Objects.equal(aXml, bXml);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error comparing objects", e);
        }
    }

    private boolean isMatch(DiskImage a, DiskImage b) {
        // TODO: Don't be evil

        DiskImage aCopy = cloneThroughJaxb(a);
        DiskImage bCopy = cloneThroughJaxb(b);

        aCopy.setKey(null);
        bCopy.setKey(null);

        aCopy.tags = null;
        bCopy.tags = null;

        aCopy.version = 0;
        bCopy.version = 0;

        aCopy.state = null;
        bCopy.state = null;

        aCopy.secret = null;
        bCopy.secret = null;

        try {
            // TODO: What if e.g. package order is different

            String aXml = JaxbHelper.toXml(aCopy, false);
            String bXml = JaxbHelper.toXml(bCopy, false);
            return Objects.equal(aXml, bXml);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error comparing objects", e);
        }
    }

}
