package by.citech.contact;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import by.citech.R;
import by.citech.param.Settings;
import by.citech.param.Tags;


public class ContactsRecyclerAdapter
        extends RecyclerView.Adapter<ContactsRecyclerAdapter.ViewHolder> {

    private static final boolean debug = Settings.debug;
    private static final String TAG = Tags.CONTACTS_ADAPTER;

    private List<Contact> contacts, contactsCleanCopy;
    private OnClickViewListener onClickViewListener;

    public ContactsRecyclerAdapter(List<Contact> contacts) {
        this.contacts = contacts;
        contactsCleanCopy = this.contacts;
        setHasStableIds(true);
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
            if (debug) Log.i(TAG, "resetSwipe");
            isSwiped = false;
            swipedPos = -1;
            swipedView = null;
        }

        public void resolveSwipe() {
            if (debug) Log.i(TAG, "resolveSwipe");
            if (isSwiped) {
                if (debug) Log.i(TAG, "resolveSwipe is swiped");
                if (swipedPos < 0) {
                    Log.e(TAG, "resolveSwipe swipedPos < 0");
                } else {
                    notifyItemChanged(swipedPos);
                }
                if (swipedView == null) {
                    Log.e(TAG, "resolveSwipe swipedView == null");
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
            Log.e(TAG, "getItem something went wrong");
            return null;
        }
    }

    //--------------------- getters and setters

    public void setOnClickViewListener(OnClickViewListener onClickViewListener) {
        this.onClickViewListener = onClickViewListener;
    }

    public List<Contact> getContacts() {
        if (debug) Log.i(TAG, "getContacts");
        return contacts;
    }

    //--------------------- base

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (debug) Log.i(TAG, "onCreateViewHolder");
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_element_contact, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Contact contact = contacts.get(position);
//        if (debug) Log.i(TAG, "onBindViewHolder " + contact.toString());
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
//        if (debug) Log.i(TAG, "onViewRecycled: " + getDebugItemInfoFromViewHolder(holder));
        super.onViewRecycled(holder);
    }

    @Override
    public boolean onFailedToRecycleView(ViewHolder holder) {
//        if (debug) Log.i(TAG, "onFailedToRecycleView: " + getDebugItemInfoFromViewHolder(holder));
        return super.onFailedToRecycleView(holder);
    }

    @Override
    public void onViewAttachedToWindow(ViewHolder holder) {
//        if (debug) Log.i(TAG, "onViewAttachedToWindow: " + getDebugItemInfoFromViewHolder(holder));
        super.onViewAttachedToWindow(holder);
    }

    @Override
    public void onViewDetachedFromWindow(ViewHolder holder) {
//        if (debug) Log.i(TAG, "onViewDetachedFromWindow: " + getDebugItemInfoFromViewHolder(holder));
        super.onViewDetachedFromWindow(holder);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
//        if (debug) Log.i(TAG, "onAttachedToRecyclerView");
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
//        if (debug) Log.i(TAG, "onDetachedFromRecyclerView");
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
            Log.e(TAG, "getItemCount contacts are null");
            return 0;
        } else {
            return contacts.size();
        }
    }

    @Override
    public long getItemId(int position) {
        if (contacts == null) {
            Log.e(TAG, "getItemCount contacts are null");
            return 0;
        } else {
            return contacts.get(position).getId();
        }
    }

    public void filter(String charText) {
        if (debug) Log.i(TAG, "filter");
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
