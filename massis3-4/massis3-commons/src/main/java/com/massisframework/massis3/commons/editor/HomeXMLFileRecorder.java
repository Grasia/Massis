/*
 * HomeXMLFileRecorder.java 
 *
 * Copyright (c) 2016 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.massisframework.massis3.commons.editor;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.ImageInputStream;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;
import javax.media.j3d.Node;

import com.eteks.sweethome3d.io.ContentRecording;
import com.eteks.sweethome3d.io.DefaultHomeInputStream;
import com.eteks.sweethome3d.io.DefaultHomeOutputStream;
import com.eteks.sweethome3d.io.XMLWriter;
import com.eteks.sweethome3d.j3d.OBJWriter;
import com.eteks.sweethome3d.j3d.Object3DBranchFactory;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeObject;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.HomeRecorder;
import com.eteks.sweethome3d.model.HomeTexture;
import com.eteks.sweethome3d.model.InterruptedRecorderException;
import com.eteks.sweethome3d.model.Level;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.Wall;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.tools.URLContent;
import com.eteks.sweethome3d.viewcontroller.Object3DFactory;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

/**
 * @formatter:off
 */
public class HomeXMLFileRecorder implements HomeRecorder {
  public static final int INCLUDE_VIEWER_DATA          = 0x0001;
  public static final int INCLUDE_HOME_STRUCTURE       = 0x0002;
  public static final int INCLUDE_ICONS                = 0x0004;
  public static final int CONVERT_MODELS_TO_OBJ_FORMAT = 0x0008;  
  public static final int REDUCE_IMAGES                = 0x0010;

  private final int compressionLevel;  
  private final int flags;
  private final int imageMaxPreferredSize;
  
  public HomeXMLFileRecorder(final int compressionLevel, final int flags) {
    this(compressionLevel, flags, 32);
  }
  
  public HomeXMLFileRecorder(final int compressionLevel, final int flags, final int imageMaxPreferredSize) {
    this.compressionLevel = compressionLevel;
    this.flags = flags;
    this.imageMaxPreferredSize = imageMaxPreferredSize;
  }

  @Override
public void writeHome(final Home home, final String exportedFileName) throws RecorderException {
    File homeFile = null;
    try {
      // Save home in a temporary file to ensure all items are gathered  
      homeFile = OperatingSystem.createTemporaryFile("Home", ".sh3d");
      final DefaultHomeOutputStream out = new DefaultHomeOutputStream(new FileOutputStream(homeFile), 0, false);
      out.writeHome(home);
      out.close();
      
      exportHome(homeFile, new File(exportedFileName), null);
    } catch (final InterruptedIOException ex) {
      throw new InterruptedRecorderException("Save home to XML");
    } catch (final IOException ex) {
      throw new RecorderException("Couldn't save home to XML", ex);
    } finally {
      if (homeFile != null) {
        homeFile.delete();
      }
    }
  }
    
  public void exportHome(final File homeFile, final File exportedFile, final UserPreferences preferences) throws RecorderException {
    DefaultHomeInputStream in = null;
    Home home;
    try {
      // If preferences are not null replace home content by the one in preferences when it's the same
      in = new DefaultHomeInputStream(homeFile, 
          ContentRecording.INCLUDE_ALL_CONTENT, null, preferences, preferences != null);
      home = in.readHome();
      //XXX MASSIS MODS: uuids & sha1
      home.setProperty(MASSIS_SHA1_KEY, Files.hash(homeFile, Hashing.sha1()).toString());
      generateIds(home);
      addPointsAsPropertyWalls(home);
    } catch (final InterruptedIOException ex) {
      throw new InterruptedRecorderException("Save home to XML");
    } catch (final IOException ex) {
      throw new RecorderException("Couldn't read exported home to XML", ex);
    } catch (final ClassNotFoundException ex) {
      // Shouldn't happen
      throw new RecorderException("Couldn't read exported home to XML", ex);
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (final IOException ex) {
          ex.printStackTrace();
        }
      }
    }

