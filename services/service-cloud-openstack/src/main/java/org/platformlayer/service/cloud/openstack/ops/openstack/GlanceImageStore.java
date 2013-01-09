package org.platformlayer.service.cloud.openstack.ops.openstack;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;
import org.openstack.client.OpenstackException;
import org.openstack.client.common.OpenstackImageClient;
import org.openstack.model.identity.Access;
import org.openstack.model.image.Image;
import org.platformlayer.TimeSpan;
import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.Tags;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.images.CloudImage;
import org.platformlayer.ops.images.ImageFormat;
import org.platformlayer.ops.images.ImageStore;
import org.platformlayer.ops.process.ProcessExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class GlanceImageStore implements ImageStore {

	private static final Logger log = LoggerFactory.getLogger(GlanceImageStore.class);

	private final OpenstackImageClient openstackImageClient;

	public GlanceImageStore(OpenstackImageClient openstackImageClient) {
		this.openstackImageClient = openstackImageClient;
	}

	@Override
	public String uploadImage(OpsTarget target, Tags tags, File imageFile, long rawImageFileSize) throws OpsException {
		OpenstackImageClient client = getOpenstackImageClient();

		String diskFormat = null;
		if (tags != null) {
			assert false; // This logic looks suspicious...
			for (Tag tag : tags.getTags()) {
				ImageFormat imageFormat = ImageFormat.fromTags(tags);
				diskFormat = mapToGlanceDiskFormat(imageFormat);
			}
		}

		String glanceBaseUrl;
		String tokenId;

		try {
			Access access = client.getSession().getAuthenticationToken();
			tokenId = access.getToken().getId();
			glanceBaseUrl = client.root().getBaseUrl();
		} catch (OpenstackException e) {
			throw new OpsException("Error getting glance url", e);
		}

		// Upload to glance
		String glanceUploadUrl = glanceBaseUrl;
		if (!glanceUploadUrl.endsWith("/")) {
			glanceUploadUrl += "/";
		}
		glanceUploadUrl += "images";

		String imageName = "image-" + System.currentTimeMillis();

		Command command = Command.build("curl");
		command.addLiteral("--fail");
		command.addLiteral("--upload-file").addFile(imageFile);
		command.addLiteral("-X").addLiteral("POST");

		command.addLiteral("-H").addQuoted("X-Auth-Token: " + tokenId);

		command.addLiteral("-H").addQuoted("Content-Type: application/octet-stream");
		command.addLiteral("-H").addQuoted("X-Image-Meta-Name: " + imageName);
		command.addLiteral("-H").addQuoted("X-Image-Meta-Is-Public: True");

		// if (isQcow2) {
		// command.addLiteral("-H").addQuoted("X-Image-Meta-Disk-Format: qcow2");
		// } else {
		if (diskFormat != null) {
			command.addLiteral("-H").addQuoted("X-Image-Meta-Disk-Format: " + diskFormat);
		}

		command.addLiteral("-H").addQuoted("X-Image-Meta-Container-Format: bare");
		// }
		// command.addLiteral("-H").addQuoted("X-Image-Meta-Min-Disk: 0");
		// command.addLiteral("-H").addQuoted("X-Image-Meta-Min-Ram: 0");
		// command.addLiteral("-H").addQuoted("X-Image-Meta-Image-Size: " + rawImageFileSize);
		command.addLiteral("-H").addQuoted("X-Image-Meta-Size: " + rawImageFileSize);

		// image_meta = {'name': fields.pop('name'),
		// 'is_public': utils.bool_from_string(
		// fields.pop('is_public', False)),
		// 'disk_format': fields.pop('disk_format', 'raw'),
		// 'min_disk': fields.pop('min_disk', 0),
		// 'min_ram': fields.pop('min_ram', 0),
		// 'container_format': fields.pop('container_format', 'ovf')}

		// glance add name=DebianSqueeze is_public=True disk_format=raw container_format=bare
		// system_id="http://org.platformlayer/service/imagefactory/v1.0:bootstrap"
		// image_size="${RAW_SIZE}" < disk.raw.gz

		command.addQuoted(glanceUploadUrl);
		command.setTimeout(TimeSpan.FIFTEEN_MINUTES);

		ProcessExecution execution = target.executeCommand(command);

		String imageId;
		// String imageLocation;
		{
			// {"image": {"status": "active", "name": null, "deleted": false,
			// "container_format": null, "created_at":
			// "2011-04-10T00:15:57.563479",
			// "disk_format": null, "updated_at":
			// "2011-04-10T00:21:29.300219", "id": 8, "location":
			// "file:///var/lib/glance/images/8", "checksum":
			// "bfbd641fe10edb3ecea933303e5408ec", "is_public": false,
			// "deleted_at":
			// null, "properties":
			// {}, "size": 8577351680}}

			// {"image": {"status": "active", "name": "image-1324662647963", "deleted": false, "container_format": null,
			// "created_at": "2011-12-23T17:50:48.265346", "disk_format": "qcow2",
			// "updated_at": "2011-12-23T17:51:42.229359", "properties": {}, "min_disk": 0, "id":
			// "f41d4043-f608-ea2f-a642-e7621bec2b66", "checksum": "ffeafdc8757658b481f3b1a3c2a33c98", "owner": null,
			// "is_public": true, "deleted_at": null, "min_ram": 0, "size": 925761536}}
			try {
				JSONObject json = new JSONObject(execution.getStdOut());
				JSONObject image = json.getJSONObject("image");
				// imageLocation = image.getString("location");
				imageId = image.getString("id");
			} catch (JSONException e) {
				log.warn("Image upload returned: " + execution.getStdOut());
				throw new OpsException("Error parsing return value from image upload", e);
			}
		}

		if (tags != null) {
			updateImageTags(imageId, tags);
		}

		return imageId;
	}

	private OpenstackImageClient getOpenstackImageClient() throws OpsException {
		return openstackImageClient;
	}

	@Override
	public void updateImageTags(String imageId, Tags tags) throws OpsException {
		OpenstackImageClient glanceClient = getOpenstackImageClient();

		boolean replace = false;
		try {
			Map<String, Object> tagMap = Maps.newHashMap();
			for (Tag tag : tags.getTags()) {
				tagMap.put(tag.getKey(), tag.getValue());
			}
			glanceClient.root().images().image(imageId).updateMetadata(tagMap, replace);
		} catch (OpenstackException e) {
			throw new OpsException("Error updating image tags", e);
		}
	}

	// Metadata metadata = mapToMetadata(tags);
	// private Metadata mapToMetadata(Tags tags) {
	// Metadata metadata = new Metadata();
	// if (tags != null) {
	// for (Tag tag : tags) {
	// Meta meta = new Meta();
	// meta.setKey(tag.getKey());
	// meta.setContent(tag.getValue());
	// metadata.getMeta().add(meta);
	// }
	// }
	// return metadata;
	// }

	@Override
	public CloudImage findImage(List<Tag> tags) throws OpsException {
		List<CloudImage> images = findImages(tags);
		if (images.size() == 0) {
			return null;
		}
		return images.get(0);
	}

	@Override
	public List<CloudImage> findImages(List<Tag> tags) throws OpsException {
		List<CloudImage> matches = Lists.newArrayList();

		OpenstackImageClient glanceClient = getOpenstackImageClient();

		Iterable<Image> images;
		try {
			images = glanceClient.root().images().list(true);
		} catch (OpenstackException e) {
			throw new OpsException("Error listing images", e);
		}

		for (Image image : images) {
			boolean match = true;

			for (Tag tag : tags) {
				match = false;

				String tagKey = tag.getKey();
				String tagValue = tag.getValue();

				boolean checked = false;

				if (ImageFormat.isImageFormatTag(tag)) {
					ImageFormat format = ImageFormat.fromTag(tag);

					// if ("qcow2".equals(tagValue)) {
					// format = ImageFormat.DiskQcow2;
					// } else {
					// throw new UnsupportedOperationException("Unknown glance disk_format: " + tagValue);
					// }
					//
					// Tag mappedTag = format.toTag();
					//
					// match = Objects.equal(mappedTag.getValue().getDiskFormat(), glanceDiskFormat);
					// checked = true;

					String glanceDiskFormat = mapToGlanceDiskFormat(format);
					if (glanceDiskFormat != null) {
						match = Objects.equal(image.getDiskFormat(), glanceDiskFormat);
						checked = true;
					}
				}

				if (!checked) {
					for (Entry<String, Object> meta : image.getProperties().asMap().entrySet()) {
						if (Objects.equal(tagKey, meta.getKey())) {
							String content = (String) meta.getValue();

							// OS BUG #885044: Content contains whitespace
							content = content.trim();

							if (content.equals(tagValue)) {
								match = true;
							}
						}
					}
				}

				if (!match) {
					break;
				}
			}

			if (match) {
				matches.add(new GlanceImage(image));
			}

		}

		return matches;
	}

	private static String mapToGlanceDiskFormat(ImageFormat format) {
		switch (format) {
		case DiskQcow2:
			return "qcow2";

		case DiskRaw:
			return "raw";

		case Tar:
			// TODO: ??
			// No mapping in glance ??
			return null;

		default:
			return null;
		}
	}

	@Override
	public void bringToMachine(String imageId, OpsTarget destination, File destinationFile) throws OpsException {
		throw new UnsupportedOperationException();
	}

}
