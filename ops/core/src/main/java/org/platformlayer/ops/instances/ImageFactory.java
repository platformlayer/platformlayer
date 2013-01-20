package org.platformlayer.ops.instances;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.TimeSpan;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Tag;
import org.platformlayer.images.model.DiskImage;
import org.platformlayer.images.model.DiskImageRecipe;
import org.platformlayer.ops.CloudContext;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.helpers.ProviderHelper;
import org.platformlayer.ops.images.CloudImage;
import org.platformlayer.ops.images.ImageFormat;
import org.platformlayer.ops.images.ImageStore;
import org.platformlayer.ops.machines.MachineProvider;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.xml.JaxbHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;

public class ImageFactory {

	static final Logger log = LoggerFactory.getLogger(ImageFactory.class);

	private static final Tag BOOTSTRAP_IMAGE_TAG = Tag.build("system_id",
			"http://org.platformlayer/service/imagefactory/v1.0:bootstrap");

	// public static final String FORMAT_TAR = "tar";
	// public static final String FORMAT_DISK = "disk";
	// public static final String FORMAT_QCOW2 = "qcow2";

	// public static final Tag TAG_IMAGEFORMAT_TAR = buildImageFormatTag(ImageFormat.Tar);
	// public static final Tag TAG_IMAGEFORMAT_DISK_RAW = buildImageFormatTag(ImageFormat.DiskRaw);
	// public static final Tag TAG_IMAGEFORMAT_DISK_QCOW2 = buildImageFormatTag(ImageFormat.DiskQcow2);

	@Inject
	PlatformLayerHelpers platformLayer;

	@Inject
	OpsContext ops;

	@Inject
	ProviderHelper providers;

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

	public CloudImage getOrCreateImageId(MachineProvider targetCloud, List<ImageFormat> formats,
			DiskImageRecipe recipeTemplate) throws OpsException {
		DiskImageRecipe recipeItem = getOrCreateRecipe(recipeTemplate);
		PlatformLayerKey recipeKey = recipeItem.getKey();

		return getOrCreateImageId(targetCloud, formats, recipeKey);
	}

	@Inject
	CloudContext cloud;

	public CloudImage getOrCreateImageId(ItemBase targetCloud, List<ImageFormat> formats, PlatformLayerKey recipeKey)
			throws OpsException {
		MachineProvider machineProvider = providers.toInterface(targetCloud, MachineProvider.class);
		return getOrCreateImageId(machineProvider, formats, recipeKey);
	}

	public CloudImage getOrCreateImageId(MachineProvider targetCloud, List<ImageFormat> formats,
			PlatformLayerKey recipeKey) throws OpsException {
		if (recipeKey == null) {
			log.debug("Looking for bootstrap image");

			for (ImageFormat format : formats) {
				CloudImage bootstrapImage = findBootstrapImage(format, cloud.getImageStore(targetCloud));
				if (bootstrapImage != null) {
					return bootstrapImage;
					// Tags tags = bootstrapImage.getTags();
					// String compression = tags.findUnique("org.openstack.sync__1__expand");
					// return new ImageInfo(bootstrapImage.getId(), format, compression);
				}
			}
			throw new OpsException("Cannot find bootstrap image for format " + Joiner.on(",").join(formats));
		}

		DiskImage imageTemplate = new DiskImage();
		imageTemplate.setFormat(formats.get(0).name());
		imageTemplate.setRecipeId(recipeKey);
		String id = "image-" + recipeKey.getItemId().getKey();
		imageTemplate.setKey(PlatformLayerKey.fromId(id));

		PlatformLayerKey cloudKey = targetCloud.getModel().getKey();
		imageTemplate.setCloud(cloudKey);

		DiskImage image = getOrCreateImage(imageTemplate);
		return getImageInfo(image);
	}

	private CloudImage findBootstrapImage(ImageFormat format, ImageStore imageStore) throws OpsException {
		// Tag formatTag = buildImageFormatTag(format);
		//
		// CloudImage image = imageStore.findImage(Lists.newArrayList(formatTag, BOOTSTRAP_IMAGE_TAG));
		// if (image != null) {
		// return image;
		// }

		Tag osDebian = Tag.build(Tag.IMAGE_OS_DISTRIBUTION, "debian.org");
		Tag osSqueeze = Tag.build(Tag.IMAGE_OS_VERSION, "6.0.4");

		Tag diskFormatTag;
		switch (format) {
		case DiskQcow2:
			diskFormatTag = format.toTag();
			break;
		default:
			log.warn("Unsupported format: " + format);
			return null;
		}

		List<CloudImage> images = imageStore.findImages(Arrays.asList(diskFormatTag, osDebian, osSqueeze));
		for (CloudImage candidate : images) {
			return candidate;
		}

		return null;
	}

	private CloudImage getImageInfo(DiskImage recipe) throws OpsException {
		final String imageId = Tag.IMAGE_ID.findUnique(recipe.getTags());
		if (imageId == null) {
			throw new OpsException("Image is not yet built: " + recipe).setRetry(TimeSpan.ONE_MINUTE);
		}
		final ImageFormat imageFormat = ImageFormat.valueOf(recipe.getFormat());
		String compression = null;
		switch (imageFormat) {
		case Tar:
			compression = "gzip";
			break;
		case DiskRaw:
			compression = "gzip";
			break;
		}
		return new CloudImage() {

			@Override
			public String getId() {
				return imageId;
			}

			@Override
			public ImageFormat getFormat() {
				return imageFormat;
			}

		};
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
