package com.example.xyzreader.ui.activities;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.example.xyzreader.utils.ImageLoaderHelper;

/**
 * An activity representing a single Article detail screen.
 */
public class ArticleDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String TAG = ArticleDetailActivity.class.getSimpleName();

    private Cursor mCursor;
    private long mArticleId;
    private Toolbar mToolbar;
    private FloatingActionButton mFloatingActionButton;
    private CollapsingToolbarLayout mCollapsingToolbar;
    private ImageView mArticleImage;
    private TextView mArticleSubtitle, mArticleBody;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                mArticleId = ItemsContract.Items.getItemId(getIntent().getData());
            }
        }
        initComponents();
        getLoaderManager().initLoader(0, null, this);
    }

    private void initComponents() {
        mArticleImage = (ImageView) findViewById(R.id.article_image);

        mArticleSubtitle = (TextView) findViewById(R.id.article_subtitle);
        mArticleSubtitle.setMovementMethod(new LinkMovementMethod());

        mArticleBody = (TextView) findViewById(R.id.article_body);
        mArticleBody.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf"));

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mCollapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);

        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.action_share);
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.");
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }
        });
    }

    private void syncUi() {
        if (mCursor != null) {
            mArticleId = mCursor.getLong(ArticleLoader.Query._ID);
            mCollapsingToolbar.setTitle(mCursor.getString(ArticleLoader.Query.TITLE));
            mArticleSubtitle.setText(Html.fromHtml("<i>" +
                    DateUtils.getRelativeTimeSpanString(
                            mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + " by <b>"
                            + mCursor.getString(ArticleLoader.Query.AUTHOR)
                            + "</b> </i>"));
            mArticleBody.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY)));
            ImageLoaderHelper.getInstance(this).getImageLoader()
                    .get(mCursor.getString(ArticleLoader.Query.PHOTO_URL), new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                            Bitmap bitmap = imageContainer.getBitmap();
                            if (bitmap != null) {
                                mArticleImage.setImageBitmap(bitmap);
                                mArticleImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            }
                        }

                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            Log.e(TAG, volleyError.getMessage());
                        }
                    });
        } else {
            Toast.makeText(this, getString(R.string.toast_error_cursor), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(this, mArticleId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (cursor == null) {
            Log.e(TAG, "onLoadFinished() -> Cursor is null.");
            return;
        }

        mCursor = cursor;
        if (!mCursor.moveToFirst()) {
            Log.e(TAG, "onLoadFinished() -> Error reading item detail cursor");
            mCursor.close();
            return;
        }

        syncUi();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
    }
}
