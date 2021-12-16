package com.td.recommend.video.rank.model;

import com.td.feature.process.FtrlFeatureProcess;
import com.td.feature.process.FtrlFeatureProcessFactory;
import com.td.recommend.commons.io.FileChangeSubject;
import com.td.recommend.commons.io.FileChangedEvent;
import com.td.recommend.commons.io.FileChangedListener;
import com.td.recommend.commons.rank.model.FtrlModel;
import com.td.recommend.video.utils.UserVideoConfig;
import com.typesafe.config.Config;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

@Setter
@Getter
public class LocalFtrlModel {
    private static final Logger LOG = LoggerFactory.getLogger(LocalFtrlModel.class);
    public static final LocalFtrlModel ftrl_base = new LocalFtrlModel("ftrl-base");

    private String modelFile;
    private FtrlFeatureProcess ftrlFeatureProcess;
    private FtrlModel ftrlModel ;
    private FileChangeSubject fileChangeSubject;

    //private Ftrl

    private LocalFtrlModel(String strategy) {
        long start = System.currentTimeMillis();
        try {
            Config userNewsConfig = UserVideoConfig.getInstance().getAppConfig();
            this.modelFile = userNewsConfig.getString(strategy);
            ftrlFeatureProcess = FtrlFeatureProcessFactory.create(strategy);
            ftrlModel = new FtrlModel(this.modelFile);


            File file = new File((this.modelFile));
            String watchDir = file.getParent();
            String watchFile = file.getName();
            fileChangeSubject = new FileChangeSubject(watchDir, watchFile);
            fileChangeSubject.addListener(new ReloadModelListener());

        }catch (Exception ex) {
            LOG.error("Create model from {} failed!", strategy, ex);
        }
    }



    public void reloadModeFile(String strategy) {
        long start = System.currentTimeMillis();
        try {
            Config userNewsConfig = UserVideoConfig.getInstance().getAppConfig();
            this.modelFile = userNewsConfig.getString(strategy);
            ftrlFeatureProcess = FtrlFeatureProcessFactory.create(strategy);
            ftrlModel = new FtrlModel(this.modelFile);


            File file = new File((this.modelFile));
            String watchDir = file.getParent();
            String watchFile = file.getName();
            fileChangeSubject = new FileChangeSubject(watchDir, watchFile);
            fileChangeSubject.addListener(new ReloadModelListener());

        }catch (Exception ex) {
            LOG.error("Create model from {} failed!", strategy, ex);
        }
    }

    private class ReloadModelListener implements FileChangedListener {
        @Override
        public void onChanged(FileChangedEvent event) {
            String fileName = event.getFileName();
            LOG.warn("ftrl File name={} has changed, begin reload model...", fileName);

            boolean succ = ftrlModel.reloadFtrlMode(fileName);

            if (succ) {
                LOG.warn("Successful load dnn model from {}.", modelFile);
            }else {
                LOG.warn("Fail load dnn model from {}.", modelFile);
            }
        }
    }
}
