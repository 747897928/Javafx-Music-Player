package cn.gxust.ui;

import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Region;
import javafx.scene.paint.Paint;
import javafx.scene.shape.SVGPath;

/**
 * <p>description:  </p>
 * <p>create:  2020/11/26 14:04</p>
 * @author zhaoyijie(AquariusGenius)
 */
 
public class VoiceSvg {

     private final SVGPath voicenSvgPath; /* 有声音*/

     private final SVGPath voiceZeroSvgPath; /* 静音*/

     private final Region voiceRegion;

     public VoiceSvg() {
          this.voicenSvgPath = new SVGPath();
          this.voicenSvgPath.setContent("M512 23.12192c-269.99808 0-488.87808 218.88-488.87808 488.87296 0 129.43872 50.304 247.12192 132.42368 334.57152 89.18016 94.98624 215.8848 154.3168 356.4544 154.3168 140.55936 0 267.264-59.33056 356.4544-154.3168 82.11456-87.4496 132.42368-205.12768 132.42368-334.57152 0-269.99808-218.88-488.87296-488.87808-488.87296z m27.02848 714.12224c0 55.68-42.7008 26.9312-42.7008 26.9312l-184.91392-155.5712h-28.55936c-17.69472 0-32.0256-14.60736-32.0256-32.60928V445.60896c0-18.01216 14.33088-32.59904 32.0256-32.59904h34.16064l179.31264-152.12032s42.7008-28.9792 42.7008 27.1616v449.19296z m62.76608-159.0784c49.81248-46.17728 0-108.65664 0-108.65664s12.4672-38.93248 32.02048-27.16672c49.81248 37.12 53.38112 119.5264 0 162.98496-23.12704 11.77088-41.79968-14.4896-32.02048-27.1616z m59.98592 106.48576c-16.01536 6.34368-37.36064-22.64064-21.34528-38.03648 107.62752-92.34944 105.85088-176.55808 0-266.20928-20.4544-20.82304 14.2336-44.36992 21.34528-38.02624 140.54912 63.38048 146.75968 274.34496 0 342.272z");
          this.voiceZeroSvgPath = new SVGPath();
          this.voiceZeroSvgPath.setContent("M512 0c282.76736 0 512 229.23264 512 512S794.76736 1024 512 1024 0 794.76736 0 512 229.23264 0 512 0z m-17.63328 307.2l-124.3136 79.62624v250.34752L494.3872 716.8c19.59936 0 35.55328-15.2576 35.55328-34.16064V341.2992c-0.08192-18.8416-15.95392-34.0992-35.55328-34.0992z m-159.78496 102.35904h-53.28896c-19.59936 0-35.5328 15.33952-35.5328 34.18112v136.51968c0 18.8416 15.872 34.18112 35.5328 34.18112h53.28896V409.55904z m235.95008 4.95616c-7.0656 6.79936-6.3488 18.5344 1.67936 26.25536L646.30784 512l-74.09664 71.22944c-7.9872 7.65952-8.74496 19.456-1.67936 26.25536 7.0656 6.79936 19.27168 6.12352 27.32032-1.59744l74.09664-71.22944 74.11712 71.22944c7.96672 7.65952 20.23424 8.3968 27.32032 1.59744 7.0656-6.79936 6.3488-18.5344-1.67936-26.25536L697.52832 512l74.11712-71.22944c7.96672-7.65952 8.74496-19.456 1.65888-26.25536-7.0656-6.79936-19.27168-6.12352-27.29984 1.59744l-74.11712 71.22944-74.0352-71.22944c-7.9872-7.72096-20.25472-8.3968-27.32032-1.59744z");
          this.voiceRegion = new Region();
     }

     public void changeSvgPath(VoiceStatus voiceStatus) {
          if (VoiceStatus.VOICE_N == voiceStatus) {
               voiceRegion.setShape(voicenSvgPath);
          } else if (VoiceStatus.VOICE_ZERO == voiceStatus) {
               voiceRegion.setShape(voiceZeroSvgPath);
          }
     }

     public Region getVoiceRegion(double wh, Paint paint) {
          voiceRegion.setShape(voicenSvgPath);
          voiceRegion.setMinSize(wh, wh);
          voiceRegion.setPrefSize(wh, wh);
          voiceRegion.setMaxSize(wh, wh);
          voiceRegion.setBackground(new Background(new BackgroundFill(paint, null, null)));
          return voiceRegion;
     }
     public enum VoiceStatus {
          VOICE_N,/* 有声音*/
          VOICE_ZERO; /* |静音*/
     }
}

