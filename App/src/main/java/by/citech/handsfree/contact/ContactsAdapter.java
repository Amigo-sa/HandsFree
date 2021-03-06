package by.citech.handsfree.contact;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import by.citech.handsfree.R;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.parameters.Tags;
import timber.log.Timber;


public class ContactsAdapter
        extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

    private static final boolean debug = Settings.debug;
    private static final String TAG = Tags.ContactsAdapter;

    private List<Contact> contacts, contactsCleanCopy;
    private OnClickViewListener onClickViewListener;

    public ContactsAdapter(List<Contact> contacts) {
        this.contacts = contacts;
        contactsCleanCopy = this.contacts;
//      setHasStableIds(true); //TODO: check if it has connection with Inconsistency FATAL EXCEPTION
        setHasStableIds(false);
    }

    public static class ViewHolder
            extends RecyclerView.ViewHolder {
        TextView textViewContactName, textViewContactIp;
        ViewHolder(View v) {
            super(v);
            textViewContactName = v.findViewById(R.id.textContactName);
            textViewContactIp = v.findViewById(R.id.textContactIp);
        }
    }

    //--------------------- additional

    public class SwipeCrutch {

        private int swipedPos;
        private View swipedView;
        private boolean isSwiped;

        public SwipeCrutch() {
            swipedPos = -1;
            isSwiped = false;
        }

        public void designateSwipe(View toDelView, int toDelPos) {
            if (isSwiped) Log.e(TAG, "designateSwipe swipe already designated");
            if (toDelView == null || toDelPos < 0) Log.e(TAG, "designateSwipe illegal parameters");
            this.swipedView = toDelView;
            this.swipedPos = toDelPos;
            isSwiped = true;
        }

        public void resetSwipe() {
            Timber.i("resetSwipe");
            isSwiped = false;
            swipedPos = -1;
            swipedView = null;
        }

        public void resolveSwipe() {
            Timber.i("resolveSwipe");
            if (isSwiped) {
                Timber.i("resolveSwipe is swiped");
                if (swipedPos < 0) {
                    Timber.e("resolveSwipe swipedPos < 0");
                } else {
                    notifyItemChanged(swipedPos);
                }
                if (swipedView == null) {
                    Timber.e("resolveSwipe swipedView == null");
                } else {
                    swipedView.setVisibility(View.VISIBLE);
                }
            }
            resetSwipe();
        }

    }

    public interface OnClickViewListener {
        void doCallbackOnClickView(Contact contact, int position);
    }

    //--------------------- extended

    public int getItemPosition(Contact contact) {
        if (contacts.contains(contact)) {
            return contacts.indexOf(contact);
        } else {
            return -1;
        }
    }

    public Contact getItem(int position) {
        if (position >= 0 && contacts != null && position < contacts.size()) {
            return contacts.get(position);
        } else {
            Timber.e("getItem something went wrong");
            return null;
        }
    }

    //--------------------- getters and setters

    public void setOnClickViewListener(OnClickViewListener onClickViewListener) {
        this.onClickViewListener = onClickViewListener;
    }

    //--------------------- base

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//      Timber.i("onCreateViewHolder");
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_element_contact, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Contact contact = contacts.get(position);
//      Timber.i("onBindViewHolder " + contact.toString());
        holder.textViewContactName.setText(contact.getName());
        holder.textViewContactIp.setText(contact.getIp());
        holder.itemView.setOnClickListener((view) -> {
            if (onClickViewListener != null) {
                onClickViewListener.doCallbackOnClickView(contact, position);
            }
        });
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
//      Timber.i("onViewRecycled: " + getDebugItemInfoFromViewHolder(holder));
        super.onViewRecycled(holder);
    }

    @Override
    public boolean onFailedToRecycleView(ViewHolder holder) {
//      Timber.i("onFailedToRecycleView: " + getDebugItemInfoFromViewHolder(holder));
        return super.onFailedToRecycleView(holder);
    }

    @Override
    public void onViewAttachedToWindow(ViewHolder holder) {
//      Timber.i("onViewAttachedToWindow: " + getDebugItemInfoFromViewHolder(holder));
        super.onViewAttachedToWindow(holder);
    }

    @Override
    public void onViewDetachedFromWindow(ViewHolder holder) {
//      Timber.i("onViewDetachedFromWindow: " + getDebugItemInfoFromViewHolder(holder));
        super.onViewDetachedFromWindow(holder);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
//      Timber.i("onAttachedToRecyclerView");
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
//      Timber.i("onDetachedFromRecyclerView");
        super.onDetachedFromRecyclerView(recyclerView);
    }

    //--------------------- debug

    private String getDebugItemInfoFromViewHolder(ViewHolder holder) {
        return String.format(Locale.US, "adapterPos is %d, itemId is %d, itemViewType is %d, layoutPos is %d, oldPos is %d, item is %s"
                ,(holder == null) ? null : holder.getAdapterPosition()
                ,(holder == null) ? null : holder.getItemId()
                ,(holder == null) ? null : holder.getItemViewType()
                ,(holder == null) ? null : holder.getLayoutPosition()
                ,(holder == null) ? null : holder.getOldPosition()
                ,(holder == null
                        || contacts.size() <= holder.getAdapterPosition()
                        || holder.getAdapterPosition() < 0) ? null : contacts.get(holder.getAdapterPosition()).toString()
        );
    }

    //--------------------- data

    @Override
    public int getItemCount() {
        if (contacts == null) {
            Timber.e("getItemCount contacts are null");
            return 0;
        } else {
            return contacts.size();
        }
    }

    @Override
    public long getItemId(int position) {
        if (contacts == null) {
            Timber.e("getItemCount contacts are null");
            return 0;
        } else {
            return contacts.get(position).getId();
        }
    }

    public void filter(String charText) {
//      Timber.i("filter");
        charText = charText.toLowerCase(Locale.getDefault());
        contacts = new ArrayList<>();
        if (charText.length() == 0) {
            contacts.addAll(contactsCleanCopy);
        } else {
            for (Contact contact : contactsCleanCopy) {
                if (contact.getName().toLowerCase(Locale.getDefault()).contains(charText)
                        || contact.getIp().toLowerCase(Locale.getDefault()).contains(charText)) {
                    contacts.add(contact);
                }
            }
        }
        notifyDataSetChanged();
    }

}
