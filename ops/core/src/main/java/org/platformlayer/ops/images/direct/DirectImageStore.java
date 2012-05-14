package org.platformlayer.ops.images.direct;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.TimeSpan;
import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.Tags;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.images.CloudImage;
import org.platformlayer.ops.images.ImageStore;
import org.platformlayer.ops.images.PropertiesFileStore;

import com.google.common.collect.Lists;

public class DirectImageStore implements ImageStore {
	static final Logger log = Logger.getLogger(DirectImageStore.class);

	private static final String PROPERTY_KEY_IMAGE_RAW_SIZE = "image.size.raw";

	PropertiesFileStore fileStore;

	OpsTarget target;

	@Inject
	ExecutorService executorService;

	public void connect(OpsTarget target) {
		this.target = target;
		this.fileStore = new PropertiesFileStore(target, getImagesDir());
	}

	private File getImagesDir() {
		String path = "/home/imagestore/images";
		return new File(path);
	}

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
		List<CloudImage> images = Lists.newArrayList();
		for (String imageId : fileStore.find(tags)) {
			Properties properties = fileStore.readProperties(imageId);
			Tags imageTags = fileStore.asTags(properties);
			images.add(new DirectCloudImage(this, imageId, imageTags));
		}
		return images;
	}

	@Override
	public void updateImageTags(String imageId, Tags tags) throws OpsException {
		if (tags.isEmpty()) {
			return;
		}

		Properties properties = fileStore.readProperties(imageId);

		Properties tagProperties = fileStore.toProperties(tags);
		properties.putAll(tagProperties);

		fileStore.writeProperties(imageId, properties);
	}

	@Override
	public String uploadImage(final OpsTarget srcImageHost, Tags tags, final File srcImageFile, long rawImageFileSize)
			throws OpsException {
		Properties properties = new Properties();
		properties.put(PROPERTY_KEY_IMAGE_RAW_SIZE, "" + rawImageFileSize);

		String imageId = UUID.randomUUID().toString();

		final File targetImageFile = getImageFile(imageId, null);

		if (srcImageHost.equals(target)) {
			Command copyCommand = Command.build("cp", srcImageFile, targetImageFile);
			target.executeCommand(copyCommand.setTimeout(TimeSpan.TEN_MINUTES));
		} else {
			int maxAttempts = 3;
			Random random = new Random();

			for (int attempt = 1; attempt <= maxAttempts; attempt++) {
				final InetAddress srcAddress = ((SshOpsTarget) srcImageHost).getHost();
				final InetAddress targetAddress = ((SshOpsTarget) target).getHost();

				// TODO: Better security
				final String port = "" + (random.nextInt(1000) + 20000);

				Callable<ProcessExecution> serveFile = new Callable<ProcessExecution>() {
					@Override
					public ProcessExecution call() throws Exception {
						// TODO: Secure this better (using host address is probably sufficient, but then we need a full
						// network map to know which IP to use)
						// Command sendCommand = Command.build("socat -u OPEN:{0},rdonly TCP4-LISTEN:{1},range={2}",
						// srcImageFile, port, targetAddress.getHostAddress() + "/32");
						Command sendCommand = Command.build("socat -u OPEN:{0},rdonly TCP4-LISTEN:{1}", srcImageFile,
								port, targetAddress.getHostAddress() + "/32");
						return srcImageHost.executeCommand(sendCommand.setTimeout(TimeSpan.TEN_MINUTES));
					}
				};

				Future<ProcessExecution> serveFuture = executorService.submit(serveFile);

				for (int readAttempts = 1; readAttempts <= 10; readAttempts++) {
					TimeSpan.ONE_SECOND.doSafeSleep();

					Command receiveCommand = Command.build("socat -u TCP4:{0} CREATE:{1}", srcAddress.getHostAddress()
							+ ":" + port, targetImageFile);
					try {
						target.executeCommand(receiveCommand.setTimeout(TimeSpan.TEN_MINUTES));
						break;
					} catch (ProcessExecutionException e) {
						ProcessExecution recvExecution = e.getExecution();
						if (recvExecution.getExitCode() == 1
								&& recvExecution.getStdErr().contains("Connection refused")) {
							log.info("Got connection refused; will retry");
						} else {
							throw new OpsException("Error receiving image file", e);
						}
					}

					if (serveFuture.isDone()) {
						try {
							ProcessExecution serveExecution = serveFuture.get();
							log.warn("Image sending exited: " + serveExecution);
						} catch (ExecutionException e) {
							throw new OpsException("Error sending file to image store", e);
						} catch (InterruptedException e) {
							ExceptionUtils.handleInterrupted(e);
							throw new OpsException("Error sending file to image store", e);
						}
					}
				}

				Md5Hash targetHash = target.getFileHash(targetImageFile);
				Md5Hash srcHash = srcImageHost.getFileHash(srcImageFile);

				if (Objects.equal(srcHash, targetHash)) {
					break;
				} else {
					target.rm(targetImageFile);
					if (attempt != maxAttempts) {
						log.warn("Files did not match after transfer");
					} else {
						throw new OpsException("Files did not match after transfer");
					}
				}

				if (serveFuture.isDone()) {
					// This is interesting for debug purposes; otherwise not very useful
					try {
						ProcessExecution serveExecution = serveFuture.get();
						log.warn("Serving process exited: " + serveExecution);
					} catch (ExecutionException e) {
						throw new OpsException("Error sending file to image store", e);
					} catch (InterruptedException e) {
						ExceptionUtils.handleInterrupted(e);
						throw new OpsException("Error sending file to image store", e);
					}
				}
			}
		}

		fileStore.writeProperties(imageId, properties);

		updateImageTags(imageId, tags);

		return imageId;
	}

	private File getImageFile(String imageId, Properties imageProperties) {
		String imageLocation = null;
		// if (imageProperties != null) {
		// imageLocation = imageProperties.getProperty("org.openstack.sync__1__image");
		// }
		if (imageLocation == null) {
			imageLocation = imageId + ".image";
		}
		// TODO: Better sanity checking of paths
		if (imageLocation.contains("/") || imageLocation.contains("..")) {
			throw new IllegalStateException();
		}
		return new File(getImagesDir(), imageLocation);
	}

	@Override
	public void bringToMachine(String imageId, OpsTarget destination, File destinationPath) throws OpsException {
		Properties imageProperties = fileStore.readProperties(imageId);
		if (imageProperties == null) {
			throw new OpsException("Image not found: " + imageId);
		}

		File imageFile = getImageFile(imageId, imageProperties);

		if (destination.isSameMachine(target)) {
			Command copyCommand = Command.build("cp {0} {1}", imageFile, destinationPath);
			destination.executeCommand(copyCommand.setTimeout(TimeSpan.FIVE_MINUTES));
		} else {
			throw new OpsException("SCPing images between machines not yet implemented");
		}
	}
}
