package com.baidu.meet.ListView;

import java.util.ArrayList;

import com.baidu.meet.util.UtilHelper;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListAdapter;
import android.widget.TextView;

public class BdListAdpter extends BaseAdapter{
	private Context mContext = null;
    private ListAdapter mAdapter = null;
    private ArrayList<FixedViewInfo> mHeaderViewInfos = null;
    private ArrayList<FixedViewInfo> mFooterViewInfos = null;
    private boolean mAreAllFixedViewsSelectable = false;
    private boolean mIsFilterable = false;
    private DataSetObserver mDataSetObserver = null;
    private DataSetObserver mAdapterDataSetObserver = null;
    
    public BdListAdpter(Context context) {
    	mContext = context;
    	mHeaderViewInfos = new ArrayList<FixedViewInfo>();
    	mFooterViewInfos = new ArrayList<FixedViewInfo>();
    	mAreAllFixedViewsSelectable =
    		areAllListInfosSelectable(mHeaderViewInfos)
                && areAllListInfosSelectable(mFooterViewInfos);
    	mAdapterDataSetObserver = new DataSetObserver() {

			@Override
			public void onChanged() {
				// TODO Auto-generated method stub
				super.onChanged();
				if(mDataSetObserver != null){
					mDataSetObserver.onChanged();
				}
			}

			@Override
			public void onInvalidated() {
				// TODO Auto-generated method stub
				super.onInvalidated();
				if(mDataSetObserver != null){
					mDataSetObserver.onInvalidated();
				}
			}
    		
		};
    }
    
    public int getWrappedCount(){
    	if (mAdapter != null) {
    		return mAdapter.getCount();
    	}else{
    		return 0;
    	}
    }
    
    public ListAdapter getWrappedAdapter() {
        return mAdapter;
    }
    
