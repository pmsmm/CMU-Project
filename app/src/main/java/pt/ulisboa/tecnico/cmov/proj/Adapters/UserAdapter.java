package pt.ulisboa.tecnico.cmov.proj.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.proj.Data.User;
import pt.ulisboa.tecnico.cmov.proj.R;

public class UserAdapter extends ArrayAdapter<User> {

    private Context context;
    private List<User> users;

    public UserAdapter(Context context, int resource, ArrayList<User> objects) {
        super(context, resource, objects);

        this.context = context;
        this.users = objects;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        User user = users.get(position);
        String userName = user.getUserName();

        //get the inflater and inflate the XML layout for each item
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (view == null) view = inflater.inflate(R.layout.activity_user_thumb, null);

        TextView user_name = view.findViewById(R.id.user_name);
        user_name.setText(userName);

        return view;
    }
}
