//package com.app.addons.buynow;
//
//import android.content.Context;
//import android.content.Intent;
//import android.net.Uri;
//import android.os.Build;
//import android.os.Bundle;
//import android.os.CancellationSignal;
//import android.os.Environment;
//import android.os.ParcelFileDescriptor;
//import android.print.PageRange;
//import android.print.PrintAttributes;
//import android.print.PrintDocumentAdapter;
//import android.print.PrintDocumentInfo;
//import android.print.PrintManager;
//import android.util.Log;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.ViewTreeObserver;
//import android.widget.ImageView;
//import android.widget.SeekBar;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.google.android.material.bottomsheet.BottomSheetBehavior;
//import com.app.datosapp.JoysaleApplication;
//import com.app.datosapp.R;
//import com.app.model.MySalesResponse;
//import com.app.utils.Constants;
//import com.otaliastudios.printer.DocumentTextView;
//import com.otaliastudios.printer.DocumentView;
//import com.otaliastudios.printer.JpegPrinter;
//import com.otaliastudios.printer.PdfPrinter;
//import com.otaliastudios.printer.PngPrinter;
//import com.otaliastudios.printer.PrintCallback;
//import com.otaliastudios.printer.PrintSize;
//import com.otaliastudios.printer.PrinterLogger;
//import com.otaliastudios.zoom.ZoomLogger;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.util.Random;
//
//// TODO: add insets and dividerWidth
//// TODO: show the WRAP_CONTENT size better
//
//public class PrinterActivity extends AppCompatActivity implements PrintCallback, View.OnClickListener {
//
//    // Pass the mime type as printId so it's easy to open
//    private final static String PRINT_PDF = "application/pdf";
//    private static final String TAG ="PDFPrinter" ;
//    private JpegPrinter mJpegPrinter;
//    private PngPrinter mPngPrinter;
//    private PdfPrinter mPdfPrinter;
//    private DocumentView mDocument;
//    ImageView backbtn;
//    private String mFilename;
//    private File mDirectory;
//     TextView textViewTitle;
//    private ViewGroup mPanel;
//    MySalesResponse.Result data = new MySalesResponse().new Result();
//    DocumentTextView documentTextViewFooterRight,documentTextViewFooterLeft,documentTextViewheaderPrinterLeft,documentTextViewheaderPrinterRight,
//    documentTextViewheaderPrinterPaymentMethod,documentTextViewheaderPrinterPaymentStatus,
//    documentTextViewPrinterBuyerName,documentTextViewPrinterBuyerEmail,documentTextViewPrinterBuyerAddress,
//    documentTextViewPrinterBuyerPhoneno,documentTextViewHeaderPrinterOrder,documentTextViewPrinterProductName,
//    documentTextViewPrinterItemUnitPrice,documentTextViewPrinterItemShippingFee,documentTextViewPrinterItemTotalPrice;
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        PrinterLogger.setLogLevel(PrinterLogger.LEVEL_VERBOSE);
//        ZoomLogger.setLogLevel(ZoomLogger.LEVEL_ERROR);
//        setContentView(R.layout.activity_printer);
//        mDocument = findViewById(R.id.pdf_preview);
//        mPdfPrinter = new PdfPrinter(mDocument, this);
//        textViewTitle = findViewById(R.id.title);
//        textViewTitle.setVisibility(View.VISIBLE);
//        backbtn = (ImageView) findViewById(R.id.backbtn);
//        documentTextViewPrinterProductName = findViewById(R.id.printerProductName);
//        documentTextViewFooterRight = findViewById(R.id.footerPrinterRight);
//        documentTextViewFooterLeft = findViewById(R.id.footerPrinterLeft);
//        documentTextViewPrinterItemUnitPrice = findViewById(R.id.printerItemUnitPrice);
//        documentTextViewPrinterItemShippingFee = findViewById(R.id.printerItemShippingFee);
//        documentTextViewPrinterItemTotalPrice = findViewById(R.id.printerItemTotalPrice);
//        documentTextViewheaderPrinterLeft = findViewById(R.id.headerPrinterLeft);
//        documentTextViewheaderPrinterRight = findViewById(R.id.headerPrinterRight);
//        documentTextViewheaderPrinterPaymentMethod = findViewById(R.id.headerPrinterPaymentMethod);
//        documentTextViewheaderPrinterPaymentStatus = findViewById(R.id.headerPrinterPaymentStatus);
//        documentTextViewPrinterBuyerName = findViewById(R.id.printerBuyerName);
//        documentTextViewPrinterBuyerEmail = findViewById(R.id.printerBuyerEmail);
//        documentTextViewPrinterBuyerAddress = findViewById(R.id.printerBuyerAddress);
//        documentTextViewPrinterBuyerPhoneno = findViewById(R.id.printerBuyerPhone);
//        documentTextViewHeaderPrinterOrder = findViewById(R.id.headerPrinterOrder);
//        backbtn.setVisibility(View.VISIBLE);
//        textViewTitle.setText("Invoice");
//        mDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
//        Random gen = new Random();
//        int n = 10000;
//        n = gen.nextInt(n);
//        mFilename = "transaction-" + n + ".pdf";
//        data = (MySalesResponse.Result) getIntent().getExtras().get("PrintData");
//        Log.d(TAG, "onCreate: "+data);
//        addTitle();
//        findViewById(R.id.print_pdf).setOnClickListener(this);
//        findViewById(R.id.edit).setOnClickListener(this);
//        mPanel = findViewById(R.id.controls);
//        mPanel.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                BottomSheetBehavior b = BottomSheetBehavior.from(mPanel);
//                b.setState(BottomSheetBehavior.STATE_HIDDEN);
//                mPanel.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//            }
//        });
//        setupControls();
//
//
//        backbtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onBackPressed();
//            }
//        });
//    }
//
//    @Override
//    public void onClick(View view) {
//        switch (view.getId()) {
//            case R.id.print_pdf: print(PRINT_PDF); break;
//            case R.id.edit:
//                BottomSheetBehavior b = BottomSheetBehavior.from(mPanel);
//                b.setState(BottomSheetBehavior.STATE_COLLAPSED);
//                break;
//
//        }
//    }
//
//
//    @Override
//    public void onBackPressed() {
//        BottomSheetBehavior b = BottomSheetBehavior.from(mPanel);
//        if (b.getState() != BottomSheetBehavior.STATE_HIDDEN) {
//            b.setState(BottomSheetBehavior.STATE_HIDDEN);
//            return;
//        }
//        super.onBackPressed();
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (mPdfPrinter.onRequestPermissionRequest(requestCode, permissions, grantResults)) {
//            print(PRINT_PDF);
//        }
//    }
//
//    private void print(String id) {
//        if (PRINT_PDF.equals(id)) {
//            mPdfPrinter.print(PRINT_PDF, mDirectory, mFilename);
//        }
//    }
//
//    private void refreshGallery(File file) {
//        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//        mediaScanIntent.setData(Uri.fromFile(file));
//        sendBroadcast(mediaScanIntent);
//    }
//
//
//
//    private void addTitle() {
//        Log.d(TAG, "addTitle: "+"title");
//        try {
//            String header = getString(R.string.app_name) + " | " + getString(R.string.app_name) + " - Online Classifieds Hub for Buying and Selling Used goods.";
//            long unixTime = System.currentTimeMillis();
//            String date = "";
//            if (data.getSaledate() != null && !data.getSaledate().equals("")) {
//                long ordDate = Long.parseLong(data.getSaledate()) * 1000;
//                date = JoysaleApplication.getDate(ordDate, "dd/MM/yyyy");
//            }
//            String title = "\n\n" + "Order # " + data.getInvoice() + " on " + date;
//            documentTextViewHeaderPrinterOrder.setText(title);
//            documentTextViewheaderPrinterLeft.setText(header);
//            documentTextViewheaderPrinterPaymentMethod.setText("Payment method : " + data.getPaymentType());
//            documentTextViewheaderPrinterPaymentStatus.setText("Payment status : Completed");
//            String stringAddress = data.getShippingaddress().getAddress1() + " ," + System.getProperty("line.separator")+data.getShippingaddress().getAddress2() + " ,"+ System.getProperty("line.separator")+data.getShippingaddress().getCity() + " - " + data.getShippingaddress().getZipcode() + " ,"+ System.getProperty("line.separator")+data.getShippingaddress().getState() + " ,"+ System.getProperty("line.separator")+data.getShippingaddress().getCountryName() + " .";
//            documentTextViewPrinterBuyerAddress.setText(stringAddress);
//            documentTextViewPrinterBuyerEmail.setText("Email : " + data.getOrderitems().getBuyerEmail());
//            documentTextViewPrinterBuyerName.setText(data.getShippingaddress().getName());
//            documentTextViewPrinterBuyerPhoneno.setText("Phone no : " + data.getShippingaddress().getPhone());
//            documentTextViewPrinterProductName.setText(JoysaleApplication.stripHtml(data.getOrderitems().getItemname()));
//            documentTextViewPrinterItemUnitPrice.setText("Item Unitprice       " + data.getOrderitems().getFormattedPrice());
//            documentTextViewPrinterItemShippingFee.setText("Shipping Fee         " + data.getOrderitems().getFormattedShippingCost());
//            documentTextViewPrinterItemTotalPrice.setText("Total Price             " + data.getOrderitems().getFormattedTotal());
//            String url = Constants.SITE_URL + "sales";
//            documentTextViewFooterLeft.setText(url);
//            documentTextViewFooterRight.setText("Page 1 of 1");
//            String dateH = JoysaleApplication.getDate(unixTime, "dd/MM/yy, h:mm a");
//            documentTextViewheaderPrinterRight.setText(dateH);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }
//
//
//    @Override
//    public void onPrint(String id, File file) {
//
//        try {
//            refreshGallery(file);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        File f = new File(file.getAbsolutePath());
//        Uri uri = Uri.fromFile(f);
//        Log.v(TAG, "Uri=" + uri);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            PrintManager printmanager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
//            String name = "CheckPrint";
//            printmanager.print(name, new MyPrintDocumentAdapter(f), null);
//        } else {
//            Intent email = new Intent(Intent.ACTION_SEND);
//            email.putExtra(Intent.EXTRA_SUBJECT, "Order Detail");
//            email.putExtra(Intent.EXTRA_STREAM, uri);
//            email.setType("message/*");
//            startActivity(Intent.createChooser(
//                    email, "Send mail..."));
//        }
//    }
//
//     /*   Intent intent = new Intent(Intent.ACTION_VIEW);
//        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
//        intent.setDataAndType(uri, id);
//        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        startActivity(intent);*//*
//    }*/
//
//    public class MyPrintDocumentAdapter extends PrintDocumentAdapter {
//        File file;
//
//        public MyPrintDocumentAdapter(File f) {
//            file = f;
//            Log.d(TAG, "MyPrintDocumentAdapter: "+file);
//            Log.d(TAG, "MyPrintDocumentAdapter: "+mFilename);
//        }
//
//        @Override
//        public void onLayout(PrintAttributes oldAttributes,
//                             PrintAttributes newAttributes,
//                             CancellationSignal cancellationSignal,
//                             LayoutResultCallback callback, Bundle extras) {
//            if (cancellationSignal.isCanceled()) {
//                callback.onLayoutCancelled();
//                return;
//            }
//
//            PrintDocumentInfo pdi = new PrintDocumentInfo.Builder(mFilename).
//                    setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT).build();
//
//            callback.onLayoutFinished(pdi, true);
//        }
//
//
//        @Override
//        public void onWrite(final PageRange[] pageRanges,
//                            final ParcelFileDescriptor destination,
//                            final CancellationSignal cancellationSignal,
//                            final WriteResultCallback callback) {
//            InputStream input = null;
//            OutputStream output = null;
//            try {
//
//                input = new FileInputStream(file);
//                output = new FileOutputStream(destination.getFileDescriptor());
//
//                byte[] buf = new byte[1024];
//                int bytesRead;
//
//                while ((bytesRead = input.read(buf)) > 0) {
//                    output.write(buf, 0, bytesRead);
//                }
//
//                callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
//
//            } catch (FileNotFoundException ee) {
//                ee.printStackTrace();
//            } catch (Exception e) {
//                e.printStackTrace();
//            } finally {
//                try {
//                    if(input != null)
//                    input.close();
//
//                    if(output != null)
//                    output.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
//    @Override
//    public void onPrintFailed(String id, Throwable error) {
//        Toast.makeText(this, "Got error while printing", Toast.LENGTH_LONG).show();
//    }
//
//    private void setupControls() {
//        // Columns
//        SeekBar col = mPanel.findViewById(R.id.control_columns);
//        final TextView colText = mPanel.findViewById(R.id.control_columns_text);
//        col.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int i, boolean fromUser) {
//                int columns = i + 1;
//                mDocument.setColumnsPerPage(columns);
//                colText.setText("" + columns);
//            }
//
//            public void onStartTrackingTouch(SeekBar seekBar) {}
//            public void onStopTrackingTouch(SeekBar seekBar) {}
//        });
//
//        // Print size
//        SeekBar size = mPanel.findViewById(R.id.control_size);
//        final TextView sizeText = mPanel.findViewById(R.id.control_size_text);
//        size.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int i, boolean fromUser) {
//                PrintSize s = null;
//                String m = null;
//                switch (i) {
//                    case 0: s = PrintSize.ISO_A4; m = "ISO A4"; break;
//                    case 1: s = PrintSize.ISO_A5; m = "ISO A5"; break;
//                    case 2: s = PrintSize.ISO_A6; m = "ISO A6"; break;
//                    case 3: s = PrintSize.ISO_A7; m = "ISO A7"; break;
//                    case 4: s = PrintSize.WRAP_CONTENT; m = "Wrap Content"; break;
//                }
//                mDocument.setPrintSize(s);
//
//                sizeText.setText(m);
//            }
//
//            public void onStartTrackingTouch(SeekBar seekBar) {}
//            public void onStopTrackingTouch(SeekBar seekBar) {}
//        });
//    }
//
//
//
//
//}
