#!/bin/bash
set -o errexit

mkdir images
cd images

MNTDIR="${BASEDIR}/mntimage"
IMGFILE=ubuntu_x64_8g.raw
MIRROR=ftp.utexas.edu

# Just shy of 8Gig (so we don't have to worry about exact fit)
# Note this is a sparse file
dd if=/dev/null bs=1M seek=8180 of=${IMGFILE}
yes | mkfs.ext3 ${IMGFILE}
mkdir ${MNTDIR}
mount -o loop ${IMGFILE}  ${MNTDIR}
debootstrap --include=openssh-server,grub,linux-image-server lucid ${MNTDIR} http://127.0.0.1:3142/${MIRROR}/ubuntu
echo "openstack" > ${MNTDIR}/etc/hostname
echo "deb http://us.archive.ubuntu.com/ubuntu lucid main" > ${MNTDIR}/etc/apt/sources.list


mount -t proc proc ${MNTDIR}/proc
chroot ${MNTDIR}
apt-get update
locale-gen en_US.utf8
dpkg-reconfigure locales
#apt-get install linux-image-server
#apt-get install openssh-server
apt-get clean
exit
umount ${MNTDIR}/proc
umount ${MNTDIR}

gzip -c ${IMGFILE} > ${IMGFILE}.gz
