/*
 * Copyright 2012-2013, webappbooster.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.webappbooster.plugin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class OAuthActivity extends Activity {

    private String redirectUri;

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith(redirectUri)) {
                Intent i = new Intent();
                i.putExtra("uri", url);
                setResult(RESULT_OK, i);
                finish();
                return true;
            }
            return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        String uri = i.getExtras().getString("uri");
        redirectUri = i.getExtras().getString("redirectUri");
        WebView v = new WebView(this);
        v.getSettings().setJavaScriptEnabled(true);
        v.setWebViewClient(new MyWebViewClient());
        v.loadUrl(uri);
        setContentView(v);
    }
}
