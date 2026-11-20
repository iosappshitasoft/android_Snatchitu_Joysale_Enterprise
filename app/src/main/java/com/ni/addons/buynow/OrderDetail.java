package com.ni.addons.buynow;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.ni.helper.LocaleManager;
import com.ni.helper.NetworkReceiver;
import com.ni.SnatchItUp.BaseActivity;
import com.ni.SnatchItUp.ChatActivity;
import com.ni.SnatchItUp.JoysaleApplication;
import com.ni.SnatchItUp.R;
import com.ni.model.MySalesResponse;
import com.ni.model.ReviewObject;
import com.ni.utils.ApiClient;
import com.ni.utils.ApiInterface;
import com.ni.utils.Constants;
import com.ni.utils.GetSet;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPCellEvent;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by hitasoft on 16/9/16.
 * <p>
 * This class is to Display Order Details.
 */

public class OrderDetail extends BaseActivity implements View.OnClickListener {

    private static final String TAG = OrderDetail.class.getSimpleName();
    /**
     * Declare Layout Elements
     **/
    ImageView backBtn, settingbtn, itemImage, titleimage, printbtn;
    TextView itemName, itemPrice, chatseller, orderstatus, title, orderid, orderDateTxt, shippedDateTxt,
            deliveryDateTxt,
            sellername, selrtext, paymenttype, transactionid, orderdate, shipdate, deliverydate, price, shipingprice,
            total, trackingId, trackingShipmentDate, trackingLogisticName, trackingCourierService, trackingNotes,
            updateReview, nickName, fullName, addressLine, city, country, reviewTitle, ratingCount, review, editReview,
            reviewDate;
    RelativeLayout main, reviewLay;
    LinearLayout writeReviewLay, trackingLay;

    DisplayMetrics displayMetrics;
    RatingBar ratingBar;
    ProgressDialog dialog;

    MySalesResponse.Result data = new MySalesResponse().new Result();

    /**
     * Declare Variables
     **/