    File homeStructureFile = null;
    ZipOutputStream zipOut = null;
    try {
      String homeStructure;
      if ((this.flags & INCLUDE_HOME_STRUCTURE) != 0) {
        // Export home structure in a zipped OBJ file
        homeStructure = "HomeStructure/Home.obj";
        homeStructureFile = exportHomeStructure(home, new Object3DBranchFactory(), 
            homeStructure.substring(homeStructure.lastIndexOf('/') + 1));
      } else {
        homeStructure = null;
      }
      
      zipOut = new ZipOutputStream(new FileOutputStream(exportedFile));
      zipOut.setLevel(this.compressionLevel);
      // Export home to XML
      zipOut.putNextEntry(new ZipEntry("Home.xml"));
      final XMLWriter writer = new XMLWriter(zipOut);
      String homeName = null;
      if (home.getName() != null) {
        homeName = new File(home.getName()).getName();
      }   
      final Set<Content> referencedContents = writeHomeToXML(writer, home, homeName, homeStructure, this.flags);
      writer.flush();
      zipOut.closeEntry();
              
      if ((this.flags & INCLUDE_HOME_STRUCTURE) != 0) {
        // Save Home.obj structure and its dependencies in HomeStructure directory
        writeAllZipEntries(zipOut, homeStructure.substring(0, homeStructure.lastIndexOf('/')), homeStructureFile.toURI().toURL(), this.flags);
        // Save content referenced home XML entry
        final List<String> homeFileEntries = new ArrayList<String>();
        for (final Content content : referencedContents) {
          if (content instanceof RedirectedURLContent) {
            String directoryName = ((RedirectedURLContent)content).getJAREntryName();
            directoryName = directoryName.substring(0, directoryName.indexOf('/'));
            writeAllZipEntries(zipOut, directoryName, ((RedirectedURLContent)content).getTargetContent().getJAREntryURL(), this.flags);
          } else if (content instanceof URLContent) {
            final HomeTexture skyTexture = home.getEnvironment().getSkyTexture();
            if (skyTexture != null && skyTexture.getImage().equals(content)) {
              // Reduce less sky texture image 
              writeContentZipEntries(zipOut, (URLContent)content, homeFileEntries, this.flags, this.imageMaxPreferredSize * 4);
            } else {
              writeContentZipEntries(zipOut, (URLContent)content, homeFileEntries, this.flags, this.imageMaxPreferredSize);
            }
          }
        }
      } else {
        // Just copy all entries taking into account export flags
        writeAllZipEntries(zipOut, "", homeFile.toURI().toURL(), this.flags);
      }
      zipOut.finish();
    } catch (final InterruptedIOException ex) {
      throw new InterruptedRecorderException("Save home to XML");
    } catch (final IOException ex) {
      throw new RecorderException("Couldn't save home to XML", ex);
    } finally {
      if (homeStructureFile != null) {
        homeStructureFile.delete();
      }
      
      if (zipOut != null) {
        try {
          zipOut.close();
        } catch (final IOException ex) {
          throw new RecorderException("Couldn't close home file", ex);
        }
      }
    }
  }
  

