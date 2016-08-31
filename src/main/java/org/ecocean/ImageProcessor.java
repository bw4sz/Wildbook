/*
 * The Shepherd Project - A Mark-Recapture Framework
 * Copyright (C) 2011 Jason Holmberg
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.ecocean;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;

import java.util.Arrays;
import javax.imageio.*;
import java.awt.image.BufferedImage;
//import java.awt.image.*;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Does actual comparison processing of batch-uploaded images.
 *
 * @author Jon Van Oast
 */
public final class ImageProcessor implements Runnable {
    private static Logger log = LoggerFactory.getLogger(ImageProcessor.class);

//  /** Enumeration representing possible status values for the batch processor. */
//  public enum Status { WAITING, INIT, RUNNING, FINISHED, ERROR };
//  /** Enumeration representing possible processing phases. */
//  public enum Phase { NONE, MEDIA_DOWNLOAD, PERSISTENCE, THUMBNAILS, PLUGIN, DONE };
//  /** Current status of the batch processor. */
//  private Status status = Status.WAITING;
//  /** Current phase of the batch processor. */
//  private Phase phase = Phase.NONE;
//  /** Throwable instance produced by the batch processor (if any). */
//  private Throwable thrown;
  
    private String context = "context0";
    private String command = null;
    private String imageSourcePath = null;
    private String imageTargetPath = null;
    private String arg = null;
    private int width = 0;
    private int height = 0;
    private float[] transform = new float[0];


  public ImageProcessor(String context, String action, int width, int height, String imageSourcePath, String imageTargetPath, String arg) {
        this.context = context;
        this.width = width;
        this.height = height;
        this.imageSourcePath = imageSourcePath;
        this.imageTargetPath = imageTargetPath;
        this.arg = arg;
        if ((action != null) && action.equals("watermark")) {
            this.command = CommonConfiguration.getProperty("imageWatermarkCommand", this.context);
        } else {
            this.command = CommonConfiguration.getProperty("imageResizeCommand", this.context);
        }
    }

    //no need for action when passing a transform, as it can only be one
    public ImageProcessor(String context, String imageSourcePath, String imageTargetPath, float w, float h, float[] transform) {
        this.context = context;
        this.imageSourcePath = imageSourcePath;
        this.imageTargetPath = imageTargetPath;
        this.width = Math.round(w);
        this.height = Math.round(h);
        this.transform = transform;
        this.command = CommonConfiguration.getProperty("imageTransformCommand", this.context);
    }

    //the crop-only version of transforming; only takes x,y,w,h
    public ImageProcessor(String context, String imageSourcePath, String imageTargetPath, float x, float y, float w, float h) {
        this.context = context;
        this.imageSourcePath = imageSourcePath;
        this.imageTargetPath = imageTargetPath;
        this.width = Math.round(w);
        this.height = Math.round(h);
        this.transform = new float[6];
        this.transform[0] = 1;
        this.transform[1] = 0;
        this.transform[2] = 0;
        this.transform[3] = 1;
        this.transform[4] = x;
        this.transform[5] = y;
        this.command = CommonConfiguration.getProperty("imageTransformCommand", this.context);
    }


    public void run()
    {
//        status = Status.INIT;

        if (StringUtils.isBlank(this.command)) {
            log.warn("Can't run processor due to empty command");
            return;
        }
        
        if (StringUtils.isBlank(this.imageSourcePath)) {
            log.warn("Can't run processor due to empty source path");
            return;            
        }
        
        if (StringUtils.isBlank(this.imageTargetPath)) {
            log.warn("Can't run processor due to empty target path");
            return;            
        }

        String fullCommand;
        fullCommand = this.command.replaceAll("%width", Integer.toString(this.width))
                                  .replaceAll("%height", Integer.toString(this.height))
                                  //.replaceAll("%imagesource", this.imageSourcePath)
                                  //.replaceAll("%imagetarget", this.imageTargetPath)
                                  .replaceAll("%arg", this.arg);

        //walk thru transform array and replace "tN" with transform[N]
        if (this.transform.length > 0) {
            for (int i = 0 ; i < this.transform.length ; i++) {
                fullCommand = fullCommand.replaceAll("%t" + Integer.toString(i), Float.toString(this.transform[i]));
            }
        }

        String[] command = fullCommand.split("\\s+");

        //we have to do this *after* the split-on-space cuz files may have spaces!
        for (int i = 0 ; i < command.length ; i++) {
            if (command[i].equals("%imagesource")) command[i] = this.imageSourcePath;
            if (command[i].equals("%imagetarget")) command[i] = this.imageTargetPath;
System.out.println("COMMAND[" + i + "] = (" + command[i] + ")");
        }
//System.out.println("done run()");
//System.out.println("command = " + Arrays.asList(command).toString());

        ProcessBuilder pb = new ProcessBuilder();
        pb.command(command);
/*
        Map<String, String> env = pb.environment();
        env.put("LD_LIBRARY_PATH", "/home/jon/opencv2.4.7");
*/
//System.out.println("before!");

        try {
            Process proc = pb.start();
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            String line;
            while ((line = stdInput.readLine()) != null) {
                System.out.println(">>>> " + line);
            }
            while ((line = stdError.readLine()) != null) {
                System.out.println("!!!! " + line);
            }
            proc.waitFor();
System.out.println("DONE?????");
            ////int returnCode = p.exitValue();
        } catch (Exception ioe) {
            log.error("Trouble running processor [" + command + "]", ioe);
        }
System.out.println("RETURN");
    }
}