    private int topPadding, leftPadding;
    int position;
    String odate = "", sdate = "", ddate = "", from, chatId = "", pdfName;
    ApiInterface apiInterface;
    private Context context;
    private NumberFormat numberFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_detail);
        context = this;
        numberFormat = NumberFormat.getInstance(new Locale("en", "US"));

        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        backBtn = (ImageView) findViewById(R.id.backbtn);
        printbtn = (ImageView) findViewById(R.id.printbtn);
        itemImage = (ImageView) findViewById(R.id.imageView);
        titleimage = (ImageView) findViewById(R.id.userImg);
        settingbtn = (ImageView) findViewById(R.id.settingbtn);
        main = (RelativeLayout) findViewById(R.id.main);
        updateReview = findViewById(R.id.updateReview);
        title = (TextView) findViewById(R.id.username);
        itemName = (TextView) findViewById(R.id.itemtitle);
        chatseller = (TextView) findViewById(R.id.chatseller);
        itemPrice = (TextView) findViewById(R.id.itemprice);
        orderstatus = (TextView) findViewById(R.id.orderstatus);
        sellername = (TextView) findViewById(R.id.selrname);
        selrtext = (TextView) findViewById(R.id.selrtext);
        paymenttype = (TextView) findViewById(R.id.paymenttype);
        transactionid = (TextView) findViewById(R.id.transid);
        orderdate = (TextView) findViewById(R.id.orderdate);
        shipdate = (TextView) findViewById(R.id.shipeddate);
        deliverydate = (TextView) findViewById(R.id.deliverydate);
        price = (TextView) findViewById(R.id.price);
        shipingprice = (TextView) findViewById(R.id.shipingfee);
        total = (TextView) findViewById(R.id.ordertotal);
        orderid = (TextView) findViewById(R.id.orderid);
        nickName = (TextView) findViewById(R.id.nickName);
        fullName = (TextView) findViewById(R.id.fullName);
        addressLine = (TextView) findViewById(R.id.addressLine);
        city = (TextView) findViewById(R.id.city);
        country = (TextView) findViewById(R.id.country);
        orderDateTxt = (TextView) findViewById(R.id.order_date_txt);
        shippedDateTxt = (TextView) findViewById(R.id.shipped_date_txt);
        deliveryDateTxt = (TextView) findViewById(R.id.delivery_date_txt);
        reviewTitle = (TextView) findViewById(R.id.reviewTitle);
        ratingCount = (TextView) findViewById(R.id.ratingCount);
        review = (TextView) findViewById(R.id.review);
        editReview = (TextView) findViewById(R.id.editReview);
        reviewLay = (RelativeLayout) findViewById(R.id.reviewLay);
        writeReviewLay = (LinearLayout) findViewById(R.id.writeReviewLay);
        trackingLay = (LinearLayout) findViewById(R.id.trackingLay);
        trackingId = (TextView) findViewById(R.id.trackigId);
        trackingShipmentDate = (TextView) findViewById(R.id.trackingShipmentDate);
        trackingLogisticName = (TextView) findViewById(R.id.trackingLogisticName);
        trackingCourierService = (TextView) findViewById(R.id.trackingCourierService);
        trackingNotes = (TextView) findViewById(R.id.trackingNotes);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        reviewDate = (TextView) findViewById(R.id.reviewDate);

        dialog = new ProgressDialog(OrderDetail.this, R.style.AppCompatAlertDialogStyle);
        dialog.setMessage(getString(R.string.pleasewait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            Drawable drawable = new ProgressBar(this).getIndeterminateDrawable().mutate();
            drawable.setColorFilter(ContextCompat.getColor(this, R.color.progressColor),
                    PorterDuff.Mode.SRC_IN);
            dialog.setIndeterminateDrawable(drawable);
        }

        topPadding = JoysaleApplication.dpToPx(OrderDetail.this, 5);
        leftPadding = JoysaleApplication.dpToPx(OrderDetail.this, 15);

        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        data = (MySalesResponse.Result) getIntent().getExtras().get(Constants.TAG_DATA);
        Log.d(TAG, "onCreate: " + data.getRating());
        from = (String) getIntent().getExtras().get(Constants.TAG_FROM);
        position = (Integer) getIntent().getExtras().get(Constants.TAG_POSITION);

        LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable().getCurrent();
        stars.getDrawable(2).setColorFilter(getResources().getColor(R.color.starColor), PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(0).setColorFilter(getResources().getColor(R.color.secondaryText), PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(2).setColorFilter(getResources().getColor(R.color.starColor), PorterDuff.Mode.SRC_ATOP);

        backBtn.setVisibility(View.VISIBLE);
        titleimage.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);
        settingbtn.setVisibility(View.VISIBLE);

        applySystemBarInsets(findViewById(R.id.actionbar), findViewById(R.id.chatseller));

        backBtn.setOnClickListener(this);
        settingbtn.setOnClickListener(this);
        chatseller.setOnClickListener(this);
        printbtn.setOnClickListener(this);
        writeReviewLay.setOnClickListener(this);
        editReview.setOnClickListener(this);

        setData();

    }

    private void refreshGallery(File file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(file));
        sendBroadcast(mediaScanIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // getReview();
    }

    private void getReview() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_USERID, GetSet.getUserId());
            map.put(Constants.TAG_ITEM_ID, data.getOrderitems().getItemid());
            Log.i(TAG, "getReview: " + map);
            dialog.show();
            Call<ReviewObject> call = apiInterface.reviewDetails(map);
            call.enqueue(new Callback<ReviewObject>() {
                @Override
                public void onResponse(Call<ReviewObject> call, retrofit2.Response<ReviewObject> response) {

                    // Log.v("reviewDetails", "=" + response.body().toString());
                    if (dialog.isShowing() && dialog != null) {
                        dialog.dismiss();
                    }
                    try {
                        if (response.body().getStatus().equalsIgnoreCase("true")) {
                            updateReview.setText(getString(R.string.edit_review));
                            ratingBar.setRating(Float.parseFloat(response.body().getResult().getRating()));
                        } else if (response.body().getStatus().equalsIgnoreCase("false")) {

                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ReviewObject> call, Throwable t) {
                    Log.v("reviewDetails", "onFailure=" + t.getMessage());

                    if (dialog.isShowing() && dialog != null) {
                        dialog.dismiss();
                    }
                }
            });
        } else {

            if (dialog.isShowing() && dialog != null) {
                dialog.dismiss();
            }
        }
    }

    private void createPdf() {
        String filePath = null;
        String root = Environment.getExternalStorageDirectory()
                .toString();
        File newDir = new File(context.getExternalCacheDir() + "/Docs_"
                + getString(R.string.app_name));
        if (!newDir.exists()) {
            newDir.mkdirs();
        }

        Random gen = new Random();
        int n = 10000;
        n = gen.nextInt(n);
        pdfName = "transaction-" + n + ".pdf";
        File file = new File(newDir, pdfName);
        filePath = file.getAbsolutePath();

        if (file.exists()) {
            file.delete();
            Log.v("file.exists", "file.exists");
        }
        try {
            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
            Rectangle rect = new Rectangle(30, 30, 550, 800);
            writer.setBoxSize("art", rect);

            HeaderFooterPageEvent event = new HeaderFooterPageEvent();
            writer.setPageEvent(event);

            document.open();

            addTitle(document);
            addContent(document);

            document.close();
            refreshGallery(file);

        } catch (Exception e) {
            e.printStackTrace();
        }
        File f = new File(filePath);
        Uri uri = Uri.fromFile(f);
        Log.v(TAG, "Uri=" + uri);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            PrintManager printmanager = (PrintManager) baseActivity.getSystemService(Context.PRINT_SERVICE);
            String name = "CheckPrint";
            printmanager.print(name, new MyPrintDocumentAdapter(f), null);
        } else {

            Intent email = new Intent(Intent.ACTION_SEND);
            email.putExtra(Intent.EXTRA_SUBJECT, "Order Detail");
            email.putExtra(Intent.EXTRA_STREAM, uri);
            email.setType("message/*");
            startActivity(Intent.createChooser(
                    email, "Send mail..."));
        }
    }

    private void addTitle(Document document) {
        try {
            Paragraph preface = new Paragraph();
            preface.setAlignment(Element.HEADER);
            String date = "";
            if (data.getSaledate() != null && !data.getSaledate().equals("")) {
                long ordDate = Long.parseLong(data.getSaledate()) * 1000;
                date = JoysaleApplication.getDate(ordDate, "dd/MM/yyyy");
            }
            String title = "\n\n" + "Order # " + data.getInvoice() + " on " + date;
            preface.add(new Paragraph(title, new Font(Font.FontFamily.UNDEFINED, 18,
                    Font.BOLD)));
            document.add(preface);
        } catch (DocumentException e) {
            e.printStackTrace();
        }

    }

    private void addContent(Document document) {
        try {
            Paragraph header = new Paragraph();
            header.add("\n" + "Payment method : " + data.getPaymentType());
            header.add("\n" + "Payment status : Completed");
            document.add(header);

            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);

            LineSeparator lineSeparator = new LineSeparator();
            lineSeparator.setLineColor(new BaseColor(Color.parseColor("#d0dbe5")));
            document.add(lineSeparator);

            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(2);
            PdfPCell cellOne = new PdfPCell(new Phrase("Buyers Details", new Font(Font.FontFamily.UNDEFINED, 14,
                    Font.BOLD)));
            PdfPCell cellTwo = new PdfPCell(new Phrase("Shipping Address", new Font(Font.FontFamily.UNDEFINED, 14,
                    Font.BOLD)));

            cellOne.setBorder(Rectangle.NO_BORDER);
            cellOne.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellOne.setPaddingBottom(JoysaleApplication.dpToPx(OrderDetail.this, 5));

            cellTwo.setBorder(Rectangle.NO_BORDER);
            cellTwo.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellOne.setPaddingBottom(JoysaleApplication.dpToPx(OrderDetail.this, 5));

            table.addCell(cellOne);
            table.addCell(cellTwo);
            table.setHorizontalAlignment(Element.ALIGN_LEFT);
            document.add(table);

            PdfPTable table2 = new PdfPTable(2);
            PdfPCell cOne = new PdfPCell(new Phrase("Buyers Name", new Font(Font.FontFamily.UNDEFINED, 12,
                    Font.BOLD)));
            PdfPCell cTwo = new PdfPCell(
                    new Phrase(data.getShippingaddress().getName(), new Font(Font.FontFamily.UNDEFINED, 12,
                            Font.BOLD)));

            cOne.setBorder(Rectangle.NO_BORDER);
            cOne.setHorizontalAlignment(Element.ALIGN_LEFT);

            cTwo.setBorder(Rectangle.NO_BORDER);
            cTwo.setHorizontalAlignment(Element.ALIGN_LEFT);

            table2.addCell(cOne);
            table2.addCell(cTwo);
            table2.setHorizontalAlignment(Element.ALIGN_LEFT);
            table2.setHeaderRows(1);

            cOne = new PdfPCell(new Phrase("Email : " + data.getOrderitems().getBuyerEmail()));
            cTwo = new PdfPCell(new Phrase(data.getShippingaddress().getAddress1() + " ,"));

            cOne.setBorder(Rectangle.NO_BORDER);
            cOne.setHorizontalAlignment(Element.ALIGN_LEFT);

            cTwo.setBorder(Rectangle.NO_BORDER);
            cTwo.setHorizontalAlignment(Element.ALIGN_LEFT);

            table2.addCell(cOne);
            table2.addCell(cTwo);
            table2.setHorizontalAlignment(Element.ALIGN_LEFT);
            table2.setHeaderRows(2);

            cOne = new PdfPCell(new Phrase(""));
            cTwo = new PdfPCell(new Phrase(data.getShippingaddress().getAddress2() + " ,"));

            cOne.setBorder(Rectangle.NO_BORDER);
            cOne.setHorizontalAlignment(Element.ALIGN_LEFT);

            cTwo.setBorder(Rectangle.NO_BORDER);
            cTwo.setHorizontalAlignment(Element.ALIGN_LEFT);

            table2.addCell(cOne);
            table2.addCell(cTwo);
            table2.setHorizontalAlignment(Element.ALIGN_LEFT);
            table2.setHeaderRows(3);

            cOne = new PdfPCell(new Phrase(""));
            cTwo = new PdfPCell(new Phrase(
                    data.getShippingaddress().getCity() + " - " + data.getShippingaddress().getZipcode() + " ,"));

            cOne.setBorder(Rectangle.NO_BORDER);
            cOne.setHorizontalAlignment(Element.ALIGN_LEFT);

            cTwo.setBorder(Rectangle.NO_BORDER);
            cTwo.setHorizontalAlignment(Element.ALIGN_LEFT);

            table2.addCell(cOne);
            table2.addCell(cTwo);
            table2.setHorizontalAlignment(Element.ALIGN_LEFT);
            table2.setHeaderRows(4);

            cOne = new PdfPCell(new Phrase(""));
            cTwo = new PdfPCell(new Phrase(data.getShippingaddress().getState() + " ,"));

            cOne.setBorder(Rectangle.NO_BORDER);
            cOne.setHorizontalAlignment(Element.ALIGN_LEFT);

            cTwo.setBorder(Rectangle.NO_BORDER);
            cTwo.setHorizontalAlignment(Element.ALIGN_LEFT);

            table2.addCell(cOne);
            table2.addCell(cTwo);
            table2.setHorizontalAlignment(Element.ALIGN_LEFT);
            table2.setHeaderRows(5);

            cOne = new PdfPCell(new Phrase(""));
            cTwo = new PdfPCell(new Phrase(data.getShippingaddress().getCountryName() + " ,"));

            cOne.setBorder(Rectangle.NO_BORDER);
            cOne.setHorizontalAlignment(Element.ALIGN_LEFT);

            cTwo.setBorder(Rectangle.NO_BORDER);
            cTwo.setHorizontalAlignment(Element.ALIGN_LEFT);

            table2.addCell(cOne);
            table2.addCell(cTwo);
            table2.setHorizontalAlignment(Element.ALIGN_LEFT);
            table2.setHeaderRows(6);

            cOne = new PdfPCell(new Phrase(""));
            cTwo = new PdfPCell(new Phrase("Phone no : " + data.getShippingaddress().getPhone()));

            cOne.setBorder(Rectangle.NO_BORDER);
            cOne.setHorizontalAlignment(Element.ALIGN_LEFT);

            cTwo.setBorder(Rectangle.NO_BORDER);
            cTwo.setHorizontalAlignment(Element.ALIGN_LEFT);

            table2.addCell(cOne);
            table2.addCell(cTwo);
            table2.setHorizontalAlignment(Element.ALIGN_LEFT);

            document.add(table2);

            document.add(Chunk.NEWLINE);

            LineSeparator lineSeparator2 = new LineSeparator();
            lineSeparator2.setLineColor(new BaseColor(Color.parseColor("#d0dbe5")));
            document.add(lineSeparator2);

            document.add(Chunk.NEWLINE);

            PdfPTable table3 = new PdfPTable(2);
            PdfPCell c1 = new PdfPCell(new Phrase("Item Name", new Font(Font.FontFamily.UNDEFINED, 12,
                    Font.BOLD)));
            PdfPCell c2 = new PdfPCell(new Phrase("Item Unitprice      " + data.getOrderitems().getFormattedPrice(),
                    new Font(Font.FontFamily.UNDEFINED, 12,
                            Font.BOLD)));

            c1.setBorder(Rectangle.NO_BORDER);
            c1.setHorizontalAlignment(Element.ALIGN_LEFT);

            c2.setBorder(Rectangle.NO_BORDER);
            c2.setHorizontalAlignment(Element.ALIGN_LEFT);
            c2.setPaddingBottom(JoysaleApplication.dpToPx(OrderDetail.this, 5));

            table3.addCell(c1);
            table3.addCell(c2);
            table3.setHorizontalAlignment(Element.ALIGN_LEFT);
            table3.setHeaderRows(1);

           /* c1 = new PdfPCell(new Phrase(JoysaleApplication.stripHtml(data.getOrderitems().getItemname())));
            c2 = new PdfPCell(new Phrase("Shipping Fee        " + data.getOrderitems().getFormattedShippingCost(),
                    new Font(Font.FontFamily.UNDEFINED, 12,
                            Font.BOLD)));

            c1.setBorder(Rectangle.NO_BORDER);
            c1.setHorizontalAlignment(Element.ALIGN_LEFT);

            c2.setBorder(Rectangle.NO_BORDER);
            c2.setHorizontalAlignment(Element.ALIGN_LEFT);
            c2.setPaddingBottom(JoysaleApplication.dpToPx(OrderDetail.this, 5));

            table3.addCell(c1);
            table3.addCell(c2);
            table3.setHorizontalAlignment(Element.ALIGN_LEFT);
            table3.setHeaderRows(2);*/

            c1 = new PdfPCell(new Phrase(""));
            c2 = new PdfPCell(new Phrase("Total Price            " + data.getOrderitems().getFormattedTotal(),
                    new Font(Font.FontFamily.UNDEFINED, 12,
                            Font.BOLD)));

            c1.setBorder(Rectangle.NO_BORDER);
            c1.setHorizontalAlignment(Element.ALIGN_LEFT);

            c2.setHorizontalAlignment(Element.ALIGN_LEFT);
            c2.setCellEvent(new LineCell());
            c2.setBorder(Rectangle.NO_BORDER);
            c2.setPaddingTop(JoysaleApplication.dpToPx(OrderDetail.this, 5));
            c2.setPaddingBottom(JoysaleApplication.dpToPx(OrderDetail.this, 5));

            table3.addCell(c1);
            table3.addCell(c2);
            table3.setHorizontalAlignment(Element.ALIGN_LEFT);

            document.add(table3);

        } catch (DocumentException e) {
            e.printStackTrace();
        }

    }

    private void setData() {
        Picasso.get().load(data.getOrderitems().getOrderImage()).fit().centerCrop().into(itemImage);
        itemName.setText(JoysaleApplication.stripHtml(data.getOrderitems().getItemname()));
        itemPrice.setText(data.getOrderitems().getFormattedPrice());
        orderid.setText(data.getOrderid());
        paymenttype.setText(data.getPaymentType());
        transactionid.setText(data.getTransactionId());
        titleimage.setBackground(null);

        if (from.equals("order")) {
            printbtn.setVisibility(View.GONE);
            title.setText(data.getOrderitems().getSellerName());
            Picasso.get()
                    .load(data.getOrderitems().getSellerImage())
                    .placeholder(R.drawable.appicon)
                    .error(R.drawable.appicon)
                    .into(titleimage);
            chatseller.setText(getString(R.string.chat_with_seller));
            sellername.setText(data.getOrderitems().getSellerName());
            /*
             * if (data.getStatus().equalsIgnoreCase("processing") &&
             * data.getCancel().equalsIgnoreCase("false")) {
             * settingbtn.setVisibility(View.GONE);
             * }
             */
        } else {
            title.setText(data.getOrderitems().getBuyerName());
            Picasso.get()
                    .load(data.getOrderitems().getBuyerImg())
                    .placeholder(R.drawable.appicon)
                    .error(R.drawable.appicon)
                    .into(titleimage);
            printbtn.setVisibility(View.VISIBLE);
            chatseller.setText(getString(R.string.chat_with_buyer));
            selrtext.setText(getString(R.string.buyer_name));
            sellername.setText(data.getOrderitems().getBuyerName());
        }

        if (data.getStatus().equals("cancelled")) {
            settingbtn.setVisibility(View.GONE);
            printbtn.setVisibility(View.GONE);
        }

        if (LocaleManager.isRTL(OrderDetail.this)) {
            itemName.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        } else {
            itemName.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        }

        try {
            if (data.getSaledate() != null && !data.getSaledate().equals("")) {
                long ordDate = Long.parseLong(data.getSaledate());
                odate = JoysaleApplication.getDate(OrderDetail.this, ordDate);
            }
            if (data.getTrackingdetails() != null && data.getTrackingdetails().getShippingdate() != null
                    && !data.getTrackingdetails().getShippingdate().equals("") &&
                    !data.getTrackingdetails().getShippingdate().equals("0")
                    && !data.getStatus().equalsIgnoreCase("processing") &&
                    !data.getStatus().equalsIgnoreCase("pending")) {
                long shipDate = Long.parseLong(data.getTrackingdetails().getShippingdate());
                sdate = JoysaleApplication.getDate(OrderDetail.this, shipDate);
            } else {
                shippedDateTxt.setVisibility(View.GONE);
                shipdate.setVisibility(View.GONE);
                deliveryDateTxt.setVisibility(View.GONE);
                deliverydate.setVisibility(View.GONE);
            }
            if (data.getDeliverydate() != null && !data.getDeliverydate().equals("") &&
                    !data.getDeliverydate().equals("0") && !data.getStatus().equalsIgnoreCase("processing") &&
                    !data.getStatus().equalsIgnoreCase("pending") && !data.getStatus().equalsIgnoreCase("shipped")) {
                long delDate = Long.parseLong(data.getDeliverydate());
                ddate = JoysaleApplication.getDate(OrderDetail.this, delDate);
            } else {
                deliveryDateTxt.setVisibility(View.GONE);
                deliverydate.setVisibility(View.GONE);
            }
            if (data.getStatus().equalsIgnoreCase("delivered") || data.getStatus().equalsIgnoreCase("paid")) {
                shippedDateTxt.setVisibility(View.GONE);
                shipdate.setVisibility(View.GONE);
                deliveryDateTxt.setVisibility(View.GONE);
                deliverydate.setVisibility(View.GONE);
            }
            orderdate.setText(odate);
            shipdate.setText(sdate);
            deliverydate.setText(ddate);
        } catch (Exception e) {
            e.printStackTrace();
        }

        price.setText(data.getOrderitems().getFormattedPrice());
        shipingprice.setText(data.getOrderitems().getFormattedShippingCost());
        total.setText(data.getOrderitems().getFormattedTotal());
        nickName.setText(data.getShippingaddress().getNickName() != null ? data.getShippingaddress().getNickName()
                : data.getShippingaddress().getNickname());
        fullName.setText(data.getShippingaddress().getName());
        addressLine.setText(data.getShippingaddress().getAddress1() + " , " + data.getShippingaddress().getAddress2());
        city.setText(data.getShippingaddress().getCity() + "," + data.getShippingaddress().getState() + " , "
                + data.getShippingaddress().getZipcode());
        country.setText(data.getShippingaddress().getCountryName() + "\n" + data.getShippingaddress().getPhone());
        setTrackingDetails();

        if (from.equals("sale") || data.getReviewId().equals("0")) {
            reviewLay.setVisibility(View.GONE);
        } else {
            reviewLay.setVisibility(View.VISIBLE);
            reviewTitle.setText(data.getReviewTitle());
            review.setText(data.getReviewDes());
            try {
                ratingBar.setRating(Float.parseFloat(data.getRating()));
                ratingCount.setText("(" + data.getRating() + ")");
                long date = Long.parseLong(data.getCreatedDate());
                reviewDate.setText(getString(R.string.on) + " " + JoysaleApplication.getDate(OrderDetail.this, date));
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (NumberFormatException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        switch (data.getStatus()) {
            case "pending":
                orderstatus.setText(getString(R.string.pending));
                orderstatus.setBackground(getResources().getDrawable(R.drawable.dark_round_corner));
                break;
            case "processing":
                orderstatus.setText(getString(R.string.under_processing));
                orderstatus.setBackground(getResources().getDrawable(R.drawable.dark_round_corner));
                break;
            case "cancelled":
                orderstatus.setText(getString(R.string.order_canceled));
                orderstatus.setBackground(getResources().getDrawable(R.drawable.red_round_corner));
                break;
            case "claimed":
                orderstatus.setText(getString(R.string.claimed));
                orderstatus.setBackground(getResources().getDrawable(R.drawable.round_corner_primary));
                break;
            case "delivered":
                orderstatus.setText(getString(R.string.item_delivered));
                if (data.getReviewId().equals("0") && from.equals("order")) {
                    writeReviewLay.setVisibility(View.VISIBLE);
                }
                orderstatus.setBackground(getResources().getDrawable(R.drawable.round_corner_primary));
                settingbtn.setVisibility(View.GONE);
                break;
            case "paid":
                if (from.equals("sale")) {
                    orderstatus.setText(getString(R.string.paid));
                    orderstatus.setBackground(getResources().getDrawable(R.drawable.round_corner_primary));
                } else {
                    orderstatus.setText(getString(R.string.item_delivered));
                    if (data.getReviewId().equals("0") && from.equals("order")) {
                        writeReviewLay.setVisibility(View.VISIBLE);
                    }
                    orderstatus.setBackground(getResources().getDrawable(R.drawable.round_corner_primary));
                }
                settingbtn.setVisibility(View.GONE);
                break;
            case "shipped":
                orderstatus.setText(getString(R.string.item_shipped));
                orderstatus.setBackground(getResources().getDrawable(R.drawable.orange_round_corner));
                break;
        }

        orderstatus.setPadding(leftPadding, topPadding, leftPadding, topPadding);

    }

    /**
     * Show courier tracking for both buyer and seller once the order is shipped or delivered.
     */
    private void setTrackingDetails() {
        MySalesResponse.Trackingdetails tracking = data.getTrackingdetails();
        String status = data.getStatus() == null ? "" : data.getStatus();
        boolean showTracking = tracking != null
                && (status.equalsIgnoreCase("shipped")
                || status.equalsIgnoreCase("delivered")
                || status.equalsIgnoreCase("paid")
                || status.equalsIgnoreCase("claimed"));
        if (!showTracking) {
            trackingLay.setVisibility(View.GONE);
            return;
        }
        trackingLay.setVisibility(View.VISIBLE);
        trackingId.setText(trackingValue(tracking.getTrackingid()));
        trackingLogisticName.setText(trackingValue(tracking.getCouriername()));
        trackingCourierService.setText(trackingValue(tracking.getCourierservice()));
        trackingNotes.setText(trackingValue(tracking.getNotes()));

        String shipmentDate = "-";
        try {
            if (tracking.getShippingdate() != null
                    && !tracking.getShippingdate().equals("")
                    && !tracking.getShippingdate().equals("0")) {
                long shipDate = Long.parseLong(tracking.getShippingdate());
                shipmentDate = JoysaleApplication.getDate(OrderDetail.this, shipDate);
                if (shipmentDate == null || shipmentDate.equals("")) {
                    shipmentDate = "-";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        trackingShipmentDate.setText(shipmentDate);
    }

    private String trackingValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "-";
        }
        return value.trim();
    }

    /**
     * for show the popupmenu window
     **/

    public void shareImage(View v) {
        String[] values = { "" };
        try {
            if (data.getStatus().equalsIgnoreCase("pending")) {
                if (from.equals("sale")) {
                    values = new String[] { getString(R.string.mark_as_processing) };
                } else if (data.getCancel().equalsIgnoreCase("true")) {
                    values = new String[] { getString(R.string.cancel_your_order) };
                }
            } else if (data.getStatus().equalsIgnoreCase("processing")) {
                if (from.equals("sale")) {
                    // Restored: Seller can mark order as shipped and enter tracking ID
                    values = new String[] { getString(R.string.mark_as_shipped) };
                } else if (data.getCancel().equalsIgnoreCase("true")) {
                    values = new String[] { getString(R.string.cancel_your_order) };
                }
            } else if (data.getStatus().equalsIgnoreCase("shipped")) {
                if (from.equals("sale")) {
                    if (data.getClaim().equalsIgnoreCase("true")) {
                        values = new String[] { getString(R.string.edit_tracking),
                                getString(R.string.mark_as_claimed) };
                    } else {
                        values = new String[] { getString(R.string.edit_tracking) };
                    }
                } else {
                    values = new String[] { getString(R.string.shipping_details),
                            getString(R.string.mark_as_received) };
                }
            } else if (data.getStatus().equalsIgnoreCase("delivered")) {
                if (from.equals("sale") && data.getClaim().equals("true")) {
                    // Restored: Seller can view shipping details along with mark as claimed
                    values = new String[] { getString(R.string.shipping_details),
                            getString(R.string.mark_as_claimed) };
                } else {
                    // Restored: Buyer can view shipping details
                    values = new String[] { getString(R.string.shipping_details) };
                }
            } else if (data.getStatus().equalsIgnoreCase("claimed")) {
                if (from.equals("sale")) {
                    values = new String[] { getString(R.string.shipping_details) };
                } else {
                    values = new String[] { getString(R.string.shipping_details),
                            getString(R.string.mark_as_received) };
                }
            } else if (data.getStatus().equalsIgnoreCase("paid")) {
                if (from.equals("sale")) {
                    values = new String[] { getString(R.string.shipping_details) };
                } else {
                    values = new String[] { getString(R.string.shipping_details) };
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.share_new, android.R.id.text1, values);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.share, null);
        layout.setAnimation(AnimationUtils.loadAnimation(this, R.anim.grow_from_topright_to_bottomleft));
        final PopupWindow popup = new PopupWindow(OrderDetail.this);
        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup.setContentView(layout);
        popup.setWidth(displayMetrics.widthPixels * 60 / 100);
        popup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popup.setFocusable(true);
        popup.showAtLocation(main, Gravity.TOP | Gravity.RIGHT, 0, 20);

        final ListView lv = (ListView) layout.findViewById(R.id.lv);
        lv.setAdapter(adapter);
        popup.showAsDropDown(v);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int pos, long id) {
                switch (pos) {
                    case 0:
                        if (data.getStatus().equalsIgnoreCase("pending") && from.equals("sale")) {
                            confirmdialog(getString(R.string.processing_confirmation), "Processing");
                        } else if (data.getStatus().equalsIgnoreCase("pending")
                                && data.getCancel().equalsIgnoreCase("true")) {
                            confirmdialog(getString(R.string.cancel_confirmation), "cancel");
                        } else if (data.getStatus().equalsIgnoreCase("processing") && from.equals("sale")) {
                            // Restored: Opens ShippingDetail so seller can enter Tracking ID & mark as shipped
                            Intent j = new Intent(OrderDetail.this, ShippingDetail.class);
                            j.putExtra(Constants.TAG_DATA, data);
                            j.putExtra(Constants.TAG_FROM, from);
                            j.putExtra("position", position);
                            startActivity(j);
                        } else if (data.getStatus().equalsIgnoreCase("processing")
                                && data.getCancel().equalsIgnoreCase("true")) {
                            confirmdialog(getString(R.string.cancel_confirmation), "cancel");
                        } else if (data.getStatus().equalsIgnoreCase("shipped") && from.equals("sale")) {
                            // Restored: Opens ShippingDetail so seller can edit tracking details
                            Intent j = new Intent(OrderDetail.this, ShippingDetail.class);
                            j.putExtra(Constants.TAG_DATA, data);
                            j.putExtra(Constants.TAG_FROM, from);
                            j.putExtra("position", position);
                            startActivity(j);
                        } else if (data.getStatus().equalsIgnoreCase("shipped")) {
                            // Restored: Opens ShippingDetail in read-only mode so buyer can view Tracking ID
                            Intent j = new Intent(OrderDetail.this, ShippingDetail.class);
                            j.putExtra(Constants.TAG_DATA, data);
                            j.putExtra(Constants.TAG_FROM, from);
                            j.putExtra("position", position);
                            startActivity(j);
                        } else if (data.getStatus().equalsIgnoreCase("delivered")) {
                            // Restored: Opens ShippingDetail so user can view past shipping details for delivered order
                            Intent j = new Intent(OrderDetail.this, ShippingDetail.class);
                            j.putExtra(Constants.TAG_DATA, data);
                            j.putExtra(Constants.TAG_FROM, from);
                            j.putExtra("position", position);
                            startActivity(j);
                        } else if (data.getStatus().equalsIgnoreCase("claimed") && from.equals("sale")) {
                            // Restored: Opens ShippingDetail to view shipping details
                            Intent j = new Intent(OrderDetail.this, ShippingDetail.class);
                            j.putExtra(Constants.TAG_DATA, data);
                            j.putExtra(Constants.TAG_FROM, from);
                            j.putExtra("position", position);
                            startActivity(j);
                        } else if (data.getStatus().equalsIgnoreCase("claimed") ||
                                data.getStatus().equalsIgnoreCase("paid")) {
                            // Restored: Opens ShippingDetail to view shipping details
                            Intent j = new Intent(OrderDetail.this, ShippingDetail.class);
                            j.putExtra(Constants.TAG_DATA, data);
                            j.putExtra(Constants.TAG_FROM, from);
                            j.putExtra("position", position);
                            startActivity(j);
                        }
                        popup.dismiss();
                        break;
                    case 1:
                        if (data.getStatus().equalsIgnoreCase("shipped") && from.equals("sale") &&
                                data.getClaim().equalsIgnoreCase("true")) {
                            confirmdialog(getString(R.string.claim_confirmation), "claim");
                        } else if (data.getStatus().equalsIgnoreCase("shipped")) {
                            confirmdialog(getString(R.string.deliver_confirmation), "Delivered");
                        } else if (data.getStatus().equalsIgnoreCase("delivered") && from.equals("sale") &&
                                data.getClaim().equalsIgnoreCase("true")) {
                            confirmdialog(getString(R.string.claim_confirmation), "claim");
                        } else if (data.getStatus().equalsIgnoreCase("claimed") && from.equals("order")) {
                            confirmdialog(getString(R.string.deliver_confirmation), "Delivered");
                        }
                        popup.dismiss();
                        break;
                }
            }
        });
    }

    public void confirmdialog(final String title, final String orderStatus) {
        final Dialog dialog = new Dialog(OrderDetail.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.default_dialog);

        dialog.getWindow().setLayout(displayMetrics.widthPixels * 90 / 100, LinearLayout.LayoutParams.WRAP_CONTENT);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        // wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);

        TextView message = (TextView) dialog.findViewById(R.id.alert_msg);
        TextView ok = (TextView) dialog.findViewById(R.id.alert_button);
        TextView cancel = (TextView) dialog.findViewById(R.id.cancel_button);

        message.setText(title);

        cancel.setVisibility(View.VISIBLE);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                changeOrderStatus(orderStatus);
            }
        });
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public class MyPrintDocumentAdapter extends PrintDocumentAdapter {
        File file;

        public MyPrintDocumentAdapter(File f) {
            file = f;
        }

        @Override
        public void onLayout(PrintAttributes oldAttributes,
                PrintAttributes newAttributes,
                CancellationSignal cancellationSignal,
                LayoutResultCallback callback, Bundle extras) {
            if (cancellationSignal.isCanceled()) {
                callback.onLayoutCancelled();
                return;
            }

            PrintDocumentInfo pdi = new PrintDocumentInfo.Builder(pdfName)
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT).build();

            callback.onLayoutFinished(pdi, true);
        }

        @Override
        public void onWrite(final PageRange[] pageRanges,
                final ParcelFileDescriptor destination,
                final CancellationSignal cancellationSignal,
                final WriteResultCallback callback) {
            InputStream input = null;
            OutputStream output = null;
            try {

                input = new FileInputStream(file);
                output = new FileOutputStream(destination.getFileDescriptor());

                byte[] buf = new byte[1024];
                int bytesRead;

                while ((bytesRead = input.read(buf)) > 0) {
                    output.write(buf, 0, bytesRead);
                }

                callback.onWriteFinished(new PageRange[] { PageRange.ALL_PAGES });

            } catch (FileNotFoundException ee) {
                ee.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (input != null)
                        input.close();
                    if (output != null)
                        output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private int computePageCount(PrintAttributes printAttributes) {
            int itemsPerPage = 4; // default item count for portrait mode

            PrintAttributes.MediaSize pageSize = printAttributes.getMediaSize();
            if (!pageSize.isPortrait()) {
                // Six items per page in landscape orientation
                itemsPerPage = 3;
            }
            // Determine number of print items
            int printItemCount = 5;

            return (int) Math.ceil(printItemCount / itemsPerPage);
        }
    }

    class HeaderFooterPageEvent extends PdfPageEventHelper {

        public void onStartPage(PdfWriter writer, Document document) {
            Rectangle rect = writer.getBoxSize("art");
            String header = getString(R.string.app_name) + " | " + getString(R.string.app_name)
                    + " - Online Classifieds Hub for Buying and Selling Used goods.";
            long unixTime = System.currentTimeMillis();
            String date = JoysaleApplication.getDate(unixTime, "dd/MM/yy, h:mm a");
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_LEFT, new Phrase(header),
                    rect.getLeft(), rect.getTop(), 0);
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, new Phrase(date),
                    rect.getRight(), rect.getTop(), 0);
        }

        public void onEndPage(PdfWriter writer, Document document) {
            Rectangle rect = writer.getBoxSize("art");
            String url = Constants.SITE_URL + "sales";
            String page = "Page" + document.getPageNumber() + " of " + document.getPageNumber();
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_LEFT, new Phrase(url), rect.getLeft(),
                    rect.getBottom(), 0);
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, new Phrase(page),
                    rect.getRight(), rect.getBottom(), 0);
        }

    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        JoysaleApplication.networkError(OrderDetail.this, isConnected);
    }

    class LineCell implements PdfPCellEvent {

        @Override
        public void cellLayout(PdfPCell cell, Rectangle position,
                PdfContentByte[] canvases) {
            PdfContentByte canvas = canvases[PdfPTable.LINECANVAS];
            canvas.setLineDash(0f, 0f);
            canvas.setColorStroke(BaseColor.LIGHT_GRAY);
            canvas.moveTo(position.getLeft(), position.getTop());
            canvas.lineTo(position.getRight(), position.getTop());
            canvas.moveTo(position.getLeft(), position.getBottom());
            canvas.lineTo(position.getRight(), position.getBottom());
            canvas.stroke();
        }
    }

    private void changeOrderStatus(final String orderStatus) {
        if (NetworkReceiver.isConnected()) {
            if (!dialog.isShowing())
                dialog.show();
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_ORDER_ID, data.getOrderid());
            map.put(Constants.TAG_CH_STATUS, orderStatus);

            Call<HashMap<String, String>> call = apiInterface.addShippingDetails(map);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call,
                        retrofit2.Response<HashMap<String, String>> response) {
                    if (response.isSuccessful()) {
                        if (dialog.isShowing())
                            dialog.dismiss();
                        if (response.body().get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {

                            String message = response.body().get(Constants.TAG_RESULT);
                            if (message.equalsIgnoreCase("Status changed to cancelled")) {
                                Toast.makeText(OrderDetail.this, getString(R.string.status_changed_to_cancelled),
                                        Toast.LENGTH_SHORT).show();
                                data.setStatus("cancelled");
                                setData();
                                if (from.equals("order")) {
                                    MyOrder.Orderary.get(position).setStatus("cancelled");
                                    MyOrder.orderAdapter.notifyDataSetChanged();
                                } else {
                                    MySale.Saleary.get(position).setStatus("cancelled");
                                    MySale.saleAdapter.notifyDataSetChanged();
                                }
                            } else if (message.equalsIgnoreCase("Status changed to claimed")) {
                                Toast.makeText(OrderDetail.this, getString(R.string.status_changed_to_claimed),
                                        Toast.LENGTH_SHORT).show();
                                if (from.equals("order")) {
                                    data.setStatus("shipped");
                                    setData();
                                    MyOrder.Orderary.get(position).setStatus("shipped");
                                    MyOrder.orderAdapter.notifyDataSetChanged();
                                } else {
                                    data.setStatus("claimed");
                                    setData();
                                    MySale.Saleary.get(position).setStatus("claimed");
                                    MySale.saleAdapter.notifyDataSetChanged();
                                }
                            } else if (message.equalsIgnoreCase("Status changed to Processing")) {
                                Toast.makeText(OrderDetail.this, getString(R.string.status_changed_to_processing),
                                        Toast.LENGTH_SHORT).show();
                                data.setStatus("processing");
                                setData();
                                if (from.equals("order")) {
                                    MyOrder.Orderary.get(position).setStatus("processing");
                                    MyOrder.orderAdapter.notifyDataSetChanged();
                                } else {
                                    MySale.Saleary.get(position).setStatus("processing");
                                    MySale.saleAdapter.notifyDataSetChanged();
                                }
                            } else if (message.equalsIgnoreCase("Status changed to delivered")) {
                                Toast.makeText(OrderDetail.this, getString(R.string.status_changed_to_delivered),
                                        Toast.LENGTH_SHORT).show();
                                data.setStatus("delivered");
                                setData();
                                if (from.equals("order")) {
                                    MyOrder.Orderary.get(position).setStatus("delivered");
                                    MyOrder.orderAdapter.notifyDataSetChanged();
                                } else {
                                    MySale.Saleary.get(position).setStatus("delivered");
                                    MySale.saleAdapter.notifyDataSetChanged();
                                }
                            }
                        } else if (("" + response.body().get(Constants.TAG_RESULT))
                                .equalsIgnoreCase("No product found")) {
                            Toast.makeText(OrderDetail.this, getString(R.string.product_removed_msg),
                                    Toast.LENGTH_SHORT).show();
                            orderstatus.setText(getString(R.string.order_canceled));
                            orderstatus.setBackground(getResources().getDrawable(R.drawable.red_round_corner));
                            orderstatus.setPadding(leftPadding, topPadding, leftPadding, topPadding);
                            if (from.equals("order")) {
                                MyOrder.Orderary.get(position).setStatus("cancelled");
                                MyOrder.orderAdapter.notifyDataSetChanged();
                            } else {
                                MySale.Saleary.get(position).setStatus("cancelled");
                                MySale.saleAdapter.notifyDataSetChanged();
                            }
                            Picasso.get()
                                    .load(R.drawable.no_item)
                                    .centerInside()
                                    .into(itemImage);

                        } else {
                            Toast.makeText(OrderDetail.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT)
                                    .show();
                        }
                    } else {
                        Toast.makeText(OrderDetail.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                        if (dialog.isShowing())
                            dialog.dismiss();
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    Toast.makeText(OrderDetail.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                    if (dialog.isShowing())
                        dialog.dismiss();
                }
            });
        }
    }

    private void getChatId(final String from) {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
            if (from.equals("order")) {
                map.put(Constants.TAG_RECEIVER_ID, data.getOrderitems().getSellerId());
            } else {
                map.put(Constants.TAG_RECEIVER_ID, data.getOrderitems().getBuyerId());
            }

            Call<HashMap<String, String>> call = apiInterface.getChatId(map);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call,
                        retrofit2.Response<HashMap<String, String>> response) {
                    if (dialog.isShowing() && dialog != null) {
                        dialog.dismiss();
                    }
                    if (response.isSuccessful()) {
                        if (response.body().get(Constants.TAG_STATUS).equals("true")) {
                            chatId = response.body().get("chat_id");
                        } else if (response.body().get(Constants.TAG_STATUS)
                                .equalsIgnoreCase(Constants.TAG_FALSE_CODE)) {
                            JoysaleApplication.disabledialog(context, "" + response.body().get(Constants.TAG_MESSAGE),
                                    GetSet.getUserId());
                        }
                        chatseller.setOnClickListener(OrderDetail.this);
                        Intent i = new Intent(OrderDetail.this, ChatActivity.class);
                        if (from.equals("order")) {
                            i.putExtra(Constants.TAG_USERNAME, data.getOrderitems().getSellerUserName());
                            i.putExtra(Constants.TAG_USER_ID, data.getOrderitems().getSellerId());
                            i.putExtra(Constants.TAG_USERIMAGE_M, data.getOrderitems().getSellerImage());
                            i.putExtra(Constants.TAG_FULL_NAME, data.getOrderitems().getSellerName());
                        } else {
                            i.putExtra(Constants.TAG_USERNAME, data.getOrderitems().getBuyerUsername());
                            i.putExtra(Constants.TAG_USER_ID, data.getOrderitems().getBuyerId());
                            i.putExtra(Constants.TAG_USERIMAGE_M, data.getOrderitems().getBuyerImg());
                            i.putExtra(Constants.TAG_FULL_NAME, data.getOrderitems().getBuyerName());
                        }
                        i.putExtra(Constants.CHATID, chatId);
                        i.putExtra(Constants.TAG_FROM, "orders");
                        startActivity(i);
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backbtn:
                finish();
                break;
            case R.id.settingbtn:
                shareImage(v);
                break;
            case R.id.chatseller:
                if (JoysaleApplication.isNetworkAvailable(OrderDetail.this)) {
                    chatseller.setOnClickListener(null);
                    dialog.show();
                    getChatId(from);
                } else {
                    JoysaleApplication.dialog(OrderDetail.this, getResources().getString(R.string.error),
                            getResources().getString(R.string.checkconnection));
                }
                break;
            case R.id.printbtn:
                createPdf();
                break;
            case R.id.writeReviewLay:
                Intent l = new Intent(OrderDetail.this, WriteReview.class);
                l.putExtra(Constants.TAG_FROM, "write");
                l.putExtra("data", data);
                l.putExtra(Constants.TAG_REVIEW_TYPE, "order");
                l.putExtra("position", position);
                startActivity(l);
                break;
            case R.id.editReview:
                Intent k = new Intent(OrderDetail.this, WriteReview.class);
                k.putExtra(Constants.TAG_FROM, "edit");
                k.putExtra("data", data);
                k.putExtra(Constants.TAG_REVIEW_TYPE, "order");
                k.putExtra("position", position);
                startActivity(k);
                break;
        }
    }
}