/**
   * Writes the given <code>home</code> in XML and returns the content that is required by this home.
   */
  protected Set<Content> writeHomeToXML(final XMLWriter writer, final Home home, final String homeName, final String homeStructure, final int flags) throws IOException {
    final HomeXMLOptionalExporter homeExporter = new HomeXMLOptionalExporter(home, homeName, homeStructure, flags);
    homeExporter.writeElement(writer, home);
    return homeExporter.getReferencedContents();
  }

  /**
   * Exports the structure of the given <code>home</code> at OBJ format 
   * and returns the temporary zip file where it's stored. 
   */
  private File exportHomeStructure(Home home, final Object3DFactory objectFactory, 
                                   final String homeStructureObjName) throws IOException {
    // Clone home to be able to handle it independently
    home = home.clone();
    final List<Level> levels = home.getLevels();
    for (int i = 0; i < levels.size(); i++) {
      if (levels.get(i).isViewable()) {
        levels.get(i).setVisible(true);
      }
    }
    
    final BranchGroup root = new BranchGroup();
    // Add 3D ground, walls, rooms and labels
    //XXX MASSIS MODS: commented
    //root.addChild(new Ground3D(home, -0.5E5f, -0.5E5f, 1E5f, 1E5f, true));
    
    for (final Selectable item : home.getSelectableViewableItems()) {
      if (!(item instanceof HomePieceOfFurniture)) {
        //XXX MASSIS MODS: Changed, set object name uid
    	// root.addChild((Node)objectFactory.createObject3D(home, item, true));
    	final Node node = (Node)objectFactory.createObject3D(home, item, true);
    	if (item instanceof HomeObject && node!=null) {
    		final String mid=getMassisID((HomeObject)item);
    		// iterate over node and all children and set userdata
    		dfs(node,n->{
    			n.setName(mid);
    			n.setUserData(mid);
    		});
    	}
    	root.addChild(node);
      }
    }
    final File tempZipFile = OperatingSystem.createTemporaryFile("HomeStructure", ".zip");
    OBJWriter.writeNodeInZIPFile(root, tempZipFile, 0, homeStructureObjName, "Home structure for HTML5 export");
    return tempZipFile;
  }

  /**
   * Writes in <code>zipOut</code> stream one or more entries matching the content
   * <code>content</code> coming from a home file.
   */
  private void writeContentZipEntries(final ZipOutputStream zipOut, final URLContent urlContent, 
                                      final List<String> homeFileEntries, final int exportFlags, final int imageMaxSize) throws IOException {
    final String entryName = urlContent.getJAREntryName();
    final int slashIndex = entryName.indexOf('/');
    // If content comes from a directory of a home file
    if (slashIndex > 0) {
      final URL zipUrl = urlContent.getJAREntryURL();
      final String entryDirectory = entryName.substring(0, slashIndex + 1);
      // Write in home stream each zipped stream entry that is stored in the same directory  
      for (final String zipEntryName : getZipUrlEntries(zipUrl, homeFileEntries)) {
        if (zipEntryName.startsWith(entryDirectory)) {
          final URLContent siblingContent = new URLContent(new URL("jar:" + zipUrl + "!/" 
              + URLEncoder.encode(zipEntryName, "UTF-8").replace("+", "%20")));
          writeZipEntry(zipOut, entryDirectory + zipEntryName.substring(slashIndex + 1), siblingContent, exportFlags, imageMaxSize);
        }
      }
    } else {
      writeZipEntry(zipOut, entryName, urlContent, exportFlags, imageMaxSize);
    }
  }

  /**
   * Writes in <code>zipOut</code> stream all the entries of the zipped <code>urlContent</code>.
   */
  private void writeAllZipEntries(final ZipOutputStream zipOut, 
                                  final String directory,
                                  final URL url, final int exportFlags) throws IOException {
    ZipInputStream zipIn = null;
    try {
      // Open zipped stream that contains urlContent
      zipIn = new ZipInputStream(url.openStream());
      // Write each zipped stream entry in zip stream 
      for (ZipEntry entry; (entry = zipIn.getNextEntry()) != null; ) {
        final String zipEntryName = entry.getName();
        final URLContent siblingContent = new URLContent(new URL("jar:" + url + "!/" 
            + URLEncoder.encode(zipEntryName, "UTF-8").replace("+", "%20")));
        writeZipEntry(zipOut, directory + "/" + zipEntryName, siblingContent, exportFlags, this.imageMaxPreferredSize);
      }
    } finally {
      if (zipIn != null) {
        zipIn.close();
      }
    }
  }

  /**
   * Writes in <code>zipOut</code> stream a new entry named <code>entryName</code> that 
   * contains a given <code>content</code>.
   */
  private void writeZipEntry(final ZipOutputStream zipOut, final String entryName, 
                             final URLContent content, final int exportFlags, final int imageMaxSize) throws IOException {
    final byte [] buffer = new byte [8192];
    InputStream contentIn = null;
    try {
      zipOut.putNextEntry(new ZipEntry(entryName));
      if ((exportFlags & REDUCE_IMAGES) != 0
          && content.isJAREntry()
          && (content.getJAREntryName().endsWith(".jpg")
              || content.getJAREntryName().endsWith(".png")
              || content.getJAREntryName().indexOf(".") == -1)) {
        // Get content
        final ByteArrayOutputStream contentOut = new ByteArrayOutputStream();
        contentIn = content.openStream();
        for (int size; (size = contentIn.read(buffer)) != -1; ) {
          contentOut.write(buffer, 0, size);
        }
        contentIn.close();
        final byte [] imageBytes = contentOut.toByteArray();

        contentIn = new ByteArrayInputStream(imageBytes);
        final ImageInputStream imageIn = ImageIO.createImageInputStream(new ByteArrayInputStream(imageBytes));
        for (final Iterator<ImageReader> it = ImageIO.getImageReaders(imageIn);
            it.hasNext(); ) {
          final ImageReader reader = it.next();
          if (reader != null) {
            reader.setInput(imageIn);
            final int minIndex = reader.getMinIndex();
            final ImageTypeSpecifier rawImageType = reader.getRawImageType(minIndex);
            final boolean opaqueImage = rawImageType == null || rawImageType.getColorModel().getTransparency() == Transparency.OPAQUE;
            // If image is larger than the max size or if it's a small opaque image not at JPEG format
            // (nothing to spare for JPEG small images and too much quality loss for small transparent images)
            if (reader.getWidth(minIndex) > imageMaxSize
                || reader.getHeight(minIndex) > imageMaxSize
                || !"JPEG".equalsIgnoreCase(reader.getFormatName())
                   && opaqueImage) {  
              // Compute reduced opaque image 
              final BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
              final BufferedImage reducedImage = new BufferedImage(
                  Math.min(image.getWidth(), imageMaxSize),  Math.min(image.getHeight(), imageMaxSize), 
                  opaqueImage // Avoid image.getType() otherwise color profile of PNG images won't work when saved at JPEG format
                      ? BufferedImage.TYPE_INT_RGB 
                      : BufferedImage.TYPE_INT_ARGB); 
              final Graphics2D g2D = (Graphics2D)reducedImage.getGraphics();
              g2D.drawImage(image.getScaledInstance(reducedImage.getWidth(), reducedImage.getHeight(), Image.SCALE_SMOOTH), 0, 0, null);
              g2D.dispose();
              final ByteArrayOutputStream out = new ByteArrayOutputStream();
              // Keep a favor for PNG for model textures and non opaque images 
              ImageIO.write(reducedImage, content.getURL().toString().endsWith(".png") || !opaqueImage ? "PNG" : "JPEG", out);
              final byte [] reducedImageBytes = out.toByteArray();
              // Use reduced image if it's 80% smaller
              if (reducedImageBytes.length < 0.8f * imageBytes.length) {
                contentIn = new ByteArrayInputStream(reducedImageBytes);
              }
            }
            // Stop iteration among readers
            break;
          }
        }        
      } else {
        contentIn = content.openStream();
      }
      
      // Write content
      int size; 
      while ((size = contentIn.read(buffer)) != -1) {
        zipOut.write(buffer, 0, size);
      }
      zipOut.closeEntry();  
    } finally {
      if (contentIn != null) {          
        contentIn.close();
      }
    }
  }

  /**
   * Returns the list of entries contained in <code>zipUrl</code>.
   */
  private List<String> getZipUrlEntries(final URL zipUrl, final List<String> zipUrlEntries) throws IOException {
    if (zipUrlEntries.isEmpty()) {
      ZipInputStream zipIn = null;
      try {
        // Search all entries of zip url
        zipIn = new ZipInputStream(zipUrl.openStream());
        for (ZipEntry entry; (entry = zipIn.getNextEntry()) != null; ) {
          zipUrlEntries.add(entry.getName());
        }
      } finally {
        if (zipIn != null) {
          zipIn.close();
        }
      }
    }
    return zipUrlEntries;
  }

  /**
   * Not supported.
   */
  @Override