    public void setAdaper(ListAdapter adapter){
    	if(mAdapter != null){
    		mIsFilterable = false;
    	}
    	mAdapter = adapter;
    	if(mAdapter != null){
	    	mIsFilterable = mAdapter instanceof Filterable;
    	}
    	notifyDataSetChanged();
    }
    
    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
    	super.registerDataSetObserver(observer);
    	mDataSetObserver = observer;
        if(mAdapter != null){
        	mAdapter.registerDataSetObserver(mAdapterDataSetObserver);
        }
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
    	super.unregisterDataSetObserver(observer);
    	mDataSetObserver = observer;
        if(mAdapter != null){
        	mAdapter.unregisterDataSetObserver(mAdapterDataSetObserver);
        }
    }
    
    public int getHeadersCount() {
        return mHeaderViewInfos.size();
    }

    public int getFootersCount() {
        return mFooterViewInfos.size();
    }
    
    private boolean areAllListInfosSelectable(ArrayList<FixedViewInfo> infos) {
        if (infos != null) {
            for (FixedViewInfo info : infos) {
                if (!info.isSelectable) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean removeHeader(View v) {
    	if(v == null){
    		return false;
    	}
        for (int i = 0; i < mHeaderViewInfos.size(); i++) {
            FixedViewInfo info = mHeaderViewInfos.get(i);
            if (info.view == v) {
                mHeaderViewInfos.remove(i);
                mAreAllFixedViewsSelectable =
                	areAllListInfosSelectable(mHeaderViewInfos)
                        && areAllListInfosSelectable(mFooterViewInfos);
                notifyDataSetChanged();
                return true;
            }
        }
        return false;
    }

    public boolean removeFooter(View v) {
    	if(v == null){
    		return false;
    	}
        for (int i = 0; i < mFooterViewInfos.size(); i++) {
            FixedViewInfo info = mFooterViewInfos.get(i);
            if (info.view == v) {
                mFooterViewInfos.remove(i);
                mAreAllFixedViewsSelectable =
                	areAllListInfosSelectable(mHeaderViewInfos)
                        && areAllListInfosSelectable(mFooterViewInfos);
                notifyDataSetChanged();
                return true;
            }
        }
        return false;
    }
    
    public Filter getFilter() {
        if (mIsFilterable && mAdapter != null) {
            return ((Filterable) mAdapter).getFilter();
        }
        return null;
    }
    
    
    public void addHeaderView(View v){
    	addHeaderView(v, null, true, -1);
    }
    
    public void addHeaderView(View v, int index){
    	addHeaderView(v, null, true, index);
    }
    
    public void addHeaderView(View v, Object data, boolean isSelectable, int index) {
    	if(v == null){
    		return;
    	}
    	FixedViewInfo info = new FixedViewInfo();
        info.view = v;
        info.data = data;
        info.isSelectable = isSelectable;
        if(index < 0 || index > mHeaderViewInfos.size()){
        	mHeaderViewInfos.add(info);
        }else{
        	mHeaderViewInfos.add(index, info);
        }
        notifyDataSetChanged();
    }

    public void addFooterView(View v){
    	addFooterView(v, null, true, -1);
    }
    
    public void addFooterView(View v, int index){
    	addFooterView(v, null, true, index);
    }
    
    public void addFooterView(View v, Object data, boolean isSelectable, int index) {
    	if(v == null){
    		return;
    	}
    	FixedViewInfo info = new FixedViewInfo();
        info.view = v;
        info.data = data;
        info.isSelectable = isSelectable;
        if(index < 0 || index > mFooterViewInfos.size()){
        	mFooterViewInfos.add(info);
        }else{
        	mFooterViewInfos.add(index, info);
        }
        notifyDataSetChanged();
    }
    
    
    
    
	@Override
	public int getCount() {
        if (mAdapter != null) {
            return getFootersCount() + getHeadersCount() + mAdapter.getCount();
        } else {
            return getFootersCount() + getHeadersCount();
        }
    }
	
	@Override
	public Object getItem(int position) {
        // Header (negative positions will throw an ArrayIndexOutOfBoundsException)
        int numHeaders = getHeadersCount();
        if (position < numHeaders) {
            return mHeaderViewInfos.get(position).data;
        }

        // Adapter
        final int adjPosition = position - numHeaders;
        int adapterCount = 0;
        if (mAdapter != null) {
            adapterCount = mAdapter.getCount();
            if (adjPosition < adapterCount) {
                return mAdapter.getItem(adjPosition);
            }
        }

        // Footer (off-limits positions will throw an ArrayIndexOutOfBoundsException)
        int index = adjPosition - adapterCount;
        if(index >= 0 && index < mFooterViewInfos.size()){
        	return mFooterViewInfos.get(index).data;
        }else{
        	return null;
        }
    }

	@Override
    public long getItemId(int position) {
        int numHeaders = getHeadersCount();
        if (mAdapter != null && position >= numHeaders) {
            int adjPosition = position - numHeaders;
            int adapterCount = mAdapter.getCount();
            if (adjPosition < adapterCount) {
                return mAdapter.getItemId(adjPosition);
            }
        }
        return Long.MIN_VALUE;
    }

    @Override
	public boolean hasStableIds() {
        if (mAdapter != null) {
            return mAdapter.hasStableIds();
        }else{
        	return super.hasStableIds();
        }
    }

	@Override
	public boolean areAllItemsEnabled() {
        if (mAdapter != null) {
            return mAreAllFixedViewsSelectable && mAdapter.areAllItemsEnabled();
        } else {
            return super.areAllItemsEnabled();
        }
    }

	@Override
    public boolean isEnabled(int position) {
        // Header (negative positions will throw an ArrayIndexOutOfBoundsException)
        int numHeaders = getHeadersCount();
        if (position < numHeaders) {
            return mHeaderViewInfos.get(position).isSelectable;
        }

        // Adapter
        final int adjPosition = position - numHeaders;
        int adapterCount = 0;
        if (mAdapter != null) {
            adapterCount = mAdapter.getCount();
            if (adjPosition < adapterCount) {
                return mAdapter.isEnabled(adjPosition);
            }
        }

        // Footer (off-limits positions will throw an ArrayIndexOutOfBoundsException)
        int index = adjPosition - adapterCount;
        if(index >= 0 && index < mFooterViewInfos.size()){
        	return mFooterViewInfos.get(index).isSelectable;
        }else{
        	return false;
        }
    }
    

	@Override
	public int getItemViewType(int position) {
        int numHeaders = getHeadersCount();
        if (mAdapter != null && position >= numHeaders) {
            int adjPosition = position - numHeaders;
            int adapterCount = mAdapter.getCount();
            if (adjPosition < adapterCount) {
                return mAdapter.getItemViewType(adjPosition);
            }
        }

        return AdapterView.ITEM_VIEW_TYPE_HEADER_OR_FOOTER;
    }

	@Override
    public int getViewTypeCount() {
        if (mAdapter != null) {
            return mAdapter.getViewTypeCount() + 1;
        }
        return 1;
    }
    
	@Override
	public boolean isEmpty() {
        return mAdapter == null || mAdapter.isEmpty();
    }

	public View getView(int position, View convertView, ViewGroup parent) {
		View v = null;
        // Header (negative positions will throw an ArrayIndexOutOfBoundsException)
        int numHeaders = getHeadersCount();
        if (position < numHeaders) {
            v = mHeaderViewInfos.get(position).view;
            if(v == null){
            	v = createErrorView();
            }
            return v;
        }

        // Adapter
        final int adjPosition = position - numHeaders;
        int adapterCount = 0;
        if (mAdapter != null) {
            adapterCount = mAdapter.getCount();
            if (adjPosition < adapterCount) {
            	try {
            		v = mAdapter.getView(adjPosition, convertView, parent);
				} catch (Exception e) {
                    e.printStackTrace();
				}
            	if(v == null){
            		v = createErrorView();
            	}
            	return v;
            }
        }

        // Footer (off-limits positions will throw an ArrayIndexOutOfBoundsException)
        try {
        	v = mFooterViewInfos.get(adjPosition - adapterCount).view;
        } catch (Exception e) {
		}
        if(v == null){
    		v = createErrorView();
    	}
        return v;
    }
    
	private View createErrorView(){
		TextView textView = new TextView(mContext);
		textView.setText("资源加载失败！");
		int padding = UtilHelper.dip2px(mContext, 15);
		textView.setPadding(padding, padding, padding, padding);
		return textView;
	}
	
    public class FixedViewInfo {
        public View view;
        public Object data;
        public boolean isSelectable;
    }

}
