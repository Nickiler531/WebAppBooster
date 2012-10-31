package org.webappbooster;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class PermissionsDialog implements DialogInterface.OnClickListener {

    private Context                         context;
    private View                            popupView;
    private AlertDialog.Builder             builder;
    private DialogInterface.OnClickListener listener;

    public PermissionsDialog(Context context) {
        this.context = context;
        builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.permissions);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        popupView = inflater.inflate(R.layout.permissions, null, false);
        builder.setView(popupView);
    }

    public void showPermissions(String url, String[] permissions) {
        setPermissions(R.string.show_permissions, url, permissions);
        builder.setPositiveButton(R.string.ok, this);
        if (permissions.length > 0) {
            builder.setNegativeButton(R.string.revoke, this);
        }
    }

    public void requestPermissions(String url, String[] permissions) {
        setPermissions(R.string.request_permissions, url, permissions);
        builder.setPositiveButton(R.string.allow_always, this);
        builder.setNeutralButton(R.string.allow_once, this);
        builder.setNegativeButton(R.string.reject, this);
    }

    private void setPermissions(int titleId, String url, String[] permissions) {
        // Show the permissions in a TextView
        Resources res = context.getResources();
        String t = "";
        for (String p : permissions) {
            int id = res.getIdentifier("PERMISSION_" + p, "string", "org.webappbooster");
            if (id != 0) {
                t += "&#8226; " + context.getString(id) + "<br/>";
            }
        }
        TextView v = (TextView) popupView.findViewById(R.id.text_permissions);
        v.setText(Html.fromHtml(t));

        // Show sub-title for the dialog
        String s = context.getString(titleId, url);
        v = (TextView) popupView.findViewById(R.id.url_permission);
        v.setText(s);
    }

    public void show(DialogInterface.OnClickListener listener) {
        this.listener = listener;
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
        listener.onClick(dialog, which);
    }
}
