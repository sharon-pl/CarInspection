package com.coretal.carinspection.adapters;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coretal.carinspection.R;
import com.evrencoskun.tableview.ITableView;
import com.evrencoskun.tableview.adapter.AbstractTableAdapter;
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractSorterViewHolder;
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder;

public class SubmissionTableViewAdapter
        extends AbstractTableAdapter<
                SubmissionTableViewAdapter.ColumnHeader,
                SubmissionTableViewAdapter.RowHeader,
                SubmissionTableViewAdapter.Cell> {

    public SubmissionTableViewAdapter(Context context) {
        super(context);
    }

    @Override
    public int getColumnHeaderItemViewType(int position) {
        return 0;
    }

    @Override
    public int getRowHeaderItemViewType(int position) {
        return 0;
    }

    @Override
    public int getCellItemViewType(int position) {
        return 0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateCellViewHolder(ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(mContext).inflate(R.layout.table_view_cell_layout, parent, false);

        // Create a Cell ViewHolder
        return new CellViewHolder(layout);
    }

    @Override
    public void onBindCellViewHolder(AbstractViewHolder holder, Object cellItemModel, int columnPosition, int rowPosition) {
        Cell cell = (Cell) cellItemModel;
        CellViewHolder viewHolder = (CellViewHolder) holder;
        viewHolder.setData(cell.getData());
    }

    @Override
    public RecyclerView.ViewHolder onCreateColumnHeaderViewHolder(ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(mContext).inflate(R.layout.table_view_column_header_layout, parent, false);
        return new ColumnHeaderViewHolder(layout, getTableView());
    }

    @Override
    public void onBindColumnHeaderViewHolder(AbstractViewHolder holder, Object columnHeaderItemModel, int columnPosition) {
        ColumnHeader columnHeader = (ColumnHeader) columnHeaderItemModel;

        ColumnHeaderViewHolder columnHeaderViewHolder = (ColumnHeaderViewHolder) holder;
        columnHeaderViewHolder.setColumnHeader(columnHeader);
    }

    @Override
    public RecyclerView.ViewHolder onCreateRowHeaderViewHolder(ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(mContext).inflate(R.layout.table_view_row_header_layout, parent, false);
        return new RowHeaderViewHolder(layout);
    }

    @Override
    public void onBindRowHeaderViewHolder(AbstractViewHolder holder, Object rowHeaderItemModel, int rowPosition) {
        RowHeader rowHeader = (RowHeader) rowHeaderItemModel;

        RowHeaderViewHolder rowHeaderViewHolder = (RowHeaderViewHolder) holder;
        rowHeaderViewHolder.row_header_textview.setText(String.valueOf(rowHeader.getData()));
    }

    @Override
    public View onCreateCornerView() {
        return null;
    }

    public static class Cell{

        private Object mData;

        public Cell(Object data) {
            this.mData = data;
        }

        public Object getData() {
            return mData;
        }

        public void setData(String data) { mData = data; }

    }

    public static class ColumnHeader {

        private String mData;

        public ColumnHeader(String mData) {
            this.mData = mData;
        }

        public String getData() {
            return mData;
        }
    }

    public static class RowHeader {

        private String mData;

        public RowHeader(String mData) {
            this.mData = mData;
        }

        public String getData() {
            return mData;
        }
    }

    public class CellViewHolder extends AbstractViewHolder {

        public final TextView cell_textview;
        public final LinearLayout cell_container;

        public CellViewHolder(View itemView) {
            super(itemView);
            cell_textview = (TextView) itemView.findViewById(R.id.cell_data);
            cell_container = (LinearLayout) itemView.findViewById(R.id.cell_container);
        }

        public void setData(Object data) {
            cell_textview.setText(String.valueOf(data));

            // If your TableView should have auto resize for cells & columns.
            // Then you should consider the below lines. Otherwise, you can ignore them.

            // It is necessary to remeasure itself.
            cell_container.getLayoutParams().width = LinearLayout.LayoutParams.WRAP_CONTENT;
            cell_textview.requestLayout();
        }
    }

    public class ColumnHeaderViewHolder extends AbstractSorterViewHolder {

        public final LinearLayout column_header_container;
        public final TextView column_header_textview;
        public final ITableView tableView;

        public ColumnHeaderViewHolder(View itemView, ITableView tableView) {
            super(itemView);

            this.tableView = tableView;
            column_header_textview = (TextView) itemView.findViewById(R.id.column_header_textView);
            column_header_container = (LinearLayout) itemView.findViewById(R.id.column_header_container);
        }

        /**
         * This method is calling from onBindColumnHeaderHolder on TableViewAdapter
         */
        public void setColumnHeader(ColumnHeader columnHeader) {
            column_header_textview.setText(String.valueOf(columnHeader.getData()));

            // If your TableView should have auto resize for cells & columns.
            // Then you should consider the below lines. Otherwise, you can ignore them.

            // It is necessary to remeasure itself.
            column_header_container.getLayoutParams().width = LinearLayout.LayoutParams.WRAP_CONTENT;
            column_header_textview.requestLayout();
            itemView.requestLayout();
        }
    }

    public class RowHeaderViewHolder extends AbstractViewHolder {
        public final TextView row_header_textview;

        public RowHeaderViewHolder(View itemView) {
            super(itemView);
            row_header_textview = (TextView) itemView.findViewById(R.id.row_header_textview);
        }
    }
}
