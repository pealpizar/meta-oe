SUMMARY = "PackageKit package management abstraction"
SECTION = "libs"
LICENSE = "GPL-2.0+"
LIC_FILES_CHKSUM = "file://COPYING;md5=94d55d512a9ba36caa9b7df079bae19f"

DEPENDS = "python polkit dbus dbus-glib glib-2.0 sqlite3 intltool intltool-native apt python-smartpm opkg"

inherit autotools gtk-doc pkgconfig pythonnative

PACKAGES =+ "${PN}-python ${PN}-backend-smart ${PN}-backend-opkg ${PN}-backend-apt"
PACKAGES_DYNAMIC += "^packagekit-plugin.* ^packagekit-backend.*"

SRC_URI = "http://www.packagekit.org/releases/PackageKit-${PV}.tar.xz"

SRC_URI[md5sum] = "c8f7207cca4fcdb3d62d012b67c2f319"
SRC_URI[sha256sum] = "110da1afcbfb9d56da18ece3161e8554f77bc3f90793332406ca54129ec43c76"

S = "${WORKDIR}/PackageKit-${PV}"

EXTRA_OECONF = " \
  --with-security-framework=dummy \
  --enable-apt \
  --enable-opkg \
  --enable-smart \
  --disable-tests \
  --disable-cron \
  --disable-connman \
  --disable-strict \
  --disable-systemd \
  --disable-systemd-updates \
  --disable-bash-completion \
  --disable-gstreamer-plugin \
  --disable-local  \
  --disable-networkmanager \
  --disable-gtk-module \
  --disable-browser-plugin \
  --disable-python3 \
  ac_cv_path_XMLTO=no \
"

FILES_${PN}-python = "${libdir}/python*"
RDEPENDS_${PN}-python = "python"

FILES_${PN} += " \
  ${datadir}/dbus-1 \
  ${datadir}/PackageKit \
  ${datadir}/mime \
"
FILES_${PN}-dbg += "${libdir}/packagekit-backend/.debug/*.so ${libdir}/packagekit-plugins/.debug/*.so"
FILES_${PN}-dev += "${libdir}/packagekit-backend/*.la ${libdir}/packagekit-plugins/*.la"
FILES_${PN}-staticdev += "${libdir}/packagekit-backend/*.a ${libdir}/packagekit-plugins/*.a"

FILES_${PN}-backend-smart += "${datadir}/PackageKit/helpers/smart"
RDEPENDS_${PN}-backend-smart += "${PN} ${PN}-python smartpm"

RDEPENDS_${PN}-backend-apt += "${PN} apt"
RDEPENDS_${PN}-backend-opkg += "${PN} opkg"

python populate_packages_prepend() {
    backenddir = d.expand('${libdir}/packagekit-backend/')
    do_split_packages(d, backenddir, '^libpk_backend_(.*)\.so$', 'packagekit-backend-%s', 'PackageKit backend for %s', extra_depends='', prepend=True)
    plugindir = d.expand('${libdir}/packagekit-plugins/')
    do_split_packages(d, plugindir, '^libpk_plugin[_\-](.*)\.so$', 'packagekit-plugin-%s', 'PackageKit plugin for %s', extra_depends='', prepend=True)
}
