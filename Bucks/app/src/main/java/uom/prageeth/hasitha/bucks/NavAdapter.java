package uom.prageeth.hasitha.bucks;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

public class NavAdapter extends RecyclerView.Adapter<NavAdapter.NavViewHolder>{

    private LayoutInflater inflater;
    //private Context context;
    private ClickListner clickListner;
    List<NavigationDrawerData> data = Collections.emptyList();


    public NavAdapter(Context context, List<NavigationDrawerData> data){
        inflater = LayoutInflater.from(context);
        this.data = data;
        //this.context = context;
    }

    @Override
    public NavViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.nav_item, parent, false);
        NavViewHolder holder = new NavViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(NavViewHolder holder,int position) {

        final NavigationDrawerData current = data.get(position);
        holder.title.setText(current.title);
        holder.icon.setImageResource(current.iconId);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setClickListner(ClickListner clickListner){
        this.clickListner = clickListner;
    }

    public void delete(int position){
        data.remove(position);
        notifyItemRemoved(position);
    }

    class NavViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView title;
        ImageView icon;

        public NavViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);
            title = (TextView)itemView.findViewById(R.id.list_title);
            icon = (ImageView) itemView.findViewById(R.id.list_icon);
            //icon.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            if(clickListner != null){

                clickListner.itemClicked(v, getAdapterPosition());
            }
            //delete(getAdapterPosition());
            //Toast.makeText(context, "Item Clicked" + getAdapterPosition(), Toast.LENGTH_SHORT).show();
        }
    }

    public interface ClickListner{

        public void itemClicked(View view, int position);
    }
}
