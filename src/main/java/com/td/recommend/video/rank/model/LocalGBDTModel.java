package com.td.recommend.video.rank.model;

import com.td.recommend.commons.io.FileChangeSubject;
import com.td.recommend.commons.io.FileChangedEvent;
import com.td.recommend.commons.io.FileChangedListener;
import com.typesafe.config.Config;
import com.td.recommend.commons.rank.model.GBDTModel2;
import com.td.recommend.video.utils.UserVideoConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Created by admin on 2017/8/1.
 */
public class LocalGBDTModel {
    private static final Logger LOG = LoggerFactory.getLogger(LocalGBDTModel.class);

    private static LocalGBDTModel instance = new LocalGBDTModel();
    private volatile GBDTModel2 gbdtModel;
    private String modelFile;
    private FileChangeSubject fileChangeSubject;


    public static LocalGBDTModel getInstance() {
        return instance;
    }

    private LocalGBDTModel() {
        Config userNewsConfig = UserVideoConfig.getInstance().getAppConfig();
        this.modelFile = userNewsConfig.getString("model-file");
//        this.featureFile = modelFile + ".vocab";
        File mf = new File(this.modelFile);
        if (mf.exists()) {
            gbdtModel = new GBDTModel2(modelFile);
        }
        File file = new File((modelFile));
        String watchDir = file.getParent();
        String watchFile = file.getName();
        try {
            fileChangeSubject = new FileChangeSubject(watchDir, watchFile);
            fileChangeSubject.addListener(new LocalGBDTModel.ReloadModelListener());
        } catch (IOException e) {
            LOG.error("Create FileChangeSubject failed!", e);
        }

    }

    public GBDTModel2 getGBDTModel() {
        return gbdtModel;
    }

    private class ReloadModelListener implements FileChangedListener {

        @Override
        public void onChanged(FileChangedEvent event) {
            String fileName = event.getFileName();
            LOG.warn("File name={} has changed, begin reload model...", fileName);
            gbdtModel = new GBDTModel2(modelFile);
            LOG.warn("Successful load gbdt model.");
        }
    }

}
