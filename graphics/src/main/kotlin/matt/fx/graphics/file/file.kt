package matt.fx.graphics.file

import matt.caching.cache.LRUCache
import matt.collect.dmap.withStoringDefault
import matt.file.MFile
import matt.file.thismachine.thisMachine
import matt.fx.image.jswingIconToImage
import matt.fx.image.toBufferedImage
import matt.model.code.sys.GAMING_WINDOWS
import java.awt.image.BufferedImage
import javax.swing.JFileChooser
import javax.swing.filechooser.FileSystemView

@Deprecated("looks bad, slow, and buggy. Use my own icons.")
val fileIcons = LRUCache<MFile, BufferedImage>(500).withStoringDefault { f ->

  if (thisMachine is GAMING_WINDOWS) jswingIconToImage(
	FileSystemView.getFileSystemView().getSystemIcon(f)
  )!!.toBufferedImage() else {
	val icon = JFileChooser().let { it.ui.getFileView(it) }.getIcon(f)!!
	val bi = BufferedImage(
	  icon.iconWidth, icon.iconHeight, BufferedImage.TYPE_INT_ARGB
	)
	icon.paintIcon(null, bi.graphics, 0, 0)
	bi
  }


}