public Home readHome(final String name) throws RecorderException {
    throw new UnsupportedOperationException("Unable to read XML files");
  }

  @Override
public boolean exists(final String name) throws RecorderException {
    return new File(name).exists();
  }
  /*
   * MASSIS modifications
   */
  /**
   * @formatter:on
   */
	private static final String MASSIS_GID_KEY = "MASSISGID";
	private static final String MASSIS_SHA1_KEY = "SHA1";

	private String getMassisID(final HomeObject ho)
	{
		if (ho.getProperty(MASSIS_GID_KEY) == null)
		{
			setMassisID(ho);
		}
		return ho.getProperty(MASSIS_GID_KEY);
	}

	private void setMassisID(final HomeObject ho)
	{
		ho.setProperty(MASSIS_GID_KEY,
				"MASSISGID" + UUID.randomUUID().toString().replace("-", ""));
	}

	private void generateIds(final Home home)
	{
		home.getLevels().forEach(this::setMassisID);
		home.getWalls().forEach(this::setMassisID);
		home.getRooms().forEach(this::setMassisID);
		home.getFurniture().forEach(this::setMassisID);
		home.getDimensionLines().forEach(this::setMassisID);
		home.getLabels().forEach(this::setMassisID);
		home.getPolylines().forEach(this::setMassisID);
	}

	private void addPointsAsPropertyWalls(final Home home)
	{
		for (final Wall w : home.getWalls())
		{
			final float[][] points = w.getPoints();
			w.setProperty("points", Arrays.deepToString(points));
		}

	}

	private void dfs(final Node n, final Consumer<Node> action)
	{
		if (n != null)
		{
			action.accept(n);
			if (n instanceof Group)
			{
				final Enumeration<Node> en = ((Group) n).getAllChildren();
				while (en.hasMoreElements())
				{
					dfs(en.nextElement(), action);
				}
			}
		}
	}

}