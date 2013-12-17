SUMMARY = "XBMC Media Center"

LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://LICENSE.GPL;md5=6eb631b6da7fdb01508a80213ffc35ff"

DEPENDS = "libusb1 libcec expat yajl gperf-native libxmu fribidi mpeg2dec samba fontconfig curl python libass libmodplug libmicrohttpd wavpack libmms cmake-native virtual/egl mysql5 sqlite3 libmms faad2 libcdio libpcre boost lzo enca avahi libsamplerate0 libxinerama libxrandr libxtst bzip2 jasper zip-native zlib libtinyxml libmad taglib libsdl-image-native"
#require recipes/egl/egl.inc

SRCREV = "32b1a5ef9e7f257a2559a3b766e85a55b22aec5f"
SRC_URI = "git://github.com/xbmc/xbmc.git;branch=frodo \
           file://0001-configure-don-t-run-python-distutils-to-find-STAGING.patch \
           file://0002-Revert-fixed-ios-Add-memory-barriers-to-atomic-Add-S.patch \
           file://0003-Revert-fixed-ios-Add-memory-barriers-to-cas-assembly.patch \
"

inherit autotools gettext python-dir

S = "${WORKDIR}/git"

# breaks compilation
CCACHE = ""

CACHED_CONFIGUREVARS += " \
    ac_cv_path_PYTHON="${STAGING_BINDIR_NATIVE}/python-native/python" \
"

PACKAGECONFIG ??= "${@base_contains('DISTRO_FEATURES', 'opengl', 'opengl', 'openglesv2', d)} sdl airplay ssh"
PACKAGECONFIG[opengl] = "--enable-gl,--enable-gles,glew"
PACKAGECONFIG[openglesv2] = "--enable-gles,--enable-gl,"
PACKAGECONFIG[sdl] = "--enable-sdl,--disable-sdl,libsdl-mixer libsdl-image"
PACKAGECONFIG[airplay] = "--enable-airplay,--disable-airplay,libplist"
PACKAGECONFIG[ssh] = "--enable-ssh,--disable-ssh,libssh"

EXTRA_OECONF = " \
    --disable-rxsx \
    --disable-rpath \
    --enable-libusb \
    --disable-optical-drive \
    --enable-external-libraries \
    --disable-external-ffmpeg \
    --with-arch=${TARGET_ARCH} \
    "

FULL_OPTIMIZATION_armv7a = "-fexpensive-optimizations -fomit-frame-pointer -O4 -ffast-math"
BUILD_OPTIMIZATION = "${FULL_OPTIMIZATION}"

EXTRA_OECONF_append_armv7a = "--cpu=cortex-a8"

# for python modules
export HOST_SYS
export BUILD_SYS
export STAGING_LIBDIR
export STAGING_INCDIR
export PYTHON_DIR

#Needed by TexturePacker to compile it using native sysroot
export TEXTUREPACKER_NATIVE_ROOT = "${STAGING_DIR_NATIVE}/usr"

do_configure() {
    sh bootstrap
    oe_runconf
}

PARALLEL_MAKE = ""

do_compile_prepend() {
    for i in $(find . -name "Makefile") ; do
        sed -i -e 's:I/usr/include:I${STAGING_INCDIR}:g' $i
    done

    for i in $(find . -name "*.mak*" -o    -name "Makefile") ; do
        sed -i -e 's:I/usr/include:I${STAGING_INCDIR}:g' -e 's:-rpath \$(libdir):-rpath ${libdir}:g' $i
    done
}

INSANE_SKIP_${PN} = "rpaths"

# on ARM architectures xbmc will use GLES which will make the regular wrapper fail, so start it directly
do_install_append_arm() {
    sed -i -e 's:Exec=xbmc:Exec=${libdir}/xbmc/xbmc.bin:g' ${D}${datadir}/applications/xbmc.desktop
}

FILES_${PN} += "${datadir}/xsessions ${datadir}/icons"
FILES_${PN}-dbg += "${libdir}/xbmc/.debug ${libdir}/xbmc/*/.debug ${libdir}/xbmc/*/*/.debug ${libdir}/xbmc/*/*/*/.debug"

# xbmc uses some kind of dlopen() method for libcec so we need to add it manually
# OpenGL builds need glxinfo, that's in mesa-demos
RRECOMMENDS_${PN}_append = " libcec \
                             python \
                             python-lang \
                             python-re \
                             python-netclient \
                             libcurl \
                             xdpyinfo \
                             ${@base_contains('DISTRO_FEATURES', 'opengl', 'mesa-demos', '', d)} \
"
RRECOMMENDS_${PN}_append_libc-glibc = " glibc-charmap-ibm850 glibc-gconv-ibm850"
