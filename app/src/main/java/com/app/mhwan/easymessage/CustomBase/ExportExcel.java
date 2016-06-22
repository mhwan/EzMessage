package com.app.mhwan.easymessage.CustomBase;

import android.content.Context;
import android.os.Environment;

import com.app.mhwan.easymessage.CustomView.MessageItem;
import com.app.mhwan.easymessage.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import jxl.CellView;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.UnderlineStyle;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

/**
 * Created by Mhwan on 2016. 6. 21..
 */
public class ExportExcel {
    private ArrayList<MessageItem> messageItems;
    private int type;
    private String filename;
    private File complete_file;
    private Calendar start_date, end_date;
    private Context context;
    public ExportExcel(ArrayList<MessageItem> messageItems, int type, String filename, Calendar start_date, Calendar end_date){
        this.messageItems = messageItems;
        this.type = type;
        this.filename = filename;
        this.start_date = start_date;
        this.end_date = end_date;
        context = AppContext.getContext();
    }
    public boolean writeToExcel(){
        ArrayList<MessageItem> resultlist = listFilter();
        if (resultlist.isEmpty())
            return false;

        try {
            WritableWorkbook workbook = Workbook.createWorkbook(complete_file);
            WritableSheet sheet = workbook.createSheet("eZMessage", 0);
            WritableCellFormat titleformat = new WritableCellFormat(new WritableFont(WritableFont.ARIAL, 9, WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.WHITE));
            titleformat.setBackground(Colour.INDIGO);
            titleformat.setAlignment(Alignment.CENTRE);
            titleformat.setBorder(Border.ALL, BorderLineStyle.MEDIUM);

            String[] titles = context.getResources().getStringArray(R.array.excel_title);
            for (int i =0; i<titles.length; i++){
                Label label = new Label(i, 0, titles[i], titleformat);
                sheet.addCell(label);
            }

            String[] types = context.getResources().getStringArray(R.array.inoroutgoing);
            for (int i=0; i<resultlist.size(); i++){
                for (int j=0; j<titles.length; j++){
                    String str="";
                    if (j == 0)
                        str = resultlist.get(i).getPh_number();
                    else if (j == 1)
                        str = types[resultlist.get(i).getType()];
                    else if (j == 2)
                        str = resultlist.get(i).getContent();
                    else if (j == 3)
                        str = resultlist.get(i).getTime();
                    else
                        str = (resultlist.get(i).getRequest_code() < 0) ? "" : context.getString(R.string.scheduled);

                    sheet.addCell(new Label(j, i+1, str));
                }
            }

            for(int x=0;x<titles.length;x++)
            {
                CellView cell=sheet.getColumnView(x);
                cell.setAutosize(true);
                sheet.setColumnView(x, cell);
            }
            workbook.write();
            workbook.close();
        }catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (WriteException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
    private ArrayList<MessageItem> listFilter(){
        ArrayList<MessageItem> resultlist = new ArrayList<>();

        for (MessageItem item : messageItems){
            if (isbetweenDate(item)){
                if (type == 0)
                    resultlist.add(item);
                else if (type == 1 && item.getType() == 0)
                    resultlist.add(item);
                else if (type == 2 && item.getType() == 1)
                    resultlist.add(item);
            }
        }
        File directory = new File(Environment.getExternalStorageDirectory()+File.separator+"eZMessage"+File.separator);
        if (!directory.exists())
            directory.mkdirs();
        complete_file = new File(directory, filename+".xls");

        return resultlist;
    }
    public String getComplete_path(){
        return complete_file.getAbsolutePath();
    }

    private boolean isbetweenDate(MessageItem item){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(AppUtility.getAppinstance().getDate(item.getTime(), AppUtility.BasicInfo.DB_DATETIME_FORMAT));
        System.out.println(end_date.getTime());
        if (start_date.compareTo(calendar)<=0 && end_date.compareTo(calendar)>=0)
            return true;

        return false;
    }
}
