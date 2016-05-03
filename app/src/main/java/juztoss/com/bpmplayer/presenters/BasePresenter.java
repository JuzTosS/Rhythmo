package juztoss.com.bpmplayer.presenters;

/**
 * Created by JuzTosS on 4/25/2016.
 */
public class BasePresenter {

    private BPMPlayerApp mApp;

    private BasePresenter() {
    }

    public BasePresenter(BPMPlayerApp app) {
        mApp = app;
    }

    public BPMPlayerApp getApp() {
        return mApp;
    }
}
