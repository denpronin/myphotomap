package com.pronin.myphotomap;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.pronin.myphotomap.model.Picture;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.Collection;

public class ImgListAdapter extends RecyclerView.Adapter<ImgListAdapter.ImgListViewHolder> {
    private final ArrayList<Picture> imgList = new ArrayList<>();
    private final OnImgClickListener onImgClickListener;

    public interface OnImgClickListener {
        void onImgClick(Picture picture);
    }

    public ImgListAdapter(OnImgClickListener onImgClickListener) {
        this.onImgClickListener = onImgClickListener;
    }
    @NonNull
    @Override
    public ImgListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ImgListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImgListViewHolder holder, int position) {
        holder.bind(imgList.get(position));
    }

    @Override
    public int getItemCount() {
        return imgList.size();
    }

    public void setItems (Collection<Picture> pictures) {
        imgList.addAll(pictures);
        notifyDataSetChanged();
    }

    public void clearItems() {
        imgList.clear();
        notifyDataSetChanged();
    }

    class ImgListViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageSrc;
        private final TextView date;

        public ImgListViewHolder(@NonNull View itemView) {
            super(itemView);
            imageSrc = itemView.findViewById(R.id.imgSrc);
            date = itemView.findViewById(R.id.imgDate);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Picture picture = imgList.get(getLayoutPosition());
                    onImgClickListener.onImgClick(picture);
                }
            });
        }

        public void bind (Picture picture) {
            Picasso.get()
                    .load("file://" + picture.getPath())
                    .error(R.drawable.ic_launcher_background)
                    .fit()
                    .centerCrop()
                    .into(imageSrc);
            date.setText(picture.getDate());
            imageSrc.setVisibility(picture.getPath() != null ? View.VISIBLE : View.GONE);
        }
    }
}
