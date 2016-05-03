package juztoss.com.bpmplayer.views;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.view.MenuItem;

import juztoss.com.bpmplayer.R;
import juztoss.com.bpmplayer.presenters.BPMPlayerApp;

public class MainActivity extends Activity {
    private DrawerArrowDrawable mHamburger;
    private BPMPlayerApp mApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApp = (BPMPlayerApp) getApplication();
        setContentView(R.layout.decor);

        BrowserFragment explorerView = (BrowserFragment) getFragmentManager().findFragmentById(R.id.file_tree);
        mApp.getFileTreePresenter().init(explorerView);
        explorerView.init(mApp.getFileTreePresenter());

        PlayerFragment playerFragment = (PlayerFragment) getFragmentManager().findFragmentById(R.id.drawer_content);
        mApp.getPlayerPresenter().init(playerFragment);
        playerFragment.init(mApp.getPlayerPresenter());


        //Add hamburger
        mHamburger = new DrawerArrowDrawable(this);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeAsUpIndicator(mHamburger);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.openDrawer(GravityCompat.START);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